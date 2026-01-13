# -------------------------------------------------------------
# 작성자 : 백강민
# 작성목적 : SKALA Python Day2 - OOP 기반 AI 추천 주문 시스템 설계
# 작성일 : 2025-01-13
# 변경사항 내역 :
#   2025-01-13 - 최초 작성
# -------------------------------------------------------------
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Dict, Iterable, List, Optional, Set, Tuple
from collections import Counter
from datetime import datetime


# ---------- Domain Models ----------


@dataclass(frozen=True, slots=True)
class MenuItem:
    name: str
    price: int

    @property
    def price_won(self) -> str:
        return f"{self.price:,}원"


@dataclass(frozen=True, slots=True)
class Beverage(MenuItem):
    tags: Tuple[str, ...] = field(default_factory=tuple)

    def __post_init__(self) -> None:
        normalized = tuple(t.strip() for t in self.tags if str(t).strip())
        object.__setattr__(self, "tags", normalized)

    @property
    def tag_set(self) -> Set[str]:
        return set(self.tags)


@dataclass(frozen=True, slots=True)
class OrderItem:
    beverage: Beverage
    qty: int = 1

    @property
    def line_total(self) -> int:
        return self.beverage.price * self.qty


@dataclass(frozen=True, slots=True)
class Order:
    customer_name: str
    items: Tuple[OrderItem, ...]
    created_at: datetime = field(default_factory=datetime.now)

    @property
    def total(self) -> int:
        return sum(it.line_total for it in self.items)


class Customer:
    """Customer + order history + preference signals"""

    def __init__(self, name: str, style_tags: Optional[Iterable[str]] = None):
        self._name = name.strip()
        if not self._name:
            raise ValueError("Customer name must not be empty.")

        # 사용자가 주문이 없을 때를 대비한 "스타일 태그"
        self._style_tags: Tuple[str, ...] = tuple(
            t.strip() for t in (style_tags or []) if str(t).strip()
        )

        self._orders: List[Order] = []

    @property
    def name(self) -> str:
        return self._name

    @property
    def style_tags(self) -> Tuple[str, ...]:
        return self._style_tags

    @property
    def orders(self) -> List[Order]:
        return list(self._orders)

    @property
    def last_order(self) -> Optional[Order]:
        return self._orders[-1] if self._orders else None

    def add_order(self, order: Order) -> None:
        self._orders.append(order)

    @property
    def total_spent(self) -> int:
        return sum(o.total for o in self._orders)

    @property
    def average_spent(self) -> float:
        if not self._orders:
            return 0.0
        return self.total_spent / len(self._orders)

    @property
    def ordered_beverage_names(self) -> Set[str]:
        names: Set[str] = set()
        for o in self._orders:
            for it in o.items:
                names.add(it.beverage.name)
        return names

    @property
    def tag_stats(self) -> Counter[str]:
        c = Counter()
        for o in self._orders:
            for it in o.items:
                c.update(it.beverage.tags)
        return c

    def recent_tags(self, n_orders: int = 1) -> Set[str]:
        """최근 n개 주문에서 태그 집합"""
        if n_orders <= 0 or not self._orders:
            return set()
        recent = self._orders[-n_orders:]
        tags: Set[str] = set()
        for o in recent:
            for it in o.items:
                tags |= it.beverage.tag_set
        return tags


# ---------- Catalog / Repositories ----------


class Catalog:
    def __init__(self, beverages: Iterable[Beverage]):
        self._items: Dict[str, Beverage] = {b.name: b for b in beverages}

    def get(self, name: str) -> Beverage:
        if name not in self._items:
            raise KeyError(f"메뉴에 없는 음료입니다: {name}")
        return self._items[name]

    def all(self) -> List[Beverage]:
        return list(self._items.values())


class CustomerStore:
    def __init__(self):
        self._customers: Dict[str, Customer] = {}

    def add(self, customer: Customer) -> None:
        self._customers[customer.name] = customer

    def get(self, name: str) -> Customer:
        if name not in self._customers:
            raise KeyError(f"등록되지 않은 사용자입니다: {name}")
        return self._customers[name]

    def all(self) -> List[Customer]:
        return list(self._customers.values())


# ---------- Services ----------


class OrderService:
    """주문 생성/적재 담당"""

    def __init__(self, catalog: Catalog, customers: CustomerStore):
        self._catalog = catalog
        self._customers = customers

    def place_order(self, customer_name: str, beverage_names: List[str]) -> Order:
        customer = self._customers.get(customer_name)

        items: List[OrderItem] = []
        for name in beverage_names:
            bev = self._catalog.get(name)
            items.append(OrderItem(beverage=bev, qty=1))

        order = Order(customer_name=customer.name, items=tuple(items))
        customer.add_order(order)
        return order


class RecommendationService:
    """
    사용자 스타일 기반 3개 추천
    - 최근 주문 태그 (강한 신호)
    - 누적 태그(tag_stats) (취향)
    - 평균 주문가 근접도 (가격대)
    - 이미 주문한 음료 제외
    """

    def __init__(self, catalog: Catalog):
        self._catalog = catalog

    def recommend(
        self, customer: Customer, top_k: int = 3, recent_n: int = 2
    ) -> List[Beverage]:
        if top_k <= 0:
            return []

        menu = self._catalog.all()
        ordered = customer.ordered_beverage_names

        # 신호들 준비
        recent_tags = customer.recent_tags(n_orders=recent_n)
        stats = customer.tag_stats
        avg_price = customer.average_spent

        # 주문이 하나도 없으면: style_tags를 기반으로 추천
        if not customer.orders and customer.style_tags:
            recent_tags = set(customer.style_tags)

        # 정말 아무 신호도 없다면: 싼 것 3개
        if not recent_tags and not stats:
            return sorted(menu, key=lambda b: (b.price, b.name))[:top_k]

        scored: List[Tuple[float, Beverage]] = []
        for b in menu:
            if b.name in ordered:
                continue

            # 1) 최근 태그 겹침 (가중치 크게)
            overlap_recent = len(b.tag_set & recent_tags)
            score_recent = overlap_recent * 3.0

            # 2) 누적 취향(태그 빈도)
            score_pref = 0.0
            for t in b.tags:
                score_pref += (
                    float(stats.get(t, 0)) * 0.7
                )  # 취향 태그가 자주 등장할수록 +

            # 3) 가격대 유사도 (평균 주문가가 있으면)
            score_price = 0.0
            if avg_price > 0:
                # 가격 차이가 작을수록 점수 +
                diff = abs(b.price - avg_price)
                score_price = max(0.0, 1500.0 - diff) / 1500.0  # 0~1 정규화 비슷한 느낌

            # 최소한 “태그 기반”으로 의미 있는 후보만
            if score_recent == 0.0 and score_pref == 0.0 and (recent_tags or stats):
                continue

            total = score_recent + score_pref + score_price
            scored.append((total, b))

        # 점수 높은 순, 동일 점수면 "태그 많은 것" -> "가격 낮은 것" -> "이름"
        scored.sort(
            key=lambda x: (x[0], len(x[1].tags), -x[1].price, x[1].name), reverse=True
        )
        return [b for _, b in scored[:top_k]]


# ---------- Presentation (Demo) ----------


def format_beverage(b: Beverage) -> str:
    return f"{b.name} ({b.price_won}) tags={list(b.tags)}"


def format_order(o: Order) -> str:
    names = [it.beverage.name for it in o.items]
    return f"{o.customer_name} 주문: {names} / 합계: {o.total:,}원"


def build_menu() -> List[Beverage]:
    # 추천이 돋보이도록 태그 조합 다양화 + 메뉴 확장
    return [
        # 기본
        Beverage("아이스 아메리카노", 1900, ("커피", "콜드")),
        Beverage("아메리카노", 1900, ("커피", "뜨거운")),
        Beverage("카페라떼", 2000, ("커피", "밀크")),
        Beverage("아이스 라떼", 3200, ("커피", "밀크", "콜드")),
        Beverage("그린티", 2800, ("차", "뜨거운")),
        Beverage("아이스 그린티", 3000, ("차", "차가운")),
        Beverage("밀크티", 3000, ("차", "차가운", "밀크")),
        Beverage("쌍화차", 2300, ("차", "뜨거운", "전통")),
        # 커피 변주 (시럽/초코/샷/디카페인/콜드브루)
        Beverage("에스프레소", 1800, ("커피", "샷", "뜨거운")),
        Beverage("더블샷 에스프레소", 2300, ("커피", "샷", "뜨거운")),
        Beverage("바닐라 라떼", 3200, ("커피", "밀크", "시럽")),
        Beverage("아이스 바닐라 라떼", 3500, ("커피", "밀크", "콜드", "시럽")),
        Beverage("카라멜 마키아또", 3500, ("커피", "밀크", "시럽")),
        Beverage("카페모카", 3600, ("커피", "밀크", "초코")),
        Beverage("디카페인 아메리카노", 2400, ("커피", "디카페인", "콜드")),
        Beverage("디카페인 라떼", 2900, ("커피", "디카페인", "밀크")),
        Beverage("콜드브루", 3300, ("커피", "콜드", "콜드브루")),
        Beverage("콜드브루 라떼", 3800, ("커피", "콜드", "콜드브루", "밀크")),
        # 말차/곡물 (네가 올린 라인 유지 + 확장)
        Beverage("말차라떼", 3500, ("커피", "밀크", "말차")),
        Beverage("오곡라떼", 3800, ("커피", "밀크", "곡물")),
        Beverage("흑임자라떼", 4200, ("밀크", "전통", "뜨거운")),
        Beverage("고구마라떼", 3800, ("밀크", "전통", "뜨거운")),
        # 티/허브/과일티
        Beverage("얼그레이", 2800, ("차", "뜨거운")),
        Beverage("아이스 얼그레이", 3000, ("차", "차가운")),
        Beverage("캐모마일", 2900, ("차", "허브", "뜨거운")),
        Beverage("페퍼민트", 2900, ("차", "허브", "뜨거운")),
        Beverage("유자차", 3200, ("차", "뜨거운", "과일")),
        Beverage("아이스 유자티", 3400, ("차", "차가운", "과일")),
        Beverage("생강차", 3300, ("차", "뜨거운", "스파이스")),
        Beverage("대추차", 3300, ("차", "뜨거운", "전통")),
        # 논커피/초코
        Beverage("초코라떼", 3500, ("밀크", "초코", "뜨거운")),
        Beverage("아이스 초코", 3700, ("밀크", "초코", "콜드")),
        # 에이드/탄산
        Beverage("레몬에이드", 4200, ("에이드", "탄산", "과일", "콜드")),
        Beverage("자몽에이드", 4500, ("에이드", "탄산", "과일", "콜드")),
        Beverage("청포도에이드", 4500, ("에이드", "탄산", "과일", "콜드")),
        # 스무디/프라페
        Beverage("딸기 스무디", 4800, ("스무디", "과일", "콜드")),
        Beverage("망고 스무디", 5000, ("스무디", "과일", "콜드")),
        Beverage("초코 프라페", 5200, ("프라페", "초코", "콜드")),
        Beverage("커피 프라페", 5200, ("프라페", "커피", "콜드")),
    ]


def main() -> None:
    catalog = Catalog(build_menu())
    customers = CustomerStore()

    # 주문이 없는 사용자도 "스타일"로 추천이 되게 태그를 부여
    customers.add(Customer("백강민", style_tags=("커피", "콜드")))
    customers.add(Customer("손흥민", style_tags=("차", "뜨거운")))
    customers.add(Customer("BTS", style_tags=("밀크", "콜드")))
    customers.add(Customer("페이커", style_tags=("커피", "샷", "뜨거운")))

    order_service = OrderService(catalog, customers)
    recommender = RecommendationService(catalog)

    # ---- 실제 주문 시스템처럼: 주문 먼저 발생 ----
    order_service.place_order("백강민", ["아이스 아메리카노", "카페라떼"])
    order_service.place_order("손흥민", ["그린티"])
    order_service.place_order("BTS", ["밀크티", "오곡라떼"])
    # 페이커는 주문 없음

    # ---- 출력: 주문 요약 + 스타일 기반 3개 추천 ----
    for c in customers.all():
        print(f"\n=== 사용자: {c.name} ===")
        if c.last_order:
            print(f"- 최근 주문: {format_order(c.last_order)}")
        else:
            print("- 최근 주문: 없음")

        print(f"- 총 주문 금액: {c.total_spent:,}원")
        print(f"- 평균 주문 금액: {c.average_spent:,.1f}원")
        if c.style_tags:
            print(f"- 스타일 태그: {list(c.style_tags)}")

        recs = recommender.recommend(c, top_k=3, recent_n=2)
        print("- 추천(스타일 맞춤 3개):")
        if recs:
            for r in recs:
                print(f"  * {format_beverage(r)}")
        else:
            print("  * 추천 없음")


if __name__ == "__main__":
    main()
