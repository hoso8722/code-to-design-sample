// 実行: npx tsx data/java-io-08/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-08";
const OUT = "site/public/images/java-io-08";

// 1回目の実行の結果を読む
const first = readFileSync(join(DATA, "waiting.txt"), "utf-8").split("# 2回目")[0];
const rows = [...first.matchAll(/^(.+?): (\d+) ms（成功 \d+ 件、JVM内プラットフォームスレッドのピーク (\d+)、サーバーの同時処理の最大数 (\d+)）$/gm)].map((m) => ({
  name: m[1],
  ms: Number(m[2]),
  threads: Number(m[3]),
  active: Number(m[4]),
}));
const slot = (name: string) => (name.startsWith("仮想スレッド") ? 1 : 2);

// 1. 1,000回の要求を送り終えるまでの時間
writeSvg(
  join(OUT, "time.svg"),
  hBarChart({
    title: "応答に200ミリ秒かかるサーバーに、1,000回の要求を送る時間",
    unit: "ミリ秒",
    labelWidth: 250,
    xTicks: [0, 1000, 2000, 3000, 4000, 5000],
    rows: rows.map((r) => ({ label: r.name, value: r.ms, valueLabel: `${fmt(r.ms)} ms`, slot: slot(r.name) })),
  })
);

// 2. JVM全体で観測したプラットフォームスレッドのピーク数
writeSvg(
  join(OUT, "threads.svg"),
  hBarChart({
    title: "クライアントとサーバーを含むJVM内プラットフォームスレッドのピーク",
    unit: "本",
    labelWidth: 250,
    xTicks: [0, 100, 200, 300],
    rows: rows.map((r) => ({ label: r.name, value: r.threads, valueLabel: `${fmt(r.threads)}本`, slot: slot(r.name) })),
  })
);
