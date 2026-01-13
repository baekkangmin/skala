# -------------------------------------------------------------
# 작성자 : 백강민
# 작성목적 : SKALA Python Day1 - Codelab1 AST(추상 구문 트리)를 활용한 자동 보안 검사기
# 작성일 : 2025-01-12
# 변경사항 내역 :
#   2025-01-12 - 최초 작성
# -------------------------------------------------------------


# -------------------------------------------------------------
# AST 기반 자동 보안 검사기 (간단 SAST)
# - 위험 함수(eval, exec, pickle.load, os.system 등) 호출을 탐지
# - 파일명 + 라인번호 + 코드 스니펫 + 탐지 규칙을 리포트로 출력
# -------------------------------------------------------------

from __future__ import annotations

import ast
import argparse
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Optional, Set, Tuple


@dataclass(frozen=True)
class Finding:
    file: str
    line: int
    col: int
    rule: str
    call: str
    snippet: str


class SecurityVisitor(ast.NodeVisitor):
    """
    모든 함수 호출(Call) 노드를 순회하면서 금지/위험 호출을 탐지한다.
    - eval(...)
    - exec(...)
    - pickle.load(...)
    - pickle.loads(...)
    - os.system(...)
    - subprocess.run/call/Popen(..., shell=True)  (추가 예시)
    """

    # "정확히 이런 함수명" (예: eval, exec)
    BANNED_SIMPLE_NAMES: Set[str] = {"eval", "exec"}

    # "모듈.함수" 형태로 금지 (예: os.system, pickle.load)
    BANNED_QUALIFIED_NAMES: Set[str] = {
        "os.system",
        "pickle.load",
        "pickle.loads",
    }

    # subprocess 계열은 shell=True일 때 더 위험하므로 별도 규칙으로 감지
    SUBPROCESS_FUNCS: Set[str] = {
        "subprocess.run",
        "subprocess.call",
        "subprocess.Popen",
    }

    def __init__(self, file_path: str, source_lines: list[str]) -> None:
        self.file_path = file_path
        self.source_lines = source_lines
        self.findings: list[Finding] = []

    def visit_Call(self, node: ast.Call) -> None:
        call_name = self._resolve_call_name(node.func)

        # 1) eval/exec
        if call_name in self.BANNED_SIMPLE_NAMES:
            self._add_finding(node, rule=f"BANNED_CALL:{call_name}", call=call_name)
            # 계속 탐색
            self.generic_visit(node)
            return

        # 2) os.system / pickle.load / pickle.loads
        if call_name in self.BANNED_QUALIFIED_NAMES:
            self._add_finding(node, rule=f"BANNED_CALL:{call_name}", call=call_name)
            self.generic_visit(node)
            return

        # 3) subprocess.* + shell=True 감지 (실무에서 특히 금지/주의)
        if call_name in self.SUBPROCESS_FUNCS and self._has_shell_true(node):
            self._add_finding(
                node,
                rule=f"DANGEROUS_SUBPROCESS_SHELL_TRUE:{call_name}",
                call=call_name,
            )
            self.generic_visit(node)
            return

        self.generic_visit(node)

    def _add_finding(self, node: ast.AST, rule: str, call: str) -> None:
        line = getattr(node, "lineno", 0)
        col = getattr(node, "col_offset", 0)
        snippet = self._get_line_snippet(line)
        self.findings.append(
            Finding(
                file=self.file_path,
                line=line,
                col=col,
                rule=rule,
                call=call,
                snippet=snippet,
            )
        )

    def _get_line_snippet(self, lineno: int) -> str:
        if 1 <= lineno <= len(self.source_lines):
            return self.source_lines[lineno - 1].rstrip("\n")
        return ""

    @staticmethod
    def _resolve_call_name(func_node: ast.AST) -> str:
        """
        호출 대상이 다음 중 무엇인지 문자열로 정규화해서 반환:
        - eval -> "eval"
        - os.system -> "os.system"
        - pickle.load -> "pickle.load"
        - (그 외는 가능한 범위에서 추정)
        """
        # eval(...)
        if isinstance(func_node, ast.Name):
            return func_node.id

        # os.system(...)
        if isinstance(func_node, ast.Attribute):
            left = SecurityVisitor._resolve_attr_left(func_node.value)
            if left:
                return f"{left}.{func_node.attr}"
            return func_node.attr  # fallback

        return "<unknown>"

    @staticmethod
    def _resolve_attr_left(node: ast.AST) -> Optional[str]:
        # os.system -> 왼쪽(os) 추출
        if isinstance(node, ast.Name):
            return node.id
        # 예: a.b.c 형태 대응
        if isinstance(node, ast.Attribute):
            left = SecurityVisitor._resolve_attr_left(node.value)
            if left:
                return f"{left}.{node.attr}"
            return node.attr
        return None

    @staticmethod
    def _has_shell_true(call_node: ast.Call) -> bool:
        # subprocess.run(..., shell=True) 같은 키워드 검사
        for kw in call_node.keywords:
            if (
                kw.arg == "shell"
                and isinstance(kw.value, ast.Constant)
                and kw.value.value is True
            ):
                return True
        return False


def scan_source(source: str, file_path: str = "<memory>") -> list[Finding]:
    source_lines = source.splitlines(keepends=True)

    try:
        tree = ast.parse(source, filename=file_path)
    except SyntaxError as e:
        # 문법 오류도 리포트로 올릴 수 있게 Finding 형태로 반환
        return [
            Finding(
                file=file_path,
                line=e.lineno or 0,
                col=e.offset or 0,
                rule="SYNTAX_ERROR",
                call="",
                snippet=(
                    source_lines[(e.lineno - 1)]
                    if e.lineno and 1 <= e.lineno <= len(source_lines)
                    else ""
                ),
            )
        ]

    visitor = SecurityVisitor(file_path=file_path, source_lines=source_lines)
    visitor.visit(tree)
    return visitor.findings


def scan_file(path: Path) -> list[Finding]:
    text = path.read_text(encoding="utf-8")
    return scan_source(text, file_path=str(path))


def iter_py_files(root: Path) -> Iterable[Path]:
    if root.is_file() and root.suffix == ".py":
        yield root
        return
    for p in root.rglob("*.py"):
        # 필요하면 venv, .venv, __pycache__ 제외
        if any(part in {".venv", "venv", "__pycache__"} for part in p.parts):
            continue
        yield p


def print_report(findings: list[Finding]) -> None:
    if not findings:
        print("✅ No issues found.")
        return

    print("🚨 Security Scan Report")
    print("-" * 80)
    for i, f in enumerate(findings, 1):
        print(f"[{i}] {f.rule}")
        print(f"    File : {f.file}")
        print(f"    Line : {f.line}:{f.col}")
        if f.call:
            print(f"    Call : {f.call}")
        if f.snippet:
            print(f"    Code : {f.snippet}")
        print("-" * 80)

    print(f"총 {len(findings)}건 발견")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="AST-based security checker (simple SAST)"
    )
    parser.add_argument(
        "target",
        help="검사할 파일(.py) 또는 디렉토리 경로",
    )
    args = parser.parse_args()

    target = Path(args.target).resolve()
    if not target.exists():
        raise SystemExit(f"대상 경로가 존재하지 않습니다: {target}")

    all_findings: list[Finding] = []
    for py_file in iter_py_files(target):
        all_findings.extend(scan_file(py_file))

    print_report(all_findings)

    # CI용: 이슈가 있으면 종료코드 1
    raise SystemExit(1 if all_findings else 0)


if __name__ == "__main__":
    main()
