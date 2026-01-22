#!/bin/bash

read -p "계속할까요? (y/n): " ans

case $ans in
    y|Y|YES)
        echo "계속 진행합니다";;
    n|N|no|NO)
        echo "중지합니다" ;;
    *)
        echo "잘못된 입력입니다" ;;
esac
