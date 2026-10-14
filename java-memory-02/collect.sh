#!/bin/bash
# 第2回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -g -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. -Xss と再帰の深さ(JITあり / -Xint で、各5回)
echo "xss_kb,mode,run,small,large" > stack-depth.csv
for kb in 256 512 1024 2048 4096; do
  for mode in int jit; do
    flag=""; [ "$mode" = int ] && flag="-Xint"
    for run in 1 2 3 4 5; do
      out=$(java -Xss${kb}k $flag -cp "$WORK" StackDepth)
      small=$(echo "$out" | sed -n 's/^small: //p'); large=$(echo "$out" | sed -n 's/^large: //p')
      echo "$kb,$mode,$run,$small,$large" >> stack-depth.csv
    done
  done
done

# 2. エスケープ解析と確保量
java -cp "$WORK" EscapeAnalysis > escape-on.csv
java -XX:-DoEscapeAnalysis -cp "$WORK" EscapeAnalysis > escape-off.csv
java -Xint -cp "$WORK" EscapeAnalysis > escape-xint.csv

# 3. スタックの余白の設定
java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -E 'Stack(Red|Yellow|Reserved|Shadow)Pages' | awk '{print $2, $3, $4}' > stack-pages.txt
java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -E ' ThreadStackSize ' | awk '{print $2, $3, $4}' >> stack-pages.txt
rm -rf "$WORK"
