// 実行: npx tsx data/java-exceptions-07/charts.ts
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";
import { readJmhCsv, nsLabel } from "../../scripts/lib/jmh.js";

const DATA = "data/java-exceptions-07";
const OUT = "site/public/images/java-exceptions-07";
const log = Math.log10;

// 1. 失敗を伝える時間（深さごと。両軸とも対数）
{
  const rows = readJmhCsv(join(DATA, "jmh-ThrowCost.csv"));
  const get = (b: string, d: string) => rows.find((r) => r.benchmark === b && r.params.depth === d)!.score;
  const depths = ["1", "10", "100"];
  const line = (b: string): [number, number][] => depths.map((d) => [log(Number(d)), log(get(b, d))]);
  writeSvg(
    join(OUT, "throw-cost.svg"),
    lineChart({
      title: "失敗を呼び出し元に伝える時間（JMH、両軸とも対数目盛り）",
      xLabel: "失敗した場所までの呼び出しの深さ",
      yLabel: "1回あたりの時間",
      xTicks: [0, 1, 2],
      xFormat: (v) => String(10 ** v),
      yTicks: [0, 1, 2, 3, 4, 5],
      yFormat: (v) => nsLabel(10 ** v),
      rightPad: 190,
      series: [
        { name: "例外を投げる", endLabel: `例外を投げる ${nsLabel(get("exception", "100"))}`, slot: 2, markers: true, points: line("exception") },
        { name: "スタックトレースなしの例外", endLabel: `トレースなしの例外 ${nsLabel(get("noTrace", "100"))}`, slot: 3, markers: true, points: line("noTrace") },
        { name: "例外を作るだけ", endLabel: `例外を作るだけ ${nsLabel(get("createOnly", "100"))}`, slot: 4, dashed: true, markers: true, points: line("createOnly") },
        { name: "戻り値で伝える", endLabel: `戻り値 ${nsLabel(get("returnCode", "100"))}`, slot: 1, markers: true, points: line("returnCode") },
      ],
    })
  );

  // 2. 深さ 1 のときの内訳
  writeSvg(
    join(OUT, "throw-cost-depth1.svg"),
    hBarChart({
      title: "深さ1で失敗を伝える時間（JMH）",
      unit: "ナノ秒",
      labelWidth: 230,
      xTicks: [0, 500, 1000, 1500],
      rows: [
        { label: "戻り値で伝える", value: get("returnCode", "1"), valueLabel: `${fmt(get("returnCode", "1"), 1)} ns`, slot: 1 },
        { label: "スタックトレースなしの例外", value: get("noTrace", "1"), valueLabel: `${fmt(get("noTrace", "1"))} ns`, slot: 3 },
        { label: "例外を作るだけ（投げない）", value: get("createOnly", "1"), valueLabel: `${fmt(get("createOnly", "1"))} ns`, slot: 4 },
        { label: "例外を投げる", value: get("exception", "1"), valueLabel: `${fmt(get("exception", "1"))} ns`, slot: 2 },
      ],
    })
  );
}

// 3. 1,000個の文字列を変換する時間（失敗の割合ごと）
{
  const rows = readJmhCsv(join(DATA, "jmh-ParseRate.csv"));
  const get = (b: string, p: string) => rows.find((r) => r.benchmark === b && r.params.failurePercent === p)!.score;
  const percents = ["0", "1", "10", "50"];
  const line = (b: string): [number, number][] => percents.map((p) => [Number(p), get(b, p)]);
  writeSvg(
    join(OUT, "parse-rate.svg"),
    lineChart({
      title: "1,000個の文字列を点数に変換する時間（JMH）",
      xLabel: "数値にできない文字列の割合（%）",
      yLabel: "時間（マイクロ秒）",
      xTicks: [0, 10, 20, 30, 40, 50],
      yTicks: [0, 200, 400, 600, 800, 1000],
      yFormat: (v) => fmt(v),
      rightPad: 190,
      series: [
        { name: "変換を試して例外を捕まえる", endLabel: `例外を捕まえる ${fmt(get("tryAndCatch", "50"))} µs`, slot: 2, markers: true, points: line("tryAndCatch") },
        { name: "先に確かめてから変換する", endLabel: `先に確かめる ${fmt(get("checkFirst", "50"), 1)} µs`, slot: 1, markers: true, points: line("checkFirst") },
      ],
    })
  );
}
