// 実行: npx tsx data/java-io-03/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-03";
const OUT = "site/public/images/java-io-03";

// read-text.txt を、実行ごと（行数ごと）に分けて読む
const runs = readFileSync(join(DATA, "read-text.txt"), "utf-8")
  .split(/^# java ReadText \d+$/m)
  .slice(1)
  .map((block) => {
    const num = (re: RegExp) => Number(block.match(re)![1].replace(/,/g, ""));
    return {
      fileMb: num(/行、([\d.]+) MB/),
      readAllLinesMs: num(/Files\.readAllLines: (\d+) ms/),
      readStringMs: num(/Files\.readString: (\d+) ms/),
      linesMs: num(/Files\.lines: (\d+) ms/),
      bufferedReaderMs: num(/BufferedReader: (\d+) ms/),
      readAllLinesMb: num(/readAllLines が読み込んだデータ: (\d+) MB/),
      readStringMb: num(/readString が読み込んだデータ: (\d+) MB/),
    };
  });
const last = runs[runs.length - 1];
const pts = (f: (r: (typeof runs)[number]) => number): [number, number][] => runs.map((r) => [r.fileMb, f(r)]);

// 1. ファイルの大きさと、読み込んだデータが使うメモリ
writeSvg(
  join(OUT, "memory.svg"),
  lineChart({
    title: "ファイルの大きさと、読み込んだデータが使うメモリ",
    xLabel: "ファイルの大きさ（MB）",
    yLabel: "メモリ（MB）",
    xTicks: [0, 20, 40, 60, 80, 100],
    yTicks: [0, 100, 200, 300, 400],
    rightPad: 200,
    series: [
      { name: "Files.readAllLines", endLabel: `readAllLines ${fmt(last.readAllLinesMb)} MB`, slot: 2, markers: true, points: pts((r) => r.readAllLinesMb) },
      { name: "Files.readString", endLabel: `readString ${fmt(last.readStringMb)} MB`, slot: 3, markers: true, points: pts((r) => r.readStringMb) },
      { name: "ファイルの大きさ（参考）", endLabel: `ファイルの大きさ ${fmt(last.fileMb)} MB`, slot: 1, dashed: true, points: pts((r) => r.fileMb) },
    ],
  })
);

// 2. ファイルの大きさと、読んで合計を求める時間
writeSvg(
  join(OUT, "time.svg"),
  lineChart({
    title: "ファイルの大きさと、全行を読んで合計を求める時間",
    xLabel: "ファイルの大きさ（MB）",
    yLabel: "時間（ミリ秒）",
    xTicks: [0, 20, 40, 60, 80, 100],
    yTicks: [0, 1000, 2000, 3000, 4000, 5000],
    yFormat: (v) => fmt(v),
    rightPad: 200,
    series: [
      { name: "Files.readString", endLabel: `readString ${fmt(last.readStringMs)} ms`, slot: 3, markers: true, points: pts((r) => r.readStringMs) },
      { name: "Files.readAllLines", endLabel: `readAllLines ${fmt(last.readAllLinesMs)} ms`, slot: 2, markers: true, points: pts((r) => r.readAllLinesMs) },
      { name: "Files.lines", endLabel: `lines ${fmt(last.linesMs)} ms`, slot: 4, markers: true, points: pts((r) => r.linesMs) },
      { name: "BufferedReader", endLabel: `BufferedReader ${fmt(last.bufferedReaderMs)} ms`, slot: 1, markers: true, points: pts((r) => r.bufferedReaderMs) },
    ],
  })
);
