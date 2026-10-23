#!/bin/bash
# 第5回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

java -cp "$WORK" Combine > combine.txt
java -cp "$WORK" WhichThread > which-thread.txt
# CPUを1つに制限したとき、supplyAsync がどのスレッドで動くか
{ echo "== jdk-$(java -XshowSettings:properties -version 2>&1 | sed -n 's/.*java.runtime.version = //p') (taskset -c 0)"; taskset -c 0 java -cp "$WORK" OneCpu; } > one-cpu.txt
rm -rf "$WORK"
