# GCログ（-Xlog:gc*:file=...:uptime,level,tags）から、停止（Pause）ごとの行をCSVにする
import re, sys
pat = re.compile(r"^\[([\d.]+)s\]\[info\]\[gc\s*\] GC\((\d+)\) Pause (.+?) (\d+)M->(\d+)M\((\d+)M\) ([\d.]+)ms")
regions = {}
reg = re.compile(r"^\[[\d.]+s\]\[info\]\[gc,heap\s*\] GC\((\d+)\) (Eden|Survivor|Old|Humongous) regions: (\d+)->(\d+)")
lines = open(sys.argv[1]).read().splitlines()
for l in lines:
    m = reg.match(l)
    if m:
        regions.setdefault(m.group(1), {})[m.group(2)] = (m.group(3), m.group(4))
print("end_s,gc,type,before_mb,after_mb,committed_mb,pause_ms,old_after,humongous_after")
for l in lines:
    m = pat.match(l)
    if not m: continue
    t, gc, kind, b, a, c, ms = m.groups()
    kind = re.sub(r" \((G1 Evacuation Pause|G1 Humongous Allocation|G1 Preventive Collection)\)", "", kind)
    r = regions.get(gc, {})
    print(f"{t},{gc},{kind},{b},{a},{c},{ms},{r.get('Old',('',''))[1]},{r.get('Humongous',('',''))[1]}")
