#!/usr/bin/env python3
"""呼び出し側のコードの長さを数える（空白を除いた文字数と、行数）"""
sites = {
    "引数の多いコンストラクタ": 'new LegacyApplication("佐藤", 60, 90, 50, 10, 1, false, false)',
    "static factory（省略値あり）": 'Application.basic("佐藤", 60, 90, 50)',
    "引数を型でまとめた版": 'new TypedApplication("佐藤", new Scores(60, 90, 50), new TypedApplication.Bonus(10), new TypedApplication.Preference(1))',
    "Builder": 'new ApplicationBuilder().name("佐藤").math(60).english(90).science(50).bonus(10).build()',
}
print("書き方,空白を除いた文字数,読み取れる項目名の数")
names = ["name", "math", "english", "science", "bonus", "preference", "scholarship", "transfer"]
for label, code in sites.items():
    print(f"{label},{len(''.join(code.split()))},{sum(1 for n in names if n + '(' in code or n.capitalize() + '(' in code)}")
