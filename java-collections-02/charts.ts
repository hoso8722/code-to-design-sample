// 実行: npx tsx data/java-collections-02/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";
import { readJmhCsv } from "../../scripts/lib/jmh.js";

const DATA = "data/java-collections-02";
const OUT = "site/public/images/java-collections-02";
const log = Math.log10;
// 対数の目盛りの表示（1、10、100、1,000、1万、10万、100万、1000万）
const jp = (v: number) => ["1", "10", "100", "1,000", "1万", "10万", "100万", "1000万"][Math.round(v)];

// 1. 要素数と内部の配列の大きさ（階段状。両軸とも対数）
{
  const lines = readFileSync(join(DATA, "capacity.txt"), "utf-8").split("\n").filter((l) => /^\d+,\d+$/.test(l));
  const steps = lines.map((l) => l.split(",").map(Number));
  const points: [number, number][] = [];
  steps.forEach(([size, cap], i) => {
    points.push([log(size), log(cap)]);
    const next = steps[i + 1];
    points.push([log(next ? next[0] - 1 : 1_000_000), log(cap)]);  // 次に作り直すまで同じ大きさ
  });
  writeSvg(
    join(OUT, "capacity.svg"),
    lineChart({
      title: "ArrayList に1つずつ追加したときの、内部の配列の大きさ（両軸とも対数目盛り）",
      xLabel: "要素数（size）",
      yLabel: "内部の配列の大きさ（容量）",
      xTicks: [0, 1, 2, 3, 4, 5, 6],
      xFormat: jp,
      yTicks: [0, 1, 2, 3, 4, 5, 6, 7],
      yFormat: jp,
      rightPad: 150,
      series: [
        { name: "要素数と同じ（参考）", endLabel: "要素数と同じ", slot: 3, dashed: true, points: [[0, 0], [6, 6]] },
        { name: "容量", endLabel: `容量 ${fmt(steps[steps.length - 1][1])}`, slot: 1, points },
      ],
    })
  );
}

// 2. 100万個を追加する時間と確保量（JMH）
{
  const rows = readJmhCsv(join(DATA, "jmh-Presize.csv"));
  const get = (b: string, n: string) => rows.find((r) => r.benchmark === b && r.params.n === n)!;
  const t = (b: string) => get(b, "1000000").score / 1000;  // ミリ秒
  const a = (b: string) => get(`${b}:gc.alloc.rate.norm`, "1000000").score / 1e6;  // MB
  writeSvg(
    join(OUT, "presize-time.svg"),
    hBarChart({
      title: "100万個を追加したリストを作る時間（JMH）",
      unit: "ミリ秒",
      labelWidth: 230,
      xTicks: [0, 10, 20, 30, 40],
      rows: [
        { label: "new ArrayList<>()", value: t("defaultCapacity"), valueLabel: `${fmt(t("defaultCapacity"), 1)} ms`, slot: 2 },
        { label: "new ArrayList<>(1_000_000)", value: t("presized"), valueLabel: `${fmt(t("presized"), 1)} ms`, slot: 1 },
      ],
    })
  );
  writeSvg(
    join(OUT, "presize-alloc.svg"),
    hBarChart({
      title: "100万個を追加したリストを作るときに確保したメモリ（JMH の -prof gc）",
      unit: "MB",
      labelWidth: 230,
      xTicks: [0, 5, 10, 15, 20],
      rows: [
        { label: "new ArrayList<>()", value: a("defaultCapacity"), valueLabel: `${fmt(a("defaultCapacity"), 1)} MB`, slot: 2 },
        { label: "new ArrayList<>(1_000_000)", value: a("presized"), valueLabel: `${fmt(a("presized"), 1)} MB`, slot: 1 },
      ],
    })
  );
}
