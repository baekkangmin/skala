# -------------------------------------------------------------
# 작성자 : 백강민
# 작성목적 : SKALA Python Day2 - typing, mypy, 그리고 성능 측정 비교
# 작성일 : 2025-01-13
# 변경사항 내역 :
#   2025-01-13 - 최초 작성
# -------------------------------------------------------------

from __future__ import annotations

from timeit import timeit
from typing import List


# -----------------------------
# A 버전: 타입 힌트 없음
# 입력: 정수 리스트
# 출력: 각 원소 제곱의 합
# -----------------------------
def sum_of_squares_a(nums):
    total = 0
    for x in nums:
        total += x * x
    return total


# -----------------------------
# B 버전: 타입 힌트 적용
# 입력: list[int]
# 출력: int
# -----------------------------
def sum_of_squares_b(nums: List[int]) -> int:
    total: int = 0
    for x in nums:
        total += x * x
    return total


def main() -> None:
    # 테스트 데이터 (정수 리스트)
    nums = list(range(10_000))

    # correctness 체크
    a = sum_of_squares_a(nums)
    b = sum_of_squares_b(nums)
    print(f"결과 비교: A={a}, B={b}, 동일={a == b}")

    # timeit 설정
    repeat = 10_000

    t_a = timeit(lambda: sum_of_squares_a(nums), number=repeat)
    t_b = timeit(lambda: sum_of_squares_b(nums), number=repeat)

    print("\n[timeit 결과]")
    print(f"- A(타입 힌트 없음): {t_a:.6f} sec  (number={repeat})")
    print(f"- B(타입 힌트 있음): {t_b:.6f} sec  (number={repeat})")

    if t_b > 0:
        print(f"- 속도 배수(A/B): {t_a / t_b:.3f}x")


if __name__ == "__main__":
    main()
