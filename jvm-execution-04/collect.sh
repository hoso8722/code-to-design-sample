#!/bin/bash
# 第4回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 実装クラスの種類(1〜4)ごとの、1回の呼び出しの時間(デフォルトと -XX:-Inline、各3回)
echo "option,kinds,run,ns_per_call" > morphism.csv
for spec in "default:" "noinline:-XX:-Inline"; do
  opt=${spec%%:*}; flag=${spec#*:}
  for kinds in 1 2 3 4; do
    for run in 1 2 3; do
      ns=$(java $flag -cp "$WORK" Morphism $kinds | sed -n "s/^$kinds,\(.*\)/\1/p")
      echo "$opt,$kinds,$run,$ns" >> morphism.csv
    done
  done
done

# 2. インライン展開の判断(種類 1〜3)。Morphism に関係する行だけを残す
for kinds in 1 2 3; do
  java -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining -cp "$WORK" Morphism $kinds | grep -E 'Morphism[$:]' > print-inlining-kinds$kinds.txt
done

# 3. メソッドの大きさの上限
java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -E " (FreqInlineSize|MaxInlineSize|MaxInlineLevel|InlineSmallCode) " > inline-flags.txt
: > freqinline5.txt
for run in 1 2 3; do
  java -XX:+UnlockDiagnosticVMOptions -XX:FreqInlineSize=5 -cp "$WORK" Morphism 1 >> freqinline5.txt
done
java -XX:+UnlockDiagnosticVMOptions -XX:FreqInlineSize=5 -XX:+PrintInlining -cp "$WORK" Morphism 1 | grep -E 'Morphism\$Square::area' | sort | uniq -c | sort -rn | head -3 > freqinline5-inlining.txt

# 4. インライン展開とエスケープ解析
{
  echo -n "[default] "; java -cp "$WORK" InlineAndEscape
  echo -n "[-XX:-Inline] "; java -XX:-Inline -cp "$WORK" InlineAndEscape
  echo -n "[-XX:-DoEscapeAnalysis] "; java -XX:-DoEscapeAnalysis -cp "$WORK" InlineAndEscape
} > inline-escape.txt
rm -rf "$WORK"
