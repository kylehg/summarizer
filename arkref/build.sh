#!/usr/bin/env zsh

# quite obviously this is not a proper build system. but if it compiles
# correctly inside eclipse, it should work here.

# REV=$( (svn info; git svn info) | perl -ne 'print $1 if /^Revision: (\d+)/')
# if [[ "$REV" == "" ]]; then
#   exit -1
# fi
# TARGET=arkref-r${REV}.jar

TARGET=arkref.jar

set -eux

rm -rf build
mkdir -p build

javac -cp $(print $(dirname $0)/lib/**/*.jar | tr ' ' :) -d build src/**/*.java

(cd build && jar cf $TARGET arkref)

mv build/$TARGET arkref.jar
