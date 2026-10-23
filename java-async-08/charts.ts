// 実行: npx tsx data/java-async-08/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, timelineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-08";
const OUT = "site/public/images/java-async-08";

// 1. プールと仮想スレッドの処理時間
{
  const [, ...lines] = readFileSync(join(DATA, "many-waits.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([k, w, , ms]) => ({ k, w, ms: Number(ms) }));
  const mean = (k: string, w: string) => {
    const xs = rows.filter((r) => r.k === k && r.w === w).map((r) => r.ms);
    return xs.reduce((a, b) => a + b, 0) / xs.length / 1000;
  };
  const bar = (label: string, k: string, w: string, slot: number) => ({ label, value: mean(k, w), valueLabel: `${fmt(mean(k, w), 2)} 秒`, slot });
  writeSvg(
    join(OUT, "elapsed.svg"),
    hBarChart({
      title: "スレッドプールと仮想スレッドの処理時間（3回の平均）",
      unit: "秒",
      labelWidth: 250,
      xTicks: [0, 3, 6, 9, 12],
      groups: { 0: "100ミリ秒待つタスク × 10,000", 3: "CPUを使うタスク（約50ミリ秒）× 64" },
      legendItems: [
        { name: "プラットフォームスレッドのプール", slot: 2 },
        { name: "仮想スレッド", slot: 1 },
      ],
      rows: [
        bar("newFixedThreadPool(100)", "fixed100", "wait", 2),
        bar("newFixedThreadPool(1000)", "fixed1000", "wait", 2),
        bar("newVirtualThreadPerTaskExecutor", "virtual", "wait", 1),
        bar("newFixedThreadPool(16)", "fixed16", "cpu", 2),
        bar("newVirtualThreadPerTaskExecutor", "virtual", "cpu", 1),
      ],
    })
  );
}

// 2. synchronized の中で待つ仮想スレッド（JDK 21）
for (const v of ["21"]) {
  const lines = readFileSync(join(DATA, `pinning-jdk-${v}.csv`), "utf-8").split("\n").filter((l) => l.startsWith("task-"));
  const rows = lines.map((l) => l.split(",")).map(([task, carrier, s, e]) => ({ task, carrier: carrier.replace("ForkJoinPool-1-", ""), start: Number(s), end: Number(e) }));
  const t0 = Math.min(...rows.map((r) => r.start));
  const carriers = [...new Set(rows.map((r) => r.carrier))].sort((a, b) => Number(a.split("-")[1]) - Number(b.split("-")[1]));
  // レーンの名前に、そのキャリアで始まったタスクの数を付ける（同じ時刻に重なった棒は1本に見えるため）
  const laneOf = (c: string) => `${c}（${rows.filter((r) => r.carrier === c).length}個）`;
  const lanes = carriers.map(laneOf);
  const total = Math.max(...rows.map((r) => r.end)) - t0;
  writeSvg(
    join(OUT, `pinning-jdk${v}.svg`),
    timelineChart({
      title: `JDK ${v}：synchronized の中で100ミリ秒待つタスク64個（キャリアスレッドごと）`,
      xLabel: "時間（ミリ秒）",
      lanes,
      laneWidth: 120,
      xTicks: [0, 100, 200, 300, 400, 500],
      bars: rows.map((r) => ({ lane: laneOf(r.carrier), start: r.start - t0, end: r.end - t0, slot: v === "21" ? 2 : 1 })),
      markers: [{ x: total, label: `すべて終わる（${total} ms）` }],
    })
  );
}
