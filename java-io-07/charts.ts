// 実行: npx tsx data/java-io-07/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-07";
const OUT = "site/public/images/java-io-07";

// 1. 200回の要求を送る時間（3回目の値）
{
  const line = readFileSync(join(DATA, "Reuse.txt"), "utf-8").split("\n").find((l) => l.startsWith("3回目"))!;
  const m = line.match(/毎回作る (\d+) ms（接続 (\d+) 本）、使い回す (\d+) ms（接続 (\d+) 本）/)!;
  writeSvg(
    join(OUT, "reuse.svg"),
    hBarChart({
      title: "200回の要求を順に送る時間",
      unit: "ミリ秒",
      labelWidth: 280,
      xTicks: [0, 200, 400, 600, 800],
      rows: [
        { label: `要求のたびに HttpClient を作る（接続${m[2]}本）`, value: Number(m[1]), valueLabel: `${fmt(Number(m[1]))} ms`, slot: 2 },
        { label: `1つの HttpClient を使い回す（接続${m[4]}本）`, value: Number(m[3]), valueLabel: `${fmt(Number(m[3]))} ms`, slot: 1 },
      ],
    })
  );
}

// 2. 応答に100ミリ秒かかるサーバーに、20回の要求を送る時間（3回目の値）
{
  const line = readFileSync(join(DATA, "Async.txt"), "utf-8").split("\n").find((l) => l.startsWith("3回目"))!;
  const m = line.match(/send (\d+) ms、sendAsync (\d+) ms/)!;
  writeSvg(
    join(OUT, "async.svg"),
    hBarChart({
      title: "応答に100ミリ秒かかるサーバーに、20回の要求を送る時間",
      unit: "ミリ秒",
      labelWidth: 250,
      xTicks: [0, 500, 1000, 1500, 2000, 2500],
      rows: [
        { label: "send（応答を待ってから次を送る）", value: Number(m[1]), valueLabel: `${fmt(Number(m[1]))} ms`, slot: 2 },
        { label: "sendAsync（待たずにすべて送る）", value: Number(m[2]), valueLabel: `${fmt(Number(m[2]))} ms`, slot: 1 },
      ],
    })
  );
}
