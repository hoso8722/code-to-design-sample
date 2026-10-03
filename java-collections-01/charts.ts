// 実行: npx tsx data/java-collections-01/charts.ts
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg } from "../../scripts/lib/svg-chart.js";
import { nsLabel, readJmhCsv } from "../../scripts/lib/jmh.js";

const DATA = "data/java-collections-01";
const OUT = "site/public/images/java-collections-01";
const ops = readJmhCsv(join(DATA, "jmh-ListOps.csv"));
const score = (b: string, size: number, type: string) =>
  ops.find((r) => r.benchmark === b && r.params.size === String(size) && r.params.type === type)!.score;
const log = Math.log10;

// 1. 要素数 10,000 での操作ごとの時間（横軸は対数）
{
  const names: [string, string][] = [
    ["getMiddle", "get(真ん中)"],
    ["addLast", "末尾に追加・削除"],
    ["addFirst", "先頭に追加・削除"],
    ["addMiddle", "真ん中に追加・削除"],
    ["iterate", "全体を走査"],
  ];
  const rows = names.flatMap(([b, label]) =>
    (["ArrayList", "LinkedList"] as const).map((t) => ({
      label: t,
      value: log(score(b, 10000, t)),
      valueLabel: nsLabel(score(b, 10000, t)),
      slot: t === "ArrayList" ? 1 : 2,
    }))
  );
  const groups = Object.fromEntries(names.map(([, label], i) => [i * 2, label]));
  writeSvg(
    join(OUT, "ops-10000.svg"),
    hBarChart({
      title: "要素数 10,000 のときの、操作1回あたりの時間（JMH、横軸は対数目盛り）",
      unit: "1回あたりの時間",
      labelWidth: 150,
      xTicks: [0, 1, 2, 3, 4, 5],
      format: (v) => nsLabel(10 ** v),
      groups,
      legendItems: [
        { name: "ArrayList", slot: 1 },
        { name: "LinkedList", slot: 2 },
      ],
      rows,
    })
  );
}

// 2. 要素数と時間（get(真ん中) と 真ん中への追加）
{
  const sizes = [1000, 10000, 100000];
  const line = (b: string, t: string, slot: number, name: string, dashed = false) => ({
    name,
    endLabel: `${name} ${nsLabel(score(b, 100000, t))}`,
    slot,
    dashed,
    markers: true,
    points: sizes.map((s) => [log(s), log(score(b, s, t))] as [number, number]),
  });
  writeSvg(
    join(OUT, "scaling.svg"),
    lineChart({
      title: "要素数と、真ん中の要素の get・真ん中への追加の時間（JMH、両軸とも対数）",
      xLabel: "要素数",
      yLabel: "1回あたりの時間",
      xTicks: [3, 4, 5].map((v) => v),
      xFormat: (v) => (10 ** v).toLocaleString("en-US"),
      yTicks: [0, 1, 2, 3, 4, 5, 6],
      yFormat: (v) => nsLabel(10 ** v),
      rightPad: 190,
      series: [
        line("addMiddle", "LinkedList", 2, "LinkedList 追加"),
        line("getMiddle", "LinkedList", 2, "LinkedList get", true),
        line("addMiddle", "ArrayList", 1, "ArrayList 追加"),
        line("getMiddle", "ArrayList", 1, "ArrayList get", true),
      ],
    })
  );
}

// 3. 待ち行列（末尾に入れて先頭から出す）
{
  const q = readJmhCsv(join(DATA, "jmh-QueueOps.csv"));
  const s = (b: string) => q.find((r) => r.benchmark === b)!.score;
  writeSvg(
    join(OUT, "queue.svg"),
    hBarChart({
      title: "1万件の待ち行列で、末尾に入れて先頭から取り出す1回の時間（JMH、横軸は対数目盛り）",
      unit: "1回あたりの時間",
      labelWidth: 160,
      xTicks: [0, 1, 2, 3, 4],
      format: (v) => nsLabel(10 ** v),
      rows: [
        { label: "ArrayDeque", value: log(s("arrayDeque")), valueLabel: nsLabel(s("arrayDeque")), slot: 1 },
        { label: "LinkedList", value: log(s("linkedList")), valueLabel: nsLabel(s("linkedList")), slot: 2 },
        { label: "ArrayList", value: log(s("arrayList")), valueLabel: nsLabel(s("arrayList")), slot: 3 },
      ],
    })
  );
}
