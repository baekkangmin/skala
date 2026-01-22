#!/bin/bash

VAR=10
if [ $VAR -eq 10 ]; then
    echo " 단일 bracket : -eq"
fi

if [[ $VAR -eq 10 ]]; then
    echo " 이중 bracket : -eq"
fi

if [[ $VAR == 10 ]]; then
    echo " 이중 bracket : =="
fi

