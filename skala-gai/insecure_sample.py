import os
import pickle
import subprocess

def bad_examples(user_input: str):
    # 1) eval (위험)
    x = eval(user_input)

    # 2) exec (위험)
    exec("print('hello')")

    # 3) os.system (위험)
    os.system("echo HACKED")

    # 4) pickle.load (위험)
    with open("data.pkl", "rb") as f:
        obj = pickle.load(f)

    # 5) subprocess + shell=True (위험)
    subprocess.run("ls -al", shell=True)

    return x

if __name__ == "__main__":
    # 실제 실행용이 아니라 '검사용' 샘플
    pass
