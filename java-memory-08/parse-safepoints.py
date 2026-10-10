# safepoint のログ（-Xlog:safepoint）に記録された全イベントの停止時間を集計する。
# GC以外の safepoint operation も含むため、純粋なGC停止時間ではない。
# 停止時間 = Total − Reaching safepoint（すべてのスレッドが止まるまでの待ちは除く。
# この計測ではアプリケーションのスレッドは main の1本だけで、待ちの間も main は動いているため）
import glob, re, statistics, sys
prefix = sys.argv[1] if len(sys.argv) > 1 else "safepoint"
pat = re.compile(r'Safepoint "([A-Za-z0-9_]+)".*Reaching safepoint: (\d+) ns.* Total: (\d+) ns')
print("gc,run,safepoints,total_safepoint_ms,max_safepoint_ms,p50_safepoint_ms")
for path in sorted(glob.glob(f"logs/{prefix}-*.log")):
    m = re.match(rf"logs/{prefix}-(.+)-(\d+)\.log", path)
    ms = [(int(total) - int(reach)) / 1e6 for op, reach, total in pat.findall(open(path).read())]
    print(f"{m.group(1)},{m.group(2)},{len(ms)},{sum(ms):.1f},{max(ms):.2f},{statistics.median(ms):.3f}")
