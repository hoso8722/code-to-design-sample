// 実行: npx tsx data/java-io-02/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-02";
const OUT = "site/public/images/java-io-02";

// 「Aさん,数学80点,合格」（12文字）を、それぞれの文字コードで表したときのバイト数
{
  const text = readFileSync(join(DATA, "Bytes.txt"), "utf-8");
  const section = text.slice(text.indexOf("「Aさん,数学80点,合格」"));
  const rows = [...section.matchAll(/^\s+(\S+)\s+(\d+)バイト$/gm)].map((m, i) => ({
    label: m[1] === "windows-31j" ? "MS932（windows-31j）" : m[1],
    value: Number(m[2]),
    valueLabel: `${m[2]}バイト`,
    slot: i + 1,
  }));
  writeSvg(
    join(OUT, "bytes.svg"),
    hBarChart({
      title: "「Aさん,数学80点,合格」（12文字）のバイト数",
      unit: "バイト",
      labelWidth: 190,
      xTicks: [0, 5, 10, 15, 20, 25, 30],
      rows,
    })
  );
}
