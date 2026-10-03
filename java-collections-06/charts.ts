// 実行: npx tsx data/java-collections-06/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-collections-06";
const OUT = "site/public/images/java-collections-06";
const [, ...lines] = readFileSync(join(DATA, "list-size.csv"), "utf-8").trim().split("\n");
const rows = lines.map((l) => l.split(",")).map(([way, size, bytes]) => ({ way, size: Number(size), bytes: Number(bytes) }));
const ways: [string, number][] = [
  ["new ArrayList<>(src)", 3],
  ["collect(toList())", 4],
  ["unmodifiableList(src)", 5],
  ["stream().toList()", 2],
  ["List.copyOf(src)", 1],
];
const sizes = [0, 1, 2, 10];
const groups = Object.fromEntries(sizes.map((s, i) => [i * ways.length, `要素数 ${s}`]));
writeSvg(
  join(OUT, "list-size.svg"),
  hBarChart({
    title: "リスト1つが保持するバイト数（要素そのものは除く）",
    unit: "バイト",
    labelWidth: 190,
    xTicks: [0, 25, 50, 75, 100, 125],
    groups,
    rows: sizes.flatMap((s) =>
      ways.map(([w, slot]) => {
        const b = rows.find((r) => r.way === w && r.size === s)!.bytes;
        return { label: w, value: b, valueLabel: `${fmt(Math.round(b))} B`, slot };
      })
    ),
  })
);
