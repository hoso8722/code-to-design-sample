#!/bin/bash
# 第1回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/*.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 起動から終了までの時間(各11回。1回目を除いた10回を startup.csv に)。時計の補正の影響を避けるため、単調な時計で計る
python3 - "$WORK" <<'PY' > startup-raw.csv
import subprocess, sys, time
for prog in ["Hello", "HttpHello"]:
    for i in range(1, 12):
        t0 = time.monotonic()
        subprocess.run(["java", "-cp", sys.argv[1], prog], stdout=subprocess.DEVNULL, check=True)
        print(f"{prog},{i},{round((time.monotonic() - t0) * 1000)}")
PY
{ echo "program,run,total_ms"; awk -F, '$2>1' startup-raw.csv; } > startup.csv

# 2. JVMを作り終えるまでの時間
java -Xlog:startuptime -cp "$WORK" Hello | grep "Create VM" > create-vm.txt

# 3. HttpHello の内訳(4回)
: > http-hello-timed.txt
for i in 1 2 3 4; do java -cp "$WORK" HttpHelloTimed >> http-hello-timed.txt; done

# 4. 読み込まれるクラスと、読み込み元
for prog in Hello HttpHello; do
  java -Xlog:class+load:file=class-load-$prog.log -cp "$WORK" $prog > /dev/null
done
echo "program,total,cds,jrt,app,generated" > class-load.csv
for prog in Hello HttpHello; do
  f=class-load-$prog.log
  total=$(grep -c "class,load" $f)
  cds=$(grep -c "source: shared objects file" $f)
  jrt=$(grep -c "source: jrt:/" $f)
  app=$(grep -c "source: file:" $f)
  echo "$prog,$total,$cds,$jrt,$app,$((total - cds - jrt - app))" >> class-load.csv
done

# 5. クラスの初期化の順番
java -cp "$WORK" InitOrder > init-order.txt
rm -rf "$WORK"
