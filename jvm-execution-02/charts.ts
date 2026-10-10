// 実行: npx tsx data/jvm-execution-02/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/jvm-execution-02";
const OUT = "site/public/images/jvm-execution-02";

// 1回ずつの時間を、呼び出し回数の対数で等間隔な区間ごとの平均にまとめる
function intervalAverages(file: string): [number, number][] {
  const [, ...lines] = readFileSync(join(DATA, file), "utf-8").trim().split("\n");
  const nanos = lines.map((l) => Number(l.split(",")[1]));
  const points: [number, number][] = [];
  const edges: number[] = [];
  for (let e = 0; e <= Math.log10(nanos.length); e += 0.1) edges.push(Math.round(10 ** e));
  edges.push(nanos.length);
  const unique = [...new Set(edges)];
  for (let i = 0; i + 1 < unique.length; i++) {
    const slice = nanos.slice(unique[i] - 1, unique[i + 1] - 1).sort((a, b) => a - b);
    if (slice.length === 0) continue;
    const average = slice.reduce((sum, value) => sum + value, 0) / slice.length;
    const mid = Math.sqrt(unique[i] * unique[i + 1]);  // 区間の真ん中（対数で）
    points.push([Math.log10(mid), average]);
  }
  return points;
}

const pow = (v: number) => 10 ** v;
const nsLabel = (ns: number) => {
  return ns >= 1000 ? `${fmt(ns / 1000)} µs` : `${fmt(ns)} ns`;
};

writeSvg(
  join(OUT, "warmup.svg"),
  lineChart({
    title: "sumOfSquares を2万回呼んだときの、1回あたりの時間（区間ごとの平均）",
    xLabel: "何回目の呼び出しか（対数目盛り）",
    yLabel: "1回あたりの時間（線形目盛り）",
    xTicks: [0, 1, 2, 3, 4, 5],
    xFormat: (v) => fmt(pow(v)),
    yTicks: [0, 50000, 100000, 150000, 200000],
    yFormat: nsLabel,
    rightPad: 140,
    series: [
      { name: "-Xint（インタプリタだけ）", endLabel: "-Xint", slot: 2, markers: true, points: intervalAverages("trace-xint.csv") },
      { name: "通常（JITコンパイルあり）", endLabel: "通常", slot: 1, markers: true, points: intervalAverages("trace-jit.csv") },
    ],
  })
);

// プログラム全体の時間
{
  const text = readFileSync(join(DATA, "total-time.txt"), "utf-8");
  const normal = Number(text.match(/^ total (\d+) ms/m)![1]);
  const xint = Number(text.match(/-Xint total (\d+) ms/)![1]);
  writeSvg(
    join(OUT, "total.svg"),
    hBarChart({
      title: "Warmup 全体（2万回）の実行時間（JVMの起動を含む）",
      unit: "ミリ秒",
      labelWidth: 200,
      xTicks: [0, 200, 400, 600, 800],
      rows: [
        { label: "通常（JITコンパイルあり）", value: normal, valueLabel: `${normal} ms`, slot: 1 },
        { label: "-Xint（インタプリタだけ）", value: xint, valueLabel: `${xint} ms`, slot: 2 },
      ],
    })
  );
}
