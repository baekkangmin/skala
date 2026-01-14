# -------------------------------------------------------------
# 작성자 : Cloud 1기 백강민
# 작성목적 : SKALA Python Day3 - Pandas + Seaborn 분석 리포트 생성
# 변경사항 내역 :
#   2026-01-14 - 최초 작성
# -------------------------------------------------------------

from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns


# -----------------------------
# Config
# -----------------------------
sns.set_theme(style="whitegrid")
plt.rcParams["axes.unicode_minus"] = False


def setup_korean_font() -> None:
    # Mac: AppleGothic / Windows: Malgun Gothic / Linux: NanumGothic (없으면 기본 폰트)
    for font in ["AppleGothic", "Malgun Gothic", "NanumGothic"]:
        try:
            plt.rcParams["font.family"] = font
            break
        except Exception:
            pass


# -----------------------------
# Column inference
# -----------------------------
@dataclass(frozen=True)
class ColMap:
    text: str
    rating: str
    category: str
    sentiment: Optional[str]


TEXT_CANDIDATES = [
    "review_text",
    "review",
    "text",
    "content",
    "comment",
    "body",
    "message",
]
RATING_CANDIDATES = ["rating", "score", "stars", "star", "grade", "rate"]
CATEGORY_CANDIDATES = ["category", "product_category", "cat", "type", "group"]
SENTIMENT_CANDIDATES = [
    "sentiment_score",
    "sentiment",
    "polarity",
    "sent_score",
    "emotion_score",
]


def _first_col(df: pd.DataFrame, candidates: List[str]) -> Optional[str]:
    # case-insensitive match
    lower_map = {c.lower(): c for c in df.columns}
    for k in candidates:
        if k.lower() in lower_map:
            return lower_map[k.lower()]
    return None


def infer_columns(df: pd.DataFrame) -> ColMap:
    text = _first_col(df, TEXT_CANDIDATES)
    rating = _first_col(df, RATING_CANDIDATES)
    category = _first_col(df, CATEGORY_CANDIDATES)
    sentiment = _first_col(df, SENTIMENT_CANDIDATES)

    missing = [
        name
        for name, col in [("text", text), ("rating", rating), ("category", category)]
        if col is None
    ]
    if missing:
        raise ValueError(
            "필수 컬럼을 찾지 못했습니다: "
            + ", ".join(missing)
            + f"\n- 현재 컬럼: {list(df.columns)}\n"
            + "- 해결: CSV 컬럼명을 수정하거나, 코드 상단 *CANDIDATES에 실제 컬럼명을 추가하세요."
        )
    return ColMap(text=text, rating=rating, category=category, sentiment=sentiment)


# -----------------------------
# IO
# -----------------------------
def read_csv_safe(path: Path, encoding: str) -> pd.DataFrame:
    try:
        return pd.read_csv(path, encoding=encoding)
    except UnicodeDecodeError as e:
        raise UnicodeDecodeError(
            e.encoding,
            e.object,
            e.start,
            e.end,
            f"{e.reason}\n- 해결: --encoding utf-8-sig 또는 --encoding cp949 등을 시도하세요.",
        )


# -----------------------------
# Preprocess (Step 1)
# -----------------------------
def preprocess(
    df_raw: pd.DataFrame, cm: ColMap
) -> Tuple[pd.DataFrame, Dict[str, int], Dict[str, float]]:
    """
    - 필수 컬럼 결측 제거
    - 타입 변환
    - review_length 생성
    - IQR 기반 outlier flag 생성
    """
    df = df_raw.copy()
    before = int(len(df))

    df[cm.text] = df[cm.text].astype("string").fillna("").str.strip()
    df[cm.rating] = pd.to_numeric(df[cm.rating], errors="coerce")
    df[cm.category] = df[cm.category].astype("string").fillna("").str.strip()

    na_cells_before = int(df.isna().sum().sum())

    # 필수값 결측/빈값 제거
    df = df[df[cm.text].str.len() > 0]
    df = df.dropna(subset=[cm.rating])
    df = df[df[cm.category].str.len() > 0]

    # sentiment는 "있으면" 숫자화 + 결측은 median으로 채움
    if cm.sentiment is not None:
        df[cm.sentiment] = pd.to_numeric(df[cm.sentiment], errors="coerce")
        if df[cm.sentiment].notna().any():
            df[cm.sentiment] = df[cm.sentiment].fillna(float(df[cm.sentiment].median()))
        else:
            df[cm.sentiment] = df[cm.sentiment].fillna(0.0)

    df["review_length"] = df[cm.text].str.len().astype(int)

    # IQR outliers
    q1 = float(df["review_length"].quantile(0.25))
    q3 = float(df["review_length"].quantile(0.75))
    iqr = q3 - q1
    lower = q1 - 1.5 * iqr
    upper = q3 + 1.5 * iqr
    df["is_length_outlier"] = (df["review_length"] < lower) | (
        df["review_length"] > upper
    )

    after = int(len(df))
    na_cells_after = int(df.isna().sum().sum())

    summary = {
        "rows_before": before,
        "rows_after": after,
        "rows_dropped": before - after,
        "na_cells_before": na_cells_before,
        "na_cells_after": na_cells_after,
        "length_outliers": int(df["is_length_outlier"].sum()),
    }
    bounds = {
        "len_q1": q1,
        "len_q3": q3,
        "len_iqr": iqr,
        "len_lower": float(lower),
        "len_upper": float(upper),
    }
    return df, summary, bounds


# -----------------------------
# Metrics + Insights (Step 3)
# -----------------------------
def pearson_corr(a: pd.Series, b: pd.Series) -> Optional[float]:
    x = pd.to_numeric(a, errors="coerce")
    y = pd.to_numeric(b, errors="coerce")
    tmp = pd.concat([x, y], axis=1).dropna()
    if len(tmp) < 2:
        return None
    if tmp.iloc[:, 0].nunique() < 2 or tmp.iloc[:, 1].nunique() < 2:
        return None
    return float(tmp.corr(method="pearson").iloc[0, 1])


def corr_label(r: Optional[float]) -> str:
    if r is None:
        return "계산 불가"
    ar = abs(r)
    if ar >= 0.5:
        return "강함"
    if ar >= 0.3:
        return "중간"
    if ar >= 0.1:
        return "약함"
    return "거의 없음"


def compute_metrics(df: pd.DataFrame, cm: ColMap) -> Dict[str, object]:
    metrics: Dict[str, object] = {}

    metrics["corr_length_rating"] = pearson_corr(df["review_length"], df[cm.rating])
    if cm.sentiment is not None:
        metrics["corr_sentiment_rating"] = pearson_corr(df[cm.sentiment], df[cm.rating])
        metrics["corr_length_sentiment"] = pearson_corr(
            df["review_length"], df[cm.sentiment]
        )
    else:
        metrics["corr_sentiment_rating"] = None
        metrics["corr_length_sentiment"] = None

    # category stats
    cat_rating = (
        df.groupby(cm.category, dropna=False)[cm.rating]
        .mean()
        .sort_values(ascending=False)
    )
    metrics["category_avg_rating"] = cat_rating

    if cm.sentiment is not None:
        cat_sent = (
            df.groupby(cm.category, dropna=False)[cm.sentiment]
            .mean()
            .sort_values(ascending=False)
        )
        metrics["category_avg_sentiment"] = cat_sent
        if len(cat_sent) >= 1:
            metrics["category_sent_gap"] = float(cat_sent.iloc[0] - cat_sent.iloc[-1])
            metrics["category_sent_top"] = (
                str(cat_sent.index[0]),
                float(cat_sent.iloc[0]),
            )
            metrics["category_sent_bottom"] = (
                str(cat_sent.index[-1]),
                float(cat_sent.iloc[-1]),
            )
        else:
            metrics["category_sent_gap"] = None
            metrics["category_sent_top"] = None
            metrics["category_sent_bottom"] = None
    else:
        metrics["category_avg_sentiment"] = None
        metrics["category_sent_gap"] = None
        metrics["category_sent_top"] = None
        metrics["category_sent_bottom"] = None

    # mean length by rating (rounded 1~5)
    tmp = df.copy()
    tmp["rating_int"] = tmp[cm.rating].round().astype(int).clip(1, 5)
    metrics["mean_length_by_rating"] = (
        tmp.groupby("rating_int")["review_length"].mean().reindex([1, 2, 3, 4, 5])
    )

    return metrics


def build_insights(metrics: Dict[str, object], cm: ColMap) -> Dict[str, str]:
    r_sr = metrics.get("corr_sentiment_rating")
    r_lr = metrics.get("corr_length_rating")
    r_ls = metrics.get("corr_length_sentiment")
    gap = metrics.get("category_sent_gap")
    top = metrics.get("category_sent_top")
    bot = metrics.get("category_sent_bottom")

    # Q1
    if cm.sentiment is None or r_sr is None:
        q1 = (
            "- 결론: sentiment_score 컬럼이 없거나(또는 값이 단조/결측) 상관 분석이 어렵습니다.\n"
            "- 해석: 다음 중 하나를 확인하세요: sentiment_score 존재 여부, 값 분포(거의 한 값이면 상관이 의미 없음)."
        )
    else:
        direction = "양(+)의" if r_sr > 0 else "음(-)의"
        q1 = (
            f"- 결론: sentiment_score와 평점은 {direction} 관계 경향이 있습니다.\n"
            f"- 지표: Pearson r={r_sr:.3f} ({corr_label(r_sr)}).\n"
            "- 그래프 해석: 회귀선 주변으로 점들이 모이면 관계가 더 뚜렷합니다."
        )

    # Q2
    # 과제에서 중요한 포인트: 길이는 직접 평점/감성과 상관 약할 수 있으나 임베딩/입력정책 영향 가능
    lr_txt = "계산 불가" if r_lr is None else f"r={r_lr:.3f} ({corr_label(r_lr)})"
    ls_txt = "계산 불가" if r_ls is None else f"r={r_ls:.3f} ({corr_label(r_ls)})"
    q2 = (
        "- 결론: 리뷰 길이(review_length) 자체가 평점/감성과 강하게 연결되진 않을 가능성이 큽니다.\n"
        f"- 지표(길이↔평점): {lr_txt}\n"
        f"- 지표(길이↔감성): {ls_txt}\n"
        "- AI/임베딩 관점: 길이 차이로 인해 truncation(잘림), 요약/전처리 정책 차이가 생기면 "
        "내용이 비슷해도 임베딩이 달라지거나, 반대로 다른 문장도 비슷하게 뭉개질 수 있습니다."
    )

    # Q3
    if gap is None or top is None or bot is None:
        q3 = (
            "- 결론: sentiment_score가 없어 카테고리별 감성 차이를 평가할 수 없습니다.\n"
            "- 해석: sentiment_score 컬럼을 추가하거나(모델 산출/룰 기반), 다른 대체 지표를 사용하세요."
        )
    else:
        top_cat, top_val = top
        bot_cat, bot_val = bot
        q3 = (
            "- 결론: 카테고리별 평균 감성 점수 차이는 크지 않을 수 있습니다(데이터에 따라 다름).\n"
            f"- 지표: 최고({top_cat}={top_val:.3f}) - 최저({bot_cat}={bot_val:.3f}) = {gap:.3f}\n"
            "- 그래프 해석: 상위/하위 카테고리 간 막대 길이가 벌어질수록 차이가 큽니다."
        )

    # 3줄 요약
    if r_sr is None:
        line1 = "1) 감성↔평점: 계산 불가(컬럼 없음/값 단조/결측)."
    else:
        line1 = f"1) 감성↔평점: r={r_sr:.3f} ({corr_label(r_sr)}) → 감성 점수가 높을수록 평점도 대체로 높음."

    if r_lr is None:
        line2 = "2) 길이↔평점: 계산 불가(결측/단조)."
    else:
        line2 = f"2) 길이↔평점: r={r_lr:.3f} ({corr_label(r_lr)}) → 길이만으로 평점이 결정되진 않음."

    if gap is None:
        line3 = "3) 카테고리별 감성 차이: 판단 불가(sentiment_score 없음)."
    else:
        line3 = f"3) 카테고리별 감성 차이: 최고-최저={gap:.3f} → 카테고리별 감성이 크게 갈리진 않음(또는 제한적)."

    return {"q1": q1, "q2": q2, "q3": q3, "summary3": "\n".join([line1, line2, line3])}


# -----------------------------
# Plots (Step 1~2)
# -----------------------------
def save_fig(figdir: Path, filename: str) -> str:
    figdir.mkdir(parents=True, exist_ok=True)
    path = figdir / filename
    plt.tight_layout()
    plt.savefig(path, dpi=160, bbox_inches="tight")
    plt.close()
    return f"figures/{filename}"


def make_plots(
    df: pd.DataFrame,
    cm: ColMap,
    figdir: Path,
    topn_categories: int,
) -> Dict[str, str]:
    """
    returns: {key: relative_path_for_md}
    """
    out: Dict[str, str] = {}

    # 1) Rating distribution
    plt.figure(figsize=(6.2, 3.2))
    sns.histplot(df[cm.rating].dropna(), bins=10, kde=True)
    plt.title("평점 분포 (Rating Distribution)")
    plt.xlabel("Rating")
    plt.ylabel("Count")
    out["rating_dist"] = save_fig(figdir, "01_rating_distribution.png")

    # 2) Review length distribution
    plt.figure(figsize=(6.2, 3.2))
    sns.histplot(df["review_length"].dropna(), bins=30, kde=True)
    plt.title("리뷰 길이 분포 (Review Length Distribution)")
    plt.xlabel("Review length (chars)")
    plt.ylabel("Count")
    out["length_dist"] = save_fig(figdir, "02_review_length_distribution.png")

    # 3) Length boxplot (outlier view)
    plt.figure(figsize=(6.2, 2.9))
    sns.boxplot(x=df["review_length"].dropna())
    plt.title("리뷰 길이 이상치 탐지 (Boxplot)")
    plt.xlabel("Review length (chars)")
    out["length_box"] = save_fig(figdir, "03_review_length_boxplot.png")

    # 4) Category avg rating barh
    cat_rating = (
        df.groupby(cm.category, dropna=False)[cm.rating]
        .mean()
        .sort_values(ascending=False)
        .head(topn_categories)
        .reset_index(name="avg_rating")
    )
    plt.figure(figsize=(7.4, max(3.8, 0.28 * len(cat_rating))))
    sns.barplot(data=cat_rating, y=cm.category, x="avg_rating")
    plt.title(f"카테고리별 평균 평점 (Top {len(cat_rating)})")
    plt.xlabel("Average rating")
    plt.ylabel("Category")
    out["cat_avg_rating"] = save_fig(figdir, "04_category_avg_rating.png")

    # 5) Category avg sentiment (if exists)
    if cm.sentiment is not None:
        cat_sent = (
            df.groupby(cm.category, dropna=False)[cm.sentiment]
            .mean()
            .sort_values(ascending=False)
            .head(topn_categories)
            .reset_index(name="avg_sentiment")
        )
        plt.figure(figsize=(7.4, max(3.8, 0.28 * len(cat_sent))))
        sns.barplot(data=cat_sent, y=cm.category, x="avg_sentiment")
        plt.title(f"카테고리별 평균 감성 점수 (Top {len(cat_sent)})")
        plt.xlabel("Average sentiment")
        plt.ylabel("Category")
        out["cat_avg_sentiment"] = save_fig(figdir, "05_category_avg_sentiment.png")

        # 6) Sentiment vs rating (scatter + fit)
        plt.figure(figsize=(6.2, 4.2))
        sns.regplot(data=df, x=cm.sentiment, y=cm.rating, scatter_kws={"alpha": 0.35})
        plt.title("감성 점수 vs 평점 (Sentiment vs Rating)")
        plt.xlabel("Sentiment score")
        plt.ylabel("Rating")
        out["sent_vs_rating"] = save_fig(figdir, "06_sentiment_vs_rating.png")

    # 7) Length by rating violin
    tmp = df.copy()
    tmp["rating_int"] = tmp[cm.rating].round().astype(int).clip(1, 5)
    plt.figure(figsize=(6.6, 4.2))
    sns.violinplot(
        data=tmp,
        x="rating_int",
        y="review_length",
        inner="quartile",
        order=[1, 2, 3, 4, 5],
    )
    plt.title("평점별 리뷰 길이 분포 (Review Length by Rating)")
    plt.xlabel("Rating (rounded)")
    plt.ylabel("Review length (chars)")
    out["len_by_rating"] = save_fig(figdir, "07_length_by_rating_violin.png")

    return out


# -----------------------------
# Report (Step 4)
# -----------------------------
def md_img(rel_path: str, width_pct: int = 70) -> str:
    # GitHub/Notion/Markdown 뷰어에서 잘 보이도록 간단 HTML img 사용
    return f'<img src="{rel_path}" width="{width_pct}%">'


def write_report(
    outdir: Path,
    author_line: str,
    input_path: Path,
    cm: ColMap,
    prep_summary: Dict[str, int],
    bounds: Dict[str, float],
    metrics: Dict[str, object],
    insights: Dict[str, str],
    fig_rel: Dict[str, str],
    df: pd.DataFrame,
) -> Path:
    report_path = outdir / "EDA_Report.md"

    # Tables (최소 필요만)
    overall = (
        df[[cm.rating, "review_length"] + ([cm.sentiment] if cm.sentiment else [])]
        .describe()
        .T.round(4)
    )
    overall_md = (
        overall.reset_index()
        .rename(columns={"index": "metric"})
        .to_markdown(index=False)
    )

    cat_agg = (
        df.groupby(cm.category, dropna=False)
        .agg(
            count=(cm.rating, "count"),
            avg_rating=(cm.rating, "mean"),
            std_rating=(cm.rating, "std"),
            avg_length=("review_length", "mean"),
            std_length=("review_length", "std"),
        )
        .reset_index()
        .sort_values("avg_rating", ascending=False)
        .round(4)
        .head(20)
    )
    if cm.sentiment is not None:
        cat_agg["avg_sentiment"] = (
            df.groupby(cm.category, dropna=False)[cm.sentiment]
            .mean()
            .reindex(cat_agg[cm.category])
            .values
        )
        cat_agg["avg_sentiment"] = pd.to_numeric(
            cat_agg["avg_sentiment"], errors="coerce"
        ).round(4)

    cat_agg_md = cat_agg.to_markdown(index=False)

    # quick numbers
    r_sr = metrics.get("corr_sentiment_rating")
    r_lr = metrics.get("corr_length_rating")
    r_ls = metrics.get("corr_length_sentiment")
    gap = metrics.get("category_sent_gap")

    # rating range
    rating_min = float(df[cm.rating].min())
    rating_max = float(df[cm.rating].max())
    rating_mean = float(df[cm.rating].mean())
    rating_std = float(df[cm.rating].std())

    # length range
    len_mean = float(df["review_length"].mean())
    len_std = float(df["review_length"].std())

    # Build markdown (첫 페이지 + 목차)
    md: List[str] = []

    md.append("# EDA Report: Reviews Dataset\n")
    md.append(f"**작성자:** {author_line}\n")
    md.append(f"**입력 파일:** `{input_path.name}`\n")
    md.append("\n---\n")

    md.append("## 목차\n")
    md.append("1. 데이터 개요 및 컬럼 매핑\n")
    md.append("2. 전처리 요약 (결측/이상치)\n")
    md.append("3. 기술 통계 요약\n")
    md.append("4. 시각화 결과\n")
    md.append("5. AI 분석 관점 인사이트 (Q1~Q3) + 3줄 요약\n")
    md.append("6. 재현 방법 (Reproducibility)\n")
    md.append("\n---\n")

    # 1) Overview
    md.append("## 1. 데이터 개요 및 컬럼 매핑\n")
    md.append(
        "- 본 리포트는 `reviews_1000.csv` 리뷰 데이터를 기반으로 EDA를 수행합니다.\n"
    )
    md.append("### 컬럼 매핑 (자동 추론 결과)\n")
    md.append(f"- text: `{cm.text}`\n")
    md.append(f"- rating: `{cm.rating}`\n")
    md.append(f"- category: `{cm.category}`\n")
    md.append(f"- sentiment_score: `{cm.sentiment}`\n")
    md.append("\n")

    # 2) Preprocess
    md.append("## 2. 전처리 요약 (결측/이상치)\n")
    md.append(
        f"- rows_before → rows_after: **{prep_summary['rows_before']} → {prep_summary['rows_after']}**\n"
    )
    md.append(
        f"- rows_dropped(필수값 결측/빈텍스트/빈카테고리 제거): **{prep_summary['rows_dropped']}**\n"
    )
    md.append(
        f"- NA cells before → after: {prep_summary['na_cells_before']} → {prep_summary['na_cells_after']}\n"
    )
    md.append(
        f"- review_length IQR 이상치: **{prep_summary['length_outliers']}** "
        f"(lower={bounds['len_lower']:.1f}, upper={bounds['len_upper']:.1f})\n"
    )
    md.append("\n")

    # 3) Technical stats
    md.append("## 3. 기술 통계 요약\n")
    md.append(f"- 평점 범위: {rating_min:.1f} ~ {rating_max:.1f}\n")
    md.append(f"- 평점 평균±표준편차: {rating_mean:.3f} ± {rating_std:.3f}\n")
    md.append(f"- 리뷰 길이 평균±표준편차: {len_mean:.1f} ± {len_std:.1f} (chars)\n")
    md.append("\n### 3.1 Overall describe\n")
    md.append(overall_md + "\n")
    md.append("\n### 3.2 Category aggregates (Top 20)\n")
    md.append(cat_agg_md + "\n")

    # 4) Visualizations
    md.append("\n## 4. 시각화 결과\n")
    md.append("### 4.1 평점 분포\n")
    md.append(md_img(fig_rel["rating_dist"], 70) + "\n")
    md.append(
        "- 해석: 특정 평점 구간에 몰림이 크면(예: 4~5점 집중) 만족도 편향 가능성이 있습니다.\n\n"
    )

    md.append("### 4.2 리뷰 길이 분포 & 이상치\n")
    md.append(md_img(fig_rel["length_dist"], 70) + "\n\n")
    md.append(md_img(fig_rel["length_box"], 62) + "\n")
    md.append(
        "- 해석: 긴 꼬리/극단값이 많으면 전처리(자르기/요약) 정책이 모델 입력에 영향을 줄 수 있습니다.\n\n"
    )

    md.append("### 4.3 카테고리별 평균 평점\n")
    md.append(md_img(fig_rel["cat_avg_rating"], 80) + "\n")
    md.append(
        "- 해석: 카테고리별 평균 평점의 차이가 크면, 카테고리 자체가 만족도에 영향을 줄 수 있습니다.\n\n"
    )

    if cm.sentiment is not None and "cat_avg_sentiment" in fig_rel:
        md.append("### 4.4 카테고리별 평균 감성 점수\n")
        md.append(md_img(fig_rel["cat_avg_sentiment"], 80) + "\n")
        md.append(
            "- 해석: 감성 점수가 특정 카테고리에 치우치면 도메인 편향 가능성을 의심할 수 있습니다.\n\n"
        )

    if cm.sentiment is not None and "sent_vs_rating" in fig_rel:
        md.append("### 4.5 감성 점수 vs 평점\n")
        md.append(md_img(fig_rel["sent_vs_rating"], 62) + "\n")
        md.append(
            "- 해석: 회귀선과 점들의 분산을 함께 보며 선형 관계의 강도를 확인합니다.\n\n"
        )

    md.append("### 4.6 평점별 리뷰 길이 분포 (Violin)\n")
    md.append(md_img(fig_rel["len_by_rating"], 70) + "\n")
    md.append(
        "- 해석: 평점에 따라 리뷰 길이가 체계적으로 달라진다면, 길이로 인한 입력 편향 가능성이 있습니다.\n"
    )

    # 5) Insights
    md.append("\n## 5. AI 분석 관점 인사이트 (Q1~Q3) + 3줄 요약\n")
    md.append("### Q1) sentiment_score가 높을수록 평점이 높나?\n")
    md.append(
        f"- 지표: sentiment_score↔rating Pearson = `{None if r_sr is None else f'{r_sr:.6f}'}`\n"
    )
    md.append(insights["q1"] + "\n\n")

    md.append("### Q2) review_length가 AI 임베딩 유사도에 영향을 줄 수 있나?\n")
    md.append(
        f"- 지표: review_length↔rating Pearson = `{None if r_lr is None else f'{r_lr:.6f}'}`\n"
    )
    md.append(
        f"- 지표: review_length↔sentiment Pearson = `{None if r_ls is None else f'{r_ls:.6f}'}`\n"
    )
    md.append(insights["q2"] + "\n\n")

    md.append("### Q3) category 별 감성 점수 평균 차이는 존재?\n")
    md.append(
        f"- 지표: category sentiment gap(max-min) = `{None if gap is None else f'{gap:.6f}'}`\n"
    )
    md.append(insights["q3"] + "\n\n")

    md.append("### 3줄 요약\n")
    md.append(insights["summary3"] + "\n")

    # 6) Reproducibility
    md.append("\n## 6. 재현 방법 (Reproducibility)\n")
    md.append(
        "아래 명령으로 동일한 산출물(리포트/그림/전처리 CSV)을 재현할 수 있습니다.\n\n"
    )
    md.append("```bash\n")
    md.append(
        f'python eda_report.py --input "{input_path.name}" --outdir "{outdir.name}" --encoding utf-8 --topn_categories 30\n'
    )
    md.append("```\n\n")
    md.append("### 분석 파이프라인 요약\n")
    md.append(
        "- 전처리: 결측 제거(필수값), 타입 변환, review_length 생성, IQR 기반 길이 이상치 플래그\n"
    )
    md.append(
        "- 시각화: 평점 분포, 길이 분포, 길이 boxplot, 카테고리 평균 평점, (옵션) 카테고리 감성, (옵션) 감성↔평점, 평점별 길이 violin\n"
    )
    md.append("- 인사이트: 상관계수/카테고리 격차 + 그래프 기반 해석 + 3줄 요약\n")

    report_path.write_text("\n".join(md), encoding="utf-8")
    return report_path


# -----------------------------
# Main
# -----------------------------
def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--input", type=str, default="reviews_1000.csv", help="Input CSV path"
    )
    parser.add_argument(
        "--outdir", type=str, default="eda_report", help="Output directory"
    )
    parser.add_argument(
        "--encoding",
        type=str,
        default="utf-8",
        help="CSV encoding (utf-8, utf-8-sig, cp949...)",
    )
    parser.add_argument(
        "--topn_categories", type=int, default=30, help="Top-N categories to plot"
    )
    args = parser.parse_args()

    setup_korean_font()

    input_path = Path(args.input).expanduser().resolve()
    outdir = Path(args.outdir).expanduser().resolve()
    figdir = outdir / "figures"
    outdir.mkdir(parents=True, exist_ok=True)
    figdir.mkdir(parents=True, exist_ok=True)

    df_raw = read_csv_safe(input_path, encoding=args.encoding)
    cm = infer_columns(df_raw)

    df, prep_summary, bounds = preprocess(df_raw, cm)

    # save processed csv
    processed_path = outdir / "processed_reviews.csv"
    df.to_csv(processed_path, index=False, encoding="utf-8-sig")

    # plots
    fig_rel = make_plots(df, cm, figdir, topn_categories=args.topn_categories)

    # metrics + insights
    metrics = compute_metrics(df, cm)
    insights = build_insights(metrics, cm)

    # report
    report_path = write_report(
        outdir=outdir,
        author_line="Cloud 1기 백강민",
        input_path=input_path,
        cm=cm,
        prep_summary=prep_summary,
        bounds=bounds,
        metrics=metrics,
        insights=insights,
        fig_rel=fig_rel,
        df=df,
    )

    print(f"[OK] Report: {report_path}")
    print(f"[OK] Figures: {figdir}")
    print(f"[OK] Processed CSV: {processed_path}")


if __name__ == "__main__":
    main()
