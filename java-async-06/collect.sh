#!/bin/bash
# 第6回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

java -cp "$WORK" AllOfFailure > allof.txt
java -cp "$WORK" Handlers > handlers.txt
java -cp "$WORK" Propagation > propagation.txt
java -cp "$WORK" Timeouts > timeouts.txt
rm -rf "$WORK"
