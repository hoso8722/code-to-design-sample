#!/usr/bin/env python3
"""第1〜7回の出力ファイルから、「言語機能や簡単な書き方」と「パターンの書き方」を比べた数値を集める"""
import csv, os, re

def lines(path):
    return open(path, encoding="utf-8").read().splitlines()

def find(path, key):
    for l in lines(path):
        if l.startswith(key):
            return l.split(",")
    raise KeyError((path, key))

root = ".."
print("回,比べたもの,簡単な書き方,パターンの書き方")

# 01 Strategy：区分を1つ足したときの追加行数
d = f"{root}/java-patterns-behavior-01/output-diffstat.txt"
lam, iface = int(find(d, "関数（ラムダ）")[3]), int(find(d, "interface（Strategy）")[3])
print(f"第1回 Strategy,区分を1つ足したときの追加行数,{lam}行（ラムダ）,{iface}行（interface）")

# 02 Template Method：行数
d = f"{root}/java-patterns-behavior-02/output-sizes.txt"
fn, tm = int(find(d, "関数を渡す")[2]), int(find(d, "Template Method")[2])
print(f"第2回 Template Method,全体の行数,{fn}行（関数を渡す）,{tm}行（Template Method）")

# 05 State：行数
d = f"{root}/java-patterns-behavior-05/output-sizes.txt"
flag, cls = int(find(d, "フラグと条件分岐")[2]), int(find(d, "状態ごとのクラス")[2])
print(f"第5回 State,4状態のときの行数,{flag}行（フラグ）,{cls}行（状態ごとのクラス）")

# 07 Visitor：行数
d = f"{root}/java-patterns-behavior-07/output-metrics.txt"
sealed, vis = int(find(d, "sealed + switch")[2]), int(find(d, "Visitor")[2])
print(f"第7回 Visitor,全体の行数,{sealed}行（sealed + switch）,{vis}行（Visitor）")

# 03 Observer：通知先を足したときの追加行数
d = f"{root}/java-patterns-behavior-03/output-diffstat.txt"
direct, lis = int(find(d, "判定のコードが直接呼ぶ")[3]), int(find(d, "リスナーを登録する")[3])
print(f"第3回 Observer,通知先を1つ足したときの追加行数,{direct}行（直接呼ぶ）,{lis}行（リスナー）")

# 06 Chain of Responsibility：検証を1つ足したときの追加行数
d = f"{root}/java-patterns-behavior-06/output-diffstat.txt"
longm, chain = int(find(d, "1つの長いメソッド")[3]), int(find(d, "検証の部品を連ねる")[3])
print(f"第6回 Chain of Responsibility,検証を1つ足したときの追加行数,{longm}行（長いメソッド）,{chain}行（部品を連ねる）")
