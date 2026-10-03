// 実行: npx tsx data/java-async-02/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, timelineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-02";
const OUT = "site/public/images/java-async-02";

// 1. run() と start() のタイムライン
{
  const [, ...lines] = readFileSync(join(DATA, "run-vs-start.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([task, thread, s, e]) => ({ task, thread, start: Number(s), end: Number(e) }));
  const t0 = Math.min(...rows.map((r) => r.start));
  const lanes = [...new Set(rows.map((r) => r.thread))];
  writeSvg(
    join(OUT, "run-vs-start.svg"),
    timelineChart({
      title: "100ミリ秒待つタスクを3つ：run() を呼んだ場合と start() した場合",
      xLabel: "時間（ミリ秒）",
      lanes,
      xTicks: [0, 100, 200, 300, 400, 500],
      legendItems: [
        { name: "run() を直接呼ぶ", slot: 1 },
        { name: "start() する", slot: 2 },
      ],
      bars: rows.map((r) => ({ lane: r.thread, start: r.start - t0, end: r.end - t0, slot: r.task.startsWith("run") ? 1 : 2, label: r.task })),
    })
  );
}

// 2. スレッドを作るコスト
{
  const text = readFileSync(join(DATA, "thread-cost.txt"), "utf-8");
  const thread = [...text.matchAll(/1本あたり ([\d.]+) マイクロ秒/g)].map((m) => Number(m[1]));
  const direct = [...text.matchAll(/1回あたり ([\d.]+) マイクロ秒/g)].map((m) => Number(m[1]));
  const avg = (xs: number[]) => xs.reduce((a, b) => a + b, 0) / xs.length;
  writeSvg(
    join(OUT, "thread-cost.svg"),
    hBarChart({
      title: "何もしない処理を1回実行するのにかかる時間（3回の平均）",
      unit: "マイクロ秒",
      labelWidth: 250,
      xTicks: [0, 100, 200, 300, 400, 500],
      rows: [
        { label: "スレッドを作って start() → join()", value: avg(thread), valueLabel: `${fmt(avg(thread), 1)} µs`, slot: 2 },
        { label: "メソッドとして呼ぶ（run()）", value: avg(direct), valueLabel: `${fmt(avg(direct), 3)} µs`, slot: 1 },
      ],
    })
  );
}
