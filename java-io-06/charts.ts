// 実行: npx tsx data/java-io-06/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-06";
const OUT = "site/public/images/java-io-06";

// 1,000,000バイトを1回で write したときに、1回の read が返したバイト数
{
  const text = readFileSync(join(DATA, "PartialRead.txt"), "utf-8");
  const sizes = text.trim().split("\n").pop()!.replace(/[\[\]\s]/g, "").split(",").map(Number);
  writeSvg(
    join(OUT, "read-sizes.svg"),
    hBarChart({
      title: "1,000,000バイトを1回で write したときに、1回の read が返したバイト数",
      unit: "バイト",
      labelWidth: 120,
      xTicks: [0, 50000, 100000, 150000],
      format: (v) => fmt(v),
      rows: sizes.map((s, i) => ({ label: `${i + 1}回目の read`, value: s, valueLabel: `${fmt(s)}バイト`, slot: 1 })),
    })
  );
}
