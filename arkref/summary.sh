#!/bin/bash
# e.g. ./summary.sh -input data/*.sent
./arkref.sh -debug "$@" 2>&1 | awk '/^\*\*\* +Input/{print "";print;x=0}  /Entity Report/ {x=1}  x{print}'
