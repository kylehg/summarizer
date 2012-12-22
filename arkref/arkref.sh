#!/usr/bin/env zsh
h=$(dirname $0)
java -mx1g -ea -cp $h/bin:$h/arkref.jar:$(print $h/lib/**/*.jar | tr ' ' :) arkref.analysis.ARKref "$@"
