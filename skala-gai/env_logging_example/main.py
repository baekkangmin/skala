# main.py
import os
import logging
from dotenv import load_dotenv


def setup_logging(log_level_str: str, log_file: str = "app.log") -> logging.Logger:
    """
    - 로그 레벨: .env에서 가져온 값 (DEBUG/INFO/ERROR...)
    - 로그 포맷: 시간 | 로그레벨 | 메시지
    - 로그 출력: 콘솔 + 파일
    """
    level = getattr(logging, (log_level_str or "INFO").upper(), logging.INFO)

    logger = logging.getLogger("app")
    logger.setLevel(level)
    logger.propagate = False  # root로 중복 출력 방지

    # 이미 핸들러가 있으면 중복 추가 방지 (노트북/재실행 대비)
    if logger.handlers:
        return logger

    fmt = logging.Formatter(
        "%(asctime)s | %(levelname)s | %(message)s", datefmt="%Y-%m-%d %H:%M:%S"
    )

    # 콘솔 핸들러
    console_handler = logging.StreamHandler()
    console_handler.setLevel(level)
    console_handler.setFormatter(fmt)

    # 파일 핸들러
    file_handler = logging.FileHandler(log_file, encoding="utf-8")
    file_handler.setLevel(level)
    file_handler.setFormatter(fmt)

    logger.addHandler(console_handler)
    logger.addHandler(file_handler)

    return logger


def main() -> None:
    load_dotenv()  # .env 로딩

    app_name = os.getenv("APP_NAME", "UnknownApp")
    log_level = os.getenv("LOG_LEVEL", "INFO")

    logger = setup_logging(log_level)

    # 요구된 로그 메시지 출력
    logger.info("앱 실행 시작")
    logger.debug("환경 변수 로딩 완료")

    # ERROR 예시: ZeroDivisionError 발생 시 출력
    try:
        _ = 1 / 0
    except ZeroDivisionError:
        logger.exception("예외 발생 예시")

    logger.info(f"APP_NAME={app_name}, LOG_LEVEL={log_level}")


if __name__ == "__main__":
    main()
