#!/bin/bash
# 第6回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
echo "JDK: $(java -version 2>&1 | sed -n 2p)"
java src/Immutables.java > behaviors.txt
java src/CopyOfSame.java > copyof-same.txt
java -Xmx2g src/ListSize.java > list-size.csv
