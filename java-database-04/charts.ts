// 実行: npx tsx data/java-database-04/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { timelineChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-database-04";
const OUT = "site/public/images/java-database-04";

// ReadTwice の出力(READ COMMITTED と REPEATABLE READ の行)から、t1が1回目と2回目に読んだ残高を取り出す
const text = readFileSync(join(DATA, "output-ReadTwice.txt"), "utf-8");
function readLine(level: string): { first: string; second: string } {
  const m = text.match(new RegExp(`^${level}\\s+残高 ([\\d.]+) -> ([\\d.]+)`, "m"));
  if (!m) throw new Error(`出力に ${level} の行がありません`);
  return { first: m[1], second: m[2] };
}

const levels = [
  { name: "READ COMMITTED", ...readLine("READ COMMITTED") },
  { name: "REPEATABLE READ", ...readLine("REPEATABLE READ") },
];
const yen = (v: string) => String(Math.round(Number(v)));

// 時間の軸は、実際の秒数ではなく、操作の順番(1〜4)を表す
const lanes: string[] = [];
const bars: { lane: string; start: number; end: number; slot: number; label: string }[] = [];
for (const level of levels) {
  const t1 = `${level.name} の t1`;
  const t2 = `${level.name} の t2`;
  lanes.push(t1, t2);
  bars.push(
    { lane: t1, start: 0, end: 1, slot: 1, label: `読む ${yen(level.first)}` },
    { lane: t2, start: 1, end: 2, slot: 2, label: "+1000円 commit" },
    { lane: t1, start: 2, end: 3, slot: 1, label: `読む ${yen(level.second)}` },
    { lane: t1, start: 3, end: 4, slot: 3, label: "commit" }
  );
}

writeSvg(
  join(OUT, "timeline.svg"),
  timelineChart({
    title: "t1が同じ行を2回読む間に、t2が更新してcommitしたとき",
    xLabel: "操作の順番（実際の時間ではない）",
    lanes,
    laneWidth: 190,
    xTicks: [0, 1, 2, 3, 4],
    xFormat: (v) => String(v),
    legendItems: [
      { name: "読み取り", slot: 1 },
      { name: "更新してcommit", slot: 2 },
      { name: "commit", slot: 3 },
    ],
    bars,
  })
);
