#!/bin/bash
# 第2回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 区間ごとの平均(通常を3回。1回目を warmup-runs.txt に。-Xint を1回)
java -cp "$WORK" Warmup | grep -v check= > warmup-runs.txt
for i in 2 3; do java -cp "$WORK" Warmup | grep -v check= > "$WORK/warmup-run$i.txt"; done
java -Xint -cp "$WORK" Warmup | grep -v check= > warmup-xint.txt
python3 - "$WORK" <<'PY' > warmup-3runs.txt
import sys
runs = [open(p, encoding="utf-8").read().strip().split("\n")[1:] for p in ["warmup-runs.txt", sys.argv[1] + "/warmup-run2.txt", sys.argv[1] + "/warmup-run3.txt"]]
print("3回の実行の、区間ごとの平均(ns)")
for rows in zip(*runs):
    f = [r.split(",") for r in rows]
    print(f"{f[0][0]}-{f[0][1]}: " + ", ".join(x[2] for x in f))
PY

# 2. 1回ずつの時間(グラフ用)と、切り替わった回
java -cp "$WORK" WarmupTrace | grep -v check= > trace-jit.csv
java -Xint -cp "$WORK" WarmupTrace | grep -v check= > trace-xint.csv
python3 - <<'PY' > switch-points.txt
rows = [l.split(",") for l in open("trace-jit.csv", encoding="utf-8").read().strip().split("\n")[1:]]
first10 = next(int(c) for c, n in rows if int(n) < 10_000)
first1 = next(int(c) for c, n in rows if int(n) < 1_000)
print(f"最初に10µs未満になった回: {first10}")
print(f"最初に1µs未満になった回: {first1}")
PY

# 3. プログラム全体の時間(各1回)
python3 - "$WORK" <<'PY' > total-time.txt
import subprocess, sys, time
for flag, label in [([], ""), (["-Xint"], "-Xint")]:
    t0 = time.monotonic()
    subprocess.run(["java", *flag, "-cp", sys.argv[1], "Warmup"], stdout=subprocess.DEVNULL, check=True)
    print(f"{label} total {round((time.monotonic() - t0) * 1000)} ms")
PY

# 4. バイトコードと、しきい値の設定
javap -c -cp "$WORK" Warmup | sed -n '/static long sumOfSquares/,/^$/p' > javap-sumOfSquares.txt
java -XX:+PrintFlagsFinal -version 2>/dev/null | grep -E "Tier[34](BackEdge|Compile|Invocation|MinInvocation)Threshold|TieredCompilation |CICompilerCount " > tier-flags.txt
rm -rf "$WORK"
