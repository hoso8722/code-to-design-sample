// 実行: npx tsx data/java-async-01/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { lineChart, timelineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-01";
const OUT = "site/public/images/java-async-01";

// 1. 待つ処理：順番に実行 と 8本のスレッドで同時に実行
{
  const text = readFileSync(join(DATA, "wait-tasks.txt"), "utf-8");
  const rows = text.split("\n").filter((l) => /^(sequential|concurrent),/.test(l)).map((l) => {
    const [mode, task, thread, start, end] = l.split(",");
    return { mode, task, thread, start: Number(start), end: Number(end) };
  });
  const t0 = Math.min(...rows.map((r) => r.start));
  const seq = rows.filter((r) => r.mode === "sequential");
  const con = rows.filter((r) => r.mode === "concurrent");
  const conStart = Math.min(...con.map((r) => r.start));
  const lanes = ["main", ...con.map((r) => r.thread)];
  writeSvg(
    join(OUT, "wait-timeline.svg"),
    timelineChart({
      title: "200ミリ秒待つ処理を8回：順番に実行（main）と、8本のスレッドで同時に実行",
      xLabel: "時間（ミリ秒）",
      lanes,
      xTicks: [0, 400, 800, 1200, 1600, 2000],
      legendItems: [
        { name: "順番に実行", slot: 1 },
        { name: "同時に実行", slot: 2 },
      ],
      bars: [
        ...seq.map((r) => ({ lane: "main", start: r.start - t0, end: r.end - t0, slot: 1, label: r.task.replace("task-", "") })),
        ...con.map((r) => ({ lane: r.thread, start: r.start - t0, end: r.end - t0, slot: 2, label: r.task })),
      ],
      markers: [{ x: conStart - t0, label: "ここから同時に実行" }],
    })
  );
}

// 2. CPUを使う処理：CPU 1つ（taskset -c 0）と、制限なし
for (const [file, title, out] of [
  ["cpu-timeline-1cpu.txt", "CPUを使うタスク4つを4本のスレッドで：CPUを1つに制限した場合", "cpu-timeline-1cpu.svg"],
  ["cpu-timeline-all.txt", "CPUを使うタスク4つを4本のスレッドで：CPUの制限なし（16）", "cpu-timeline-all.svg"],
] as const) {
  const rows = readFileSync(join(DATA, file), "utf-8").split("\n").filter((l) => l.startsWith("worker-")).map((l) => {
    const [thread, start, end] = l.split(",");
    return { thread, start: Number(start), end: Number(end) };
  });
  writeSvg(
    join(OUT, out),
    timelineChart({
      title,
      xLabel: "時間（ミリ秒）",
      lanes: rows.map((r) => r.thread),
      xTicks: [0, 50, 100, 150, 200, 250],
      bars: rows.map((r) => ({ lane: r.thread, start: r.start, end: r.end, slot: 3, label: `${r.end - r.start} ms` })),
    })
  );
}

// 3. スレッドの数と速さ（1本のときの何倍速くなったか。横軸は 2 を底とした対数）
{
  const [, ...lines] = readFileSync(join(DATA, "scale.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([kind, n, ms]) => ({ kind, n: Number(n), ms: Number(ms) }));
  const mean = (kind: string, n: number) => {
    const xs = rows.filter((r) => r.kind === kind && r.n === n).map((r) => r.ms);
    return xs.reduce((a, b) => a + b, 0) / xs.length;
  };
  const counts = [1, 2, 4, 8, 16, 32];
  const speedup = (kind: string) => counts.map((n) => [Math.log2(n), mean(kind, 1) / mean(kind, n)] as [number, number]);
  writeSvg(
    join(OUT, "scale.svg"),
    lineChart({
      title: "32個のタスクを、スレッドの数を変えて実行したときの速さ（1本のときの何倍か）",
      xLabel: "スレッドの数",
      yLabel: "1本のときの何倍速いか",
      xTicks: [0, 1, 2, 3, 4, 5],
      xFormat: (v) => fmt(2 ** v),
      yTicks: [0, 8, 16, 24, 32],
      rightPad: 190,
      series: [
        { name: "理想（スレッドの数と同じ倍率）", endLabel: "理想", slot: 3, dashed: true, points: counts.map((n) => [Math.log2(n), n]) },
        { name: "待つ処理（100ミリ秒 × 32）", endLabel: `待つ処理 ${fmt(speedup("wait")[5][1], 1)}倍`, slot: 1, markers: true, points: speedup("wait") },
        { name: "CPUを使う処理（約50ミリ秒 × 32）", endLabel: `CPU ${fmt(speedup("cpu")[5][1], 1)}倍`, slot: 2, markers: true, points: speedup("cpu") },
      ],
    })
  );
}
