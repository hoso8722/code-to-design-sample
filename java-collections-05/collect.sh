#!/bin/bash
# 第5回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
echo "JDK: $(java -version 2>&1 | sed -n 2p)"
{ for c in Contract MutableKey CompareVsEquals; do echo "== $c"; java src/$c.java; done; } > results.txt
for i in 1 2 3; do java src/Chance.java; done > chance.txt
