#!/usr/bin/env python3
"""ブレースの深さだけで、インデントを整える簡易な整形（生成したソース用）"""
import glob, sys

def fmt(path):
    depth, out = 0, []
    for raw in open(path, encoding="utf-8").read().split("\n"):
        line = raw.strip()
        if not line:
            out.append("")
            continue
        opens = line.count("{") - line.count("}")
        lead = depth - (1 if line.startswith("}") else 0)
        if line.startswith("}") and opens > 0:   # "} else {" のような行
            lead = depth - 1
        out.append("    " * max(lead, 0) + line)
        depth += opens
    open(path, "w", encoding="utf-8").write("\n".join(out))

for p in glob.glob("*/*/exam/*.java"):
    fmt(p)
