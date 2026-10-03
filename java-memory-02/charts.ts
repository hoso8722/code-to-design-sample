// 実行: npx tsx data/java-memory-02/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, svgDoc, writeSvg, esc, fmt } from "../../scripts/lib/svg-chart.js";
import { readCsv } from "../../scripts/lib/nmt.js";

const DATA = "data/java-memory-02";
const OUT = "site/public/images/java-memory-02";

// 1. スタックフレームとヒープの関係（Frames.java の total() の最後の繰り返しで止めたとき）
{
  const parts: string[] = [];
  const title = "total() の最後の繰り返し（s = 90）で止めたときのスタックとヒープ";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  // スタック
  parts.push(`<text x="20" y="52" class="ink bold">スタック（main スレッド）</text>`);
  parts.push(`<text x="20" y="70" class="muted">呼び出すたびに上にフレームが積まれる</text>`);
  const frame = (y: number, name: string, rows: [string, string][]) => {
    const h = 30 + rows.length * 30;
    parts.push(`<rect x="20" y="${y}" width="270" height="${h}" rx="6" class="box"/>`);
    parts.push(`<text x="32" y="${y + 20}" class="ink bold">${esc(name)}</text>`);
    rows.forEach(([k, v], i) => {
      const ry = y + 30 + i * 30;
      parts.push(`<rect x="32" y="${ry}" width="246" height="24" rx="4" class="box"/>`);
      parts.push(`<text x="42" y="${ry + 17}" class="ink">${esc(k)}</text>`);
      parts.push(`<text x="268" y="${ry + 17}" text-anchor="end" class="${v === "●" ? "ink" : "ink"}">${esc(v)}</text>`);
    });
    return h;
  };
  frame(86, "total(scores) のフレーム", [["scores（参照）", "●"], ["sum（int）", "170"], ["s（int）", "90"]]);
  frame(222, "main(args) のフレーム", [["args（参照）", "●"], ["count（int）", "2"], ["p（参照）", "●"], ["scores（参照）", "●"]]);
  // ヒープ
  parts.push(`<rect x="420" y="40" width="280" height="330" rx="8" class="box"/>`);
  parts.push(`<text x="436" y="64" class="ink bold">ヒープ</text>`);
  parts.push(`<text x="436" y="82" class="muted">new で作ったオブジェクト</text>`);
  const obj = (y: number, label: string, body: string) => {
    parts.push(`<rect x="470" y="${y}" width="200" height="48" rx="6" class="f1 wash"/>`);
    parts.push(`<rect x="470" y="${y}" width="200" height="48" rx="6" fill="none" class="k1" stroke-width="1.5"/>`);
    parts.push(`<text x="482" y="${y + 20}" class="ink bold">${esc(label)}</text>`);
    parts.push(`<text x="482" y="${y + 38}" class="ink">${esc(body)}</text>`);
  };
  obj(110, "int[]", "{ 80, 90 }");
  obj(200, "Point", "x = 1, y = 2");
  obj(290, "String[]", "長さ 0");
  // 参照の矢印（スタックの●から、ヒープのオブジェクトへ）
  const arrow = (fromY: number, toY: number) =>
    parts.push(`<path d="M280,${fromY} C370,${fromY} 380,${toY} 466,${toY}" class="edge" marker-end="url(#arrow)"/>`);
  // 行の中心の y 座標：フレームの y + 30 + 行番号 × 30 + 12
  arrow(128, 128); // total の scores → int[]
  arrow(354, 146); // main の scores → int[]
  arrow(324, 224); // main の p → Point
  arrow(264, 314); // main の args → String[]
  parts.push(`<text x="20" y="400" class="muted">スタックにあるのは値（int）と参照だけ。2つの scores は同じ int[] を指している</text>`);
  writeSvg(join(OUT, "frames.svg"), svgDoc(720, 412, title, parts.join("\n")));
}

// 2. -Xss と再帰の深さ（-Xint で計測。毎回同じ値になる）
{
  const rows = readCsv(readFileSync(join(DATA, "stack-depth.csv"), "utf-8").replace(/,int,/g, ",0,").replace(/,jit,/g, ",1,"));
  const interp = rows.filter((r) => r.mode === 0 && r.run === 1);
  writeSvg(
    join(OUT, "xss-depth.svg"),
    lineChart({
      title: "-Xss と、StackOverflowError になるまでの再帰の深さ（-Xint）",
      xLabel: "-Xss（KB）",
      yLabel: "再帰の深さ（回）",
      xTicks: [0, 1024, 2048, 3072, 4096],
      series: [
        { name: "small（ローカル変数なし）", endLabel: "small", slot: 1, markers: true, points: interp.map((r) => [r.xss_kb, r.small]) },
        { name: "large（long のローカル変数8個）", endLabel: "large", slot: 2, markers: true, points: interp.map((r) => [r.xss_kb, r.large]) },
      ],
    })
  );
}

// 3. エスケープ解析の有無と確保バイト数（10回目）
{
  const on = readCsv(readFileSync(join(DATA, "escape-on.csv"), "utf-8"));
  const off = readCsv(readFileSync(join(DATA, "escape-off.csv"), "utf-8"));
  const last = (rows: typeof on) => rows[rows.length - 1];
  writeSvg(
    join(OUT, "escape.svg"),
    hBarChart({
      title: "Point を1,000万個作ったときにヒープに確保したバイト数（10回目）",
      unit: "MB",
      labelWidth: 250,
      xTicks: [0, 100, 200, 300],
      groups: { 0: "Point がメソッドの外に出ない（noEscape）", 2: "Point をフィールドに入れる（escape）" },
      rows: [
        { label: "エスケープ解析あり（デフォルト）", value: last(on).noEscape_MB, slot: 1, valueLabel: `${fmt(last(on).noEscape_MB, 1)} MB` },
        { label: "-XX:-DoEscapeAnalysis", value: last(off).noEscape_MB, slot: 2, valueLabel: `${fmt(last(off).noEscape_MB, 1)} MB` },
        { label: "エスケープ解析あり（デフォルト）", value: last(on).escape_MB, slot: 1, valueLabel: `${fmt(last(on).escape_MB, 1)} MB` },
        { label: "-XX:-DoEscapeAnalysis", value: last(off).escape_MB, slot: 2, valueLabel: `${fmt(last(off).escape_MB, 1)} MB` },
      ],
      legendItems: [
        { name: "エスケープ解析あり", slot: 1 },
        { name: "エスケープ解析なし", slot: 2 },
      ],
    })
  );
}
