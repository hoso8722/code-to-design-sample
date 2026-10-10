// 実行: npx tsx data/java-http-client-05/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { timelineChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-http-client-05";
const OUT = "site/public/images/java-http-client-05";

// Parallel3 の出力(mode,name,start,end)から、要求ごとの開始と終了を読む
const rows = readFileSync(join(DATA, "output-Parallel3.txt"), "utf-8")
  .split("\n")
  .filter((l) => /^(sequential|async-parallel-server|async-single-thread-server),/.test(l))
  .map((l) => l.split(","))
  .map(([mode, name, start, end]) => ({ mode, name, start: Number(start), end: Number(end) }));

const label: Record<string, string> = {
  sequential: "send を3回(順番に)",
  "async-parallel-server": "sendAsync(サーバーは並列)",
  "async-single-thread-server": "sendAsync(サーバーは1本)",
};
const slot: Record<string, number> = { sequential: 1, "async-parallel-server": 2, "async-single-thread-server": 3 };
const order = ["sequential", "async-parallel-server", "async-single-thread-server"];
const sorted = order.flatMap((mode) => rows.filter((r) => r.mode === mode).sort((a, b) => a.name.localeCompare(b.name)));
const laneOf = (r: { mode: string; name: string }) => `${label[r.mode]} ${r.name}`;

writeSvg(
  join(OUT, "parallel.svg"),
  timelineChart({
    title: "300ミリ秒かかる要求を3つ送ったときの、要求ごとの開始と終了",
    xLabel: "時間（ミリ秒）",
    lanes: sorted.map(laneOf),
    laneWidth: 250,
    xTicks: [0, 200, 400, 600, 800, 1000],
    legendItems: order.map((m) => ({ name: label[m], slot: slot[m] })),
    bars: sorted.map((r) => ({ lane: laneOf(r), start: r.start, end: r.end, slot: slot[r.mode], label: `${r.end - r.start}` })),
  })
);
