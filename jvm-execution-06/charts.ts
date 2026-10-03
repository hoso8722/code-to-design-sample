// 実行: npx tsx data/jvm-execution-06/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/jvm-execution-06";
const OUT = "site/public/images/jvm-execution-06";

const naive = readFileSync(join(DATA, "naive.txt"), "utf-8");
const avg = (xs: number[]) => xs.reduce((a, b) => a + b, 0) / xs.length;

// JMH の結果（CSV）：Benchmark, Mode, Threads, Samples, Score, Score Error, Unit, Param: n
const [, ...jmhLines] = readFileSync(join(DATA, "jmh-result.csv"), "utf-8").trim().split("\n");
const jmh = jmhLines.map((l) => l.split(",").map((c) => c.replace(/"/g, ""))).map((c) => ({
  bench: c[0].replace("bench.StringConcatBench.", ""),
  score: Number(c[4]),
  error: Number(c[5]),
  n: Number(c[7]),
}));
const jmhScore = (b: string, n: number) => jmh.find((r) => r.bench === b && r.n === n)!;

// 1. 結果を使わない計算（素朴な計測、6回の平均）
{
  const unused = avg([...naive.matchAll(/結果を捨てる (\d+) ms/g)].map((m) => Number(m[1])));
  const used = avg([...naive.matchAll(/結果を使う (\d+) ms/g)].map((m) => Number(m[1])));
  writeSvg(
    join(OUT, "dead-code.svg"),
    hBarChart({
      title: "10億回の計算を素朴に計った時間（6回の平均）",
      unit: "ミリ秒",
      labelWidth: 200,
      xTicks: [0, 500, 1000, 1500, 2000],
      rows: [
        { label: "結果を捨てる", value: unused, valueLabel: `${fmt(unused)} ms`, slot: 2 },
        { label: "結果を使う", value: used, valueLabel: `${fmt(used)} ms`, slot: 1 },
      ],
    })
  );
}

// 2. 同じ比較（n = 100）を、素朴な計測と JMH で
{
  const perOp = (name: string, order: string) => {
    const sections = naive.split("10億回の計算").filter((s) => s.includes(order));
    return avg(sections.map((s) => Number(s.match(new RegExp(`${name}\\s+2,000回で ([\\d,]+) µs`))![1].replace(/,/g, "")) / 2000));
  };
  const rows = [
    { label: "素朴（plusLoop を先に計る）", p: perOp("plusLoop", "plusLoop → builder"), b: perOp("builder", "plusLoop → builder") },
    { label: "素朴（builder を先に計る）", p: perOp("plusLoop", "builder → plusLoop"), b: perOp("builder", "builder → plusLoop") },
    { label: "JMH", p: jmhScore("plusLoop", 100).score, b: jmhScore("builder", 100).score },
  ];
  writeSvg(
    join(OUT, "naive-vs-jmh.svg"),
    hBarChart({
      title: "数を100個つなぐ処理の1回あたりの時間：素朴な計測と JMH",
      unit: "マイクロ秒",
      labelWidth: 230,
      xTicks: [0, 10, 20, 30, 40],
      groups: { 0: "plusLoop（+ でつなぐ）", 3: "builder（StringBuilder）" },
      legendItems: [
        { name: "素朴な計測", slot: 2 },
        { name: "JMH", slot: 1 },
      ],
      rows: [
        ...rows.map((r) => ({ label: r.label, value: r.p, valueLabel: `${fmt(r.p, 2)} µs`, slot: r.label === "JMH" ? 1 : 2 })),
        ...rows.map((r) => ({ label: r.label, value: r.b, valueLabel: `${fmt(r.b, 2)} µs`, slot: r.label === "JMH" ? 1 : 2 })),
      ],
    })
  );
}

// 3. JMH：つなぐ数と1回あたりの時間（両軸とも対数）
{
  const ns = [10, 100, 1000];
  const series = (b: string, slot: number, label: string) => ({
    name: label,
    endLabel: `${label} ${fmt(jmhScore(b, 1000).score, 1)} µs`,
    slot,
    markers: true,
    points: ns.map((n) => [Math.log10(n), Math.log10(jmhScore(b, n).score)] as [number, number]),
  });
  writeSvg(
    join(OUT, "jmh-scaling.svg"),
    lineChart({
      title: "JMH：つなぐ数と、1回あたりの時間（両軸とも対数目盛り）",
      xLabel: "つなぐ数（n）",
      yLabel: "1回あたりの時間",
      xTicks: [0, 1, 2, 3],
      xFormat: (v) => fmt(10 ** v),
      yTicks: [-2, -1, 0, 1, 2, 3].map((v) => v + 2),
      yFormat: (v) => `${fmt(10 ** (v - 2), v < 2 ? 2 : 0)} µs`,
      rightPad: 190,
      series: [series("plusLoop", 2, "plusLoop"), series("joining", 3, "joining"), series("builder", 1, "builder")].map((s) => ({
        ...s,
        points: s.points.map(([x, y]) => [x, y + 2] as [number, number]),  // 縦軸を 0 始まりにするため、2 だけずらす
      })),
    })
  );
}
