// 実行: npx tsx data/java-io-01/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-01";
const OUT = "site/public/images/java-io-01";
const log = Math.log10;

// 「ラベル  1234.5 ms」の行から、最初に現れた値を取り出す（1回目の実行の値）
const pick = (text: string, label: string): number => {
  const line = text.split("\n").find((l) => l.startsWith(label))!;
  return Number(line.match(/([\d.]+) ms/)![1]);
};

// 1. 4MB のファイルを読む時間
{
  const text = readFileSync(join(DATA, "read-ways.txt"), "utf-8");
  const rows = [
    { label: "1バイトずつ（FileInputStream）", key: "1バイトずつ（FileInputStream）", slot: 2 },
    { label: "1バイトずつ（BufferedInputStream）", key: "1バイトずつ（BufferedInputStream）", slot: 3 },
    { label: "8,192バイトの配列で読む", key: "8,192バイトの配列で読む", slot: 1 },
    { label: "Files.readAllBytes", key: "Files.readAllBytes", slot: 4 },
  ].map((r) => {
    const v = pick(text, r.key);
    return { label: r.label, value: v, valueLabel: `${fmt(v, 1)} ms`, slot: r.slot };
  });
  writeSvg(
    join(OUT, "read-ways.svg"),
    hBarChart({ title: "4MBのファイルを読む時間", unit: "ミリ秒", labelWidth: 270, xTicks: [0, 2000, 4000, 6000], rows })
  );
}

// 2. 配列の大きさと、32MB のファイルを読む時間（両軸とも対数）
{
  const lines = readFileSync(join(DATA, "buffer-size.csv"), "utf-8").trim().split("\n").slice(1);
  const points: [number, number][] = lines.map((l) => {
    const [size, , ms] = l.split(",").map(Number);
    return [log(size), log(ms)];
  });
  const sizeLabel = (v: number) => {
    const n = Math.round(10 ** v);
    return n >= 1048576 ? "1MB" : n >= 1024 ? `${fmt(n / 1024)}KB` : `${n}B`;
  };
  writeSvg(
    join(OUT, "buffer-size.svg"),
    lineChart({
      title: "配列の大きさと、32MBのファイルを読む時間（両軸とも対数目盛り）",
      xLabel: "1回の read に渡す配列の大きさ",
      yLabel: "時間（ミリ秒）",
      xTicks: [16, 64, 256, 1024, 8192, 65536, 1048576].map(log),
      xFormat: sizeLabel,
      yTicks: [0, 1, 2, 3, 4],
      yFormat: (v) => fmt(10 ** v),
      rightPad: 40,
      endLabels: false,
      series: [{ name: "読む時間", slot: 1, markers: true, points }],
    })
  );
}

// 3. 1MB を書く時間
{
  const text = readFileSync(join(DATA, "write-ways.txt"), "utf-8");
  const rows = [
    { label: "1バイトずつ（FileOutputStream）", slot: 2 },
    { label: "1バイトずつ（BufferedOutputStream）", slot: 3 },
    { label: "配列ごと1回で書く", slot: 1 },
  ].map((r) => {
    const v = pick(text, r.label);
    return { label: r.label, value: v, valueLabel: `${fmt(v, 1)} ms`, slot: r.slot };
  });
  writeSvg(
    join(OUT, "write-ways.svg"),
    hBarChart({ title: "1MBをファイルに書く時間", unit: "ミリ秒", labelWidth: 280, xTicks: [0, 1000, 2000, 3000], rows })
  );
}
