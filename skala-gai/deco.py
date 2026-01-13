# -------------------------------------------------------------
# 작성자 : 백강민
# 작성목적 : SKALA Python Day2 - 데코레이터로 함수 실행시간 측정기 구현
# 작성일 : 2025-01-13
# 변경사항 내역 :
#   2025-01-13 - 최초 작성
# -------------------------------------------------------------

import time
from functools import wraps


def measure_time(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        result = func(*args, **kwargs)
        elapsed = time.perf_counter() - start
        print(f"{func.__name__} took {elapsed:.4f} seconds", flush=True)
        return result

    return wrapper


# 테스트용 함수
@measure_time
def slow_function():
    time.sleep(1.5)
    return "완료!"


# 실행
if __name__ == "__main__":
    result = slow_function()
    print("함수 결과:", result)
