#!/bin/bash
# 第5回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 1回ごとの時間(グラフ用の1回分と、300・301・302・600回目だけの3回分)
java -cp "$WORK" Deopt > trace.csv
: > spike-runs.txt
for i in 1 2 3; do
  java -cp "$WORK" Deopt | awk -F, '$1==300||$1==301||$1==302||$1==600 {printf "%s:%s ", $1, $4} END {print ""}' >> spike-runs.txt
done

# 2. コンパイルの記録と、脱最適化のログ(別の実行)。PrintCompilation は標準出力に混ざるので、-Xlog:jit+compilation でファイルに出す
java -Xlog:jit+compilation=debug:file="$WORK/jit.log" -Xlog:deoptimization=debug:file="$WORK/deopt.log" -cp "$WORK" Deopt > /dev/null
python3 - "$WORK/jit.log" <<'PY' > print-compilation-total.txt
import re, sys
for line in open(sys.argv[1], encoding="utf-8"):
    m = re.match(r"\[([\d.]+)s\]\[debug\]\[jit,compilation\]\s*(.*)", line)
    if m and "Deopt::total" in m.group(2):
        print(f"{round(float(m.group(1)) * 1000)}  {m.group(2)}")
PY
grep "Deopt.total" "$WORK/deopt.log" > deopt-log.txt

# 3. トラップの位置のバイトコード
javap -c -cp "$WORK" Deopt | sed -n '/static double total/,/^$/p' > javap-total.txt
rm -rf "$WORK"
