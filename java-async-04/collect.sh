#!/bin/bash
# 第4回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

java -cp "$WORK" GetOrder > get-order.csv
java -cp "$WORK" InvokeAny > invoke-any.txt
java -cp "$WORK" TimeoutAndCancel > timeout-cancel.txt
rm -rf "$WORK"
