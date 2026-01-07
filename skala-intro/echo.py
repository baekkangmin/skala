# echo.py
from password import validate_password

def prompt_password() -> None:
    while True:
        pw = input("비밀번호를 입력하세요 (최소 6자, 알파벳/숫자/특수문자 포함): ").strip()

        if validate_password(pw):
            print("비밀번호 OK. 에코를 시작합니다.")
            return
        else:
            print("비밀번호 FAIL: 최소 6자 + 알파벳/숫자/특수문자 각각 1개 이상 포함해야 합니다.\n")


def run_echo() -> None:
    while True:
        text = input("입력 (!exit 종료): ")

        if text == "!exit":
            print("프로그램을 종료합니다.")
            break

        print(text)


if __name__ == "__main__":
    prompt_password()
    run_echo()
