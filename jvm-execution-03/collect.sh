#!/bin/bash
# 第3回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. JITコンパイルの記録(1回)
java -XX:+PrintCompilation -cp "$WORK" Tiers | grep -v "^処理時間" > print-compilation.txt

# 2. モードごとの処理時間(各3回)
echo "mode,run,elapsed_ms" > modes.csv
for spec in "normal:" "c1:-XX:TieredStopAtLevel=1" "xint:-Xint"; do
  mode=${spec%%:*}; flag=${spec#*:}
  for i in 1 2 3; do
    ms=$(java $flag -cp "$WORK" Tiers | sed -n 's/処理時間: \([0-9]*\) ms.*/\1/p')
    echo "$mode,$i,$ms" >> modes.csv
  done
done

# 3. コードキャッシュ
java -XX:+PrintCodeCache -cp "$WORK" Tiers > codecache.txt
# 4. main のOSRコードが使われなくなった理由(JDK 21の PrintCompilation には、理由が付かないため、脱最適化のログで確かめる)
java -Xlog:deoptimization=debug -XX:+PrintCompilation -cp "$WORK" Tiers | grep -E "Tiers::main @ 56.*made not entrant|osr level=4 Tiers.main" > deopt-main.txt
rm -rf "$WORK"
