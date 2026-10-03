// 実行: npx tsx data/java-collections-08/charts.ts
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";
import { readJmhCsv } from "../../scripts/lib/jmh.js";

const DATA = "data/java-collections-08";
const OUT = "site/public/images/java-collections-08";
const log = Math.log10;
const speedLabel = (v: number) => (v >= 1 ? `${fmt(v, 1)} 倍速い` : `${fmt(1 / v, v < 0.01 ? 0 : 1)} 倍遅い`);
const tick = (v: number) => (v >= 0 ? `${fmt(10 ** v)}倍` : `1/${fmt(10 ** -v)}`);

// 1. 要素数と、並列にしたときの速さの倍率（逐次の時間 ÷ 並列の時間）
{
  const rows = readJmhCsv(join(DATA, "jmh-ParallelSum.csv"));
  const s = (b: string, n: number, w: string) => rows.find((r) => r.benchmark === b && r.params.n === String(n) && r.params.work === w)!.score;
  const ns = [1000, 100000, 10000000];
  const speedup = (w: string) => ns.map((n) => [log(n), log(s("sequential", n, w) / s("parallel", n, w))] as [number, number]);
  writeSvg(
    join(OUT, "speedup.svg"),
    lineChart({
      title: "IntStream.range(0, n) を並列にしたときの速さ（逐次の何倍か、両軸とも対数目盛り）",
      xLabel: "要素数 n",
      yLabel: "逐次に対する速さ",
      xTicks: [3, 4, 5, 6, 7],
      xFormat: (v) => ["", "", "", "1,000", "1万", "10万", "100万", "1,000万"][v],
      yTicks: [-3, -2, -1, 0, 1, 2],
      yFormat: tick,
      rightPad: 200,
      series: [
        { name: "1倍（逐次と同じ）", endLabel: "逐次と同じ", slot: 3, dashed: true, points: [[3, 0], [7, 0]] },
        { name: "重い処理（計算100回）", endLabel: `重い処理 ${speedLabel(10 ** speedup("heavy")[2][1])}`, slot: 1, markers: true, points: speedup("heavy") },
        { name: "軽い処理（掛け算1回）", endLabel: `軽い処理 ${speedLabel(10 ** speedup("light")[2][1])}`, slot: 2, markers: true, points: speedup("light") },
      ],
    })
  );
}

// 2. データの持ち方と処理の重さ（10万個）
{
  const rows = readJmhCsv(join(DATA, "jmh-ParallelSource.csv"));
  const s = (b: string, src: string, w: string) => rows.find((r) => r.benchmark === b && r.params.source === src && r.params.work === w)!.score;
  const sources = ["ArrayList", "LinkedList", "Stream.iterate"];
  const bars = (w: string, slot: number) =>
    sources.map((src) => {
      const v = s("sequential", src, w) / s("parallel", src, w);
      return { label: src, value: log(v) + 1, valueLabel: speedLabel(v), slot };  // 1/10 倍を左端にするため +1
    });
  writeSvg(
    join(OUT, "sources.svg"),
    hBarChart({
      title: "10万個を並列にしたときの速さ（逐次の何倍か、横軸は対数目盛り。1倍より左は遅くなった）",
      unit: "逐次に対する速さ",
      labelWidth: 150,
      xTicks: [0, 1, 2, 3],
      format: (v) => tick(v - 1),
      groups: { 0: "軽い処理（掛け算1回）", 3: "重い処理（計算100回）" },
      rows: [...bars("light", 2), ...bars("heavy", 1)],
    })
  );
}
