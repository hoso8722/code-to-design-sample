// 実行: npx tsx data/java-io-05/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-05";
const OUT = "site/public/images/java-io-05";

// 1. ファイルの大きさと、コピーの時間（1回目の実行）
{
  const first = readFileSync(join(DATA, "copy.txt"), "utf-8").split("# 2回目")[0];
  const rows = first.split("\n").filter((l) => /^\d+,/.test(l)).map((l) => l.split(",").map(Number));
  const col = (i: number): [number, number][] => rows.map((r) => [r[0], r[i]]);
  const last = rows[rows.length - 1];
  writeSvg(
    join(OUT, "copy.svg"),
    lineChart({
      title: "ファイルの大きさと、コピーにかかる時間",
      xLabel: "ファイルの大きさ（MB）",
      yLabel: "時間（ミリ秒）",
      xTicks: [0, 64, 128, 192, 256],
      yTicks: [0, 250, 500, 750, 1000, 1250],
      yFormat: (v) => fmt(v),
      rightPad: 200,
      series: [
        { name: "ストリームのループ", endLabel: `ストリーム ${fmt(last[1])} ms`, slot: 2, markers: true, points: col(1) },
        { name: "メモリマップ", endLabel: `メモリマップ ${fmt(last[4])} ms`, slot: 4, markers: true, points: col(4) },
        { name: "transferTo", endLabel: `transferTo ${fmt(last[3])} ms`, slot: 3, markers: true, points: col(3) },
        { name: "Files.copy", endLabel: `Files.copy ${fmt(last[2])} ms`, slot: 1, markers: true, points: col(2) },
      ],
    })
  );
}

// 2. 64MB のファイルから、ばらばらの位置の8バイトを100万回読む時間（1回目の実行）
{
  const first = readFileSync(join(DATA, "random-read.txt"), "utf-8").split("# 2回目")[0];
  const channel = Number(first.match(/チャネルの read: ([\d.]+) ms/)![1]);
  const mapped = Number(first.match(/メモリマップ: ([\d.]+) ms/)![1]);
  writeSvg(
    join(OUT, "random-read.svg"),
    hBarChart({
      title: "ばらばらの位置の8バイトを、100万回読む時間",
      unit: "ミリ秒",
      labelWidth: 200,
      xTicks: [0, 500, 1000, 1500, 2000],
      rows: [
        { label: "チャネルの read", value: channel, valueLabel: `${fmt(channel, 1)} ms`, slot: 2 },
        { label: "メモリマップ", value: mapped, valueLabel: `${fmt(mapped, 1)} ms`, slot: 1 },
      ],
    })
  );
}
