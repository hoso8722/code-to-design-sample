// 実行: npx tsx data/java-collections-04/charts.ts
import { join } from "node:path";
import { hBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";
import { readJmhCsv } from "../../scripts/lib/jmh.js";

const DATA = "data/java-collections-04";
const OUT = "site/public/images/java-collections-04";
const types: [string, number][] = [["HashMap", 1], ["LinkedHashMap", 3], ["TreeMap", 2]];

// 1. get の時間（要素数 1,000 と 100,000）
{
  const rows = readJmhCsv(join(DATA, "jmh-MapOps.csv"));
  const s = (b: string, n: string, t: string) => rows.find((r) => r.benchmark === b && r.params.n === n && r.params.type === t)!.score;
  writeSvg(
    join(OUT, "map-get.svg"),
    hBarChart({
      title: "既存のキーで get する1回の時間（JMH）",
      unit: "ナノ秒",
      labelWidth: 150,
      xTicks: [0, 100, 200, 300, 400],
      groups: { 0: "要素数 1,000", 3: "要素数 100,000" },
      legendItems: types.map(([name, slot]) => ({ name, slot })),
      rows: ["1000", "100000"].flatMap((n) =>
        types.map(([t, slot]) => ({ label: t, value: s("get", n, t), valueLabel: `${fmt(s("get", n, t), 1)} ns`, slot }))
      ),
    })
  );
}

// 2. 範囲のキーを数える
{
  const rows = readJmhCsv(join(DATA, "jmh-RangeCount.csv"));
  const s = (b: string) => rows.find((r) => r.benchmark === b)!.score;
  writeSvg(
    join(OUT, "range-count.svg"),
    hBarChart({
      title: "10万個のキーから「50,000以上51,000未満」のキーを数える時間（JMH）",
      unit: "マイクロ秒",
      labelWidth: 250,
      xTicks: [0, 200, 400, 600, 800, 1000],
      rows: [
        { label: "TreeMap の subMap(...).size()", value: s("treeMapSubMap"), valueLabel: `${fmt(s("treeMapSubMap"), 1)} µs`, slot: 2 },
        { label: "HashMap のキーをすべて調べる", value: s("hashMapScan"), valueLabel: `${fmt(s("hashMapScan"))} µs`, slot: 1 },
      ],
    })
  );
}
