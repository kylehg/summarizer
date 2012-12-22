#!/usr/bin/env zsh
set -eux
./build.sh
rm -rf build
rm -f demo/*.{sst,osent,parse,tagged}
svn status
rm -rf **/.svn
