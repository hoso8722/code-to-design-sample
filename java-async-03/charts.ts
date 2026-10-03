// 実行: npx tsx data/java-async-03/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { lineChart, timelineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-03";
const OUT = "site/public/images/java-async-03";

// 1. プールの大きさと処理時間
{
  const [, ...lines] = readFileSync(join(DATA, "pool-size.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([kind, n, ms]) => ({ kind, n: Number(n), ms: Number(ms) }));
  const sizes = [1, 2, 4, 8, 16, 32, 64];
  const mean = (kind: string, n: number) => {
    const xs = rows.filter((r) => r.kind === kind && r.n === n).map((r) => r.ms);
    return xs.reduce((a, b) => a + b, 0) / xs.length;
  };
  writeSvg(
    join(OUT, "pool-size.svg"),
    lineChart({
      title: "64個のタスクを、プールの大きさを変えて実行したときの処理時間",
      xLabel: "プールのスレッドの数（newFixedThreadPool の引数）",
      yLabel: "処理時間（ミリ秒）",
      xTicks: [0, 1, 2, 3, 4, 5, 6],
      xFormat: (v) => fmt(2 ** v),
      yTicks: [0, 1000, 2000, 3000, 4000],
      rightPad: 190,
      series: [
        { name: "待つ処理（50ミリ秒 × 64）", endLabel: `待つ処理 ${fmt(mean("wait", 64))} ms`, slot: 1, markers: true, points: sizes.map((n) => [Math.log2(n), mean("wait", n)]) },
        { name: "CPUを使う処理（約50ミリ秒 × 64）", endLabel: `CPU ${fmt(mean("cpu", 64))} ms`, slot: 2, markers: true, points: sizes.map((n) => [Math.log2(n), mean("cpu", n)]) },
      ],
    })
  );
}

// 2. キューで待たされるタスク
{
  const [, ...lines] = readFileSync(join(DATA, "queue-timeline.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([task, thread, sub, s, e]) => ({ task, thread, sub: Number(sub), start: Number(s), end: Number(e) }));
  const t0 = Math.min(...rows.map((r) => r.sub));
  const threadSlot = (thread: string) => Number(thread.replace(/.*thread-/, ""));  // thread-1 → 1 番目の色
  writeSvg(
    join(OUT, "queue-timeline.svg"),
    timelineChart({
      title: "スレッド4本のプールに、100ミリ秒のタスクを16個まとめて投入したとき",
      xLabel: "時間（ミリ秒）",
      lanes: rows.map((r) => r.task),
      laneWidth: 80,
      xTicks: [0, 100, 200, 300, 400, 500],
      legendItems: [
        { name: "キューで待っている", slot: 0 },
        { name: "thread-1", slot: 1 },
        { name: "thread-2", slot: 2 },
        { name: "thread-3", slot: 3 },
        { name: "thread-4", slot: 4 },
      ],
      bars: rows.flatMap((r) => [
        ...(r.start - r.sub > 2 ? [{ lane: r.task, start: r.sub - t0, end: r.start - t0, slot: 0 }] : []),
        { lane: r.task, start: r.start - t0, end: r.end - t0, slot: threadSlot(r.thread), label: r.thread.replace("pool-1-", "") },
      ]),
    })
  );
}
