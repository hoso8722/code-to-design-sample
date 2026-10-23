// 実行: npx tsx data/jvm-execution-04/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, svgDoc, writeSvg, esc, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/jvm-execution-04";
const OUT = "site/public/images/jvm-execution-04";

// 1. 実装クラスの種類の数と、1回の呼び出しの時間
{
  const [, ...lines] = readFileSync(join(DATA, "morphism.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(","));
  const mean = (o: string, k: number) => {
    const xs = rows.filter(([opt, kinds]) => opt === o && Number(kinds) === k).map(([, , , ns]) => Number(ns));
    return xs.reduce((a, b) => a + b, 0) / xs.length;
  };
  const ks = [1, 2, 3, 4];
  writeSvg(
    join(OUT, "morphism.svg"),
    lineChart({
      title: "s.area() の実装クラスの種類の数と、1要素あたりの時間（3回の平均）",
      xLabel: "配列に入っている図形の種類の数",
      yLabel: "1要素あたりの時間（ナノ秒）",
      xTicks: [0, 1, 2, 3, 4],
      xFormat: (v) => (v === 0 ? "" : String(v)),
      yTicks: [0, 5, 10, 15],
      rightPad: 170,
      series: [
        { name: "インライン展開なし（-XX:-Inline）", endLabel: "-XX:-Inline", slot: 2, markers: true, points: ks.map((k) => [k, mean("noinline", k)]) },
        { name: "デフォルト", endLabel: "デフォルト", slot: 1, markers: true, points: ks.map((k) => [k, mean("default", k)]) },
      ],
    })
  );
}

// 2. インライン展開とエスケープ解析
{
  const text = readFileSync(join(DATA, "inline-escape.txt"), "utf-8");
  const mb = (o: string) => Number(text.match(new RegExp(`\\[${o}\\] .*確保量: ([\\d.]+) MB`))![1]);
  writeSvg(
    join(OUT, "inline-escape.svg"),
    hBarChart({
      title: "makePoint() で作った Point を1,000万個使ったときのヒープへの確保量",
      unit: "MB",
      labelWidth: 230,
      xTicks: [0, 100, 200, 300],
      rows: [
        { label: "デフォルト", value: mb("default"), valueLabel: `${fmt(mb("default"), 1)} MB`, slot: 1 },
        { label: "-XX:-Inline", value: mb("-XX:-Inline"), valueLabel: `${fmt(mb("-XX:-Inline"), 1)} MB`, slot: 2 },
        { label: "-XX:-DoEscapeAnalysis", value: mb("-XX:-DoEscapeAnalysis"), valueLabel: `${fmt(mb("-XX:-DoEscapeAnalysis"), 1)} MB`, slot: 2 },
      ],
    })
  );
}

// 3. 種類の数によって、s.area() の呼び出しがどうコンパイルされるか（模式図）
{
  const parts: string[] = [];
  const title = "C2 が s.area() をどうコンパイルするか（模式図）";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const col = (x: number, heading: string, sub: string, boxes: [string, string][]) => {
    parts.push(`<text x="${x}" y="52" class="ink bold">${esc(heading)}</text>`);
    parts.push(`<text x="${x}" y="70" class="muted small">${esc(sub)}</text>`);
    boxes.forEach(([text, cls], i) => {
      const y = 84 + i * 46;
      parts.push(`<rect x="${x}" y="${y}" width="220" height="38" rx="6" class="${cls}"/>`);
      parts.push(`<text x="${x + 110}" y="${y + 24}" text-anchor="middle" class="small" style="fill:${cls === "box" || cls === "neutral" ? "var(--ink)" : "#fff"}">${esc(text)}</text>`);
    });
  };
  col(0, "1種類（モノモーフィック）", "プロファイル：Square が 100%", [
    ["Square か？", "box"],
    ["はい → side * side（埋め込み）", "f1"],
    ["いいえ → 脱最適化（第5回）", "neutral"],
  ]);
  col(250, "2種類（バイモーフィック）", "プロファイル：Square 50%・Rect 50%", [
    ["Square か？ → side * side（埋め込み）", "f1"],
    ["Rect か？ → w * h（埋め込み）", "f1"],
    ["どちらでもない → 脱最適化", "neutral"],
  ]);
  col(500, "3種類以上（メガモーフィック）", "プロファイル：3種類に分散", [
    ["仮想呼び出し（呼び出し先を探す）", "f2"],
    ["area() を呼ぶ（埋め込めない）", "f2"],
  ]);
  parts.push(`<text x="0" y="238" class="muted">青：呼び出しが消えて、中身が埋め込まれる　橙：実際にメソッドを呼び出す</text>`);
  writeSvg(join(OUT, "dispatch.svg"), svgDoc(720, 250, title, parts.join("\n")));
}
