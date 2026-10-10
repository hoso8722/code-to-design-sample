// 実行: npx tsx data/java-database-05/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { timelineChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-database-05";
const OUT = "site/public/images/java-database-05";

// ForUpdateWait の出力のうち、「A.」の部分(ロック待ちの上限なし)を読む
const section = readFileSync(join(DATA, "output-ForUpdateWait.txt"), "utf-8").split("# B.")[0];
const held = section.match(/^t1,commitしてロックを解放,(\d+),(\d+)$/m);
const waited = section.match(/^t2,UPDATEが待たされた,(\d+),(\d+)$/m);
if (!held || !waited) throw new Error("出力から時刻を読めません");

const lanes = ["t1(行をロックして保持)", "t2(同じ行を更新)"];
writeSvg(
  join(OUT, "wait.svg"),
  timelineChart({
    title: "t1が行をロックしている間、t2のUPDATEは待たされる",
    xLabel: "時間（ミリ秒。t1が行をロックした時点が0）",
    lanes,
    laneWidth: 190,
    xTicks: [0, 200, 400, 600, 800, 1000, 1200],
    legendItems: [
      { name: "ロックを保持", slot: 1 },
      { name: "待ち", slot: 0 },
      { name: "更新できた", slot: 2 },
    ],
    bars: [
      { lane: lanes[0], start: Number(held[1]), end: Number(held[2]), slot: 1, label: `ロックを保持 ${held[2]}ms` },
      { lane: lanes[1], start: Number(waited[1]), end: Number(waited[2]), slot: 0, label: `待ち ${Number(waited[2]) - Number(waited[1])}ms` },
      { lane: lanes[1], start: Number(waited[2]), end: Number(waited[2]) + 40, slot: 2 },
    ],
    markers: [{ x: Number(held[2]), label: "t1がcommit" }],
  })
);
