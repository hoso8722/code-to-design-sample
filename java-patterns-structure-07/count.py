#!/usr/bin/env python3
"""機能の組み合わせを実現するために必要なクラスの数と、空行を除いた行数を、機能の数ごとに数える"""
import glob

def lines(path):
    return sum(1 for l in open(path, encoding="utf-8") if l.strip())

inherit = {f.split("/")[-1][:-5]: lines(f) for f in glob.glob("inherit/exam/*Judge.java")
           if f.split("/")[-1] not in ("Judge.java", "ScoreJudge.java")}
decor = {f.split("/")[-1][:-5]: lines(f) for f in glob.glob("decorator/exam/*Judge.java")
         if f.split("/")[-1] not in ("Judge.java", "ScoreJudge.java")}
print("方式,機能3つに必要なクラス数,空行を除いた行数の合計")
print(f"継承（組み合わせごと）,{len(inherit)},{sum(inherit.values())}")
print(f"Decorator（機能ごと）,{len(decor)},{sum(decor.values())}")
print()
print("機能の数,継承で必要なクラス数(2^n-1),Decoratorで必要なクラス数(n)")
for n in range(1, 7):
    print(f"{n},{2**n - 1},{n}")
