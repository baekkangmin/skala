#!/bin/bash

unset_var="var"

echo ${unset_var?Error}

printf "%s %s\n" bye script

echo ${unset_var:+define}
