./build.sh && grep '\$' README.md | perl -pe 's/\$//' |grep -Ev -- '-ace|servers' | bash -x -
