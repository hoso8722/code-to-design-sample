#!/usr/bin/env python3
"""/proc/<pid>/smaps から、約1MBのスレッドスタックの領域ごとにRSSを数え、値ごとの本数を表示する。
使い方: python3 stack-rss.py <pid>
"""
import re
import sys
from collections import Counter

entries = []
size = rss = None
anon = False
name = ""
for line in open(f"/proc/{sys.argv[1]}/smaps"):
    if re.match(r"^[0-9a-f]+-[0-9a-f]+ ", line):
        if size is not None:
            entries.append((size, rss, name))
        parts = line.split()
        name = parts[5] if len(parts) > 5 else ""
        size = rss = None
    elif line.startswith("Size:"):
        size = int(line.split()[1])
    elif line.startswith("Rss:"):
        rss = int(line.split()[1])
if size is not None:
    entries.append((size, rss, name))

# 1MB(1024KB)のスタックと、その前後のガードページを含む領域: 1000〜1100KB、無名
stacks = [rss for size, rss, name in entries if 1000 <= size <= 1100 and name == ""]
print(f"stacks={len(stacks)} total_rss_kb={sum(stacks)}")
for rss_kb, n in sorted(Counter(stacks).items(), key=lambda kv: -kv[1])[:6]:
    print(f"{rss_kb:>6} KB  {n:>5} 本")
