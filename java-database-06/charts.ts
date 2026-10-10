// 実行: npx tsx data/java-database-06/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { timelineChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-database-06";
const OUT = "site/public/images/java-database-06";

// OptimisticUpdate の出力(「2.」の部分)から、読んだ値と更新できた行数を取り出す
const text = readFileSync(join(DATA, "output-OptimisticUpdate.txt"), "utf-8").split("2. ")[1];
const read = text.match(/stock=(\d+) version=(\d+)/);
const rowsA = text.match(/Aの更新\s*: (\d+)行/);
const rowsB = text.match(/Bの更新\s*: (\d+)行/);
const reread = text.match(/Bが読み直す\s*: stock=(\d+) version=(\d+)/);
const rowsB2 = text.match(/Bの再更新\s*: (\d+)行/);
if (!read || !rowsA || !rowsB || !reread || !rowsB2) throw new Error("出力から値を読めません");

const lanes = ["トランザクションA", "トランザクションB"];
// 時間の軸は、実際の時間ではなく、操作の順番を表す
writeSvg(
  join(OUT, "conflict.svg"),
  timelineChart({
    title: "同じversionを読んだ2つのトランザクションが、順に更新しようとしたとき",
    xLabel: "操作の順番（実際の時間ではない）",
    lanes,
    laneWidth: 180,
    xTicks: [0, 1, 2, 3, 4, 5, 6],
    xFormat: (v) => String(v),
    legendItems: [
      { name: "読み取り", slot: 1 },
      { name: "更新できた", slot: 3 },
      { name: "競合（0行）", slot: 2 },
    ],
    bars: [
      { lane: lanes[0], start: 0, end: 1, slot: 1, label: `読む v${read[2]}` },
      { lane: lanes[1], start: 1, end: 2, slot: 1, label: `読む v${read[2]}` },
      { lane: lanes[0], start: 2, end: 3, slot: 3, label: `更新 ${rowsA[1]}行` },
      { lane: lanes[1], start: 3, end: 4, slot: 2, label: `更新 ${rowsB[1]}行` },
      { lane: lanes[1], start: 4, end: 5, slot: 1, label: `読む v${reread[2]}` },
      { lane: lanes[1], start: 5, end: 6, slot: 3, label: `更新 ${rowsB2[1]}行` },
    ],
  })
);
