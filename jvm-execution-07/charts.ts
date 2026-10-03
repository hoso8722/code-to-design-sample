// 実行: npx tsx data/jvm-execution-07/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, stackedBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/jvm-execution-07";
const OUT = "site/public/images/jvm-execution-07";
const modes: [string, string, number][] = [
  ["noshare", "CDSなし（-Xshare:off）", 3],
  ["default", "デフォルトのCDS", 2],
  ["aot", "AOTキャッシュ", 1],
];

// 1. 起動方法ごとの時間（1回目を除く10回の平均）
{
  const [, ...lines] = readFileSync(join(DATA, "startup.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).filter(([, run]) => run !== "1");
  const mean = (m: string, col: number) => {
    const xs = rows.filter(([mode]) => mode === m).map((r) => Number(r[col]));
    return xs.reduce((a, b) => a + b, 0) / xs.length;
  };
  writeSvg(
    join(OUT, "startup.svg"),
    hBarChart({
      title: "StartupApp の起動方法ごとの時間（10回の平均）",
      unit: "ミリ秒",
      labelWidth: 200,
      xTicks: [0, 200, 400, 600, 800],
      groups: { 0: "プロセスの開始から終了まで", 3: "main の処理（main の中で計測）" },
      legendItems: modes.map(([, name, slot]) => ({ name, slot })),
      rows: [
        ...modes.map(([m, name, slot]) => ({ label: name, value: mean(m, 2), valueLabel: `${fmt(mean(m, 2))} ms`, slot })),
        ...modes.map(([m, name, slot]) => ({ label: name, value: mean(m, 3), valueLabel: `${fmt(mean(m, 3))} ms`, slot })),
      ],
    })
  );
}

// 2. 読み込まれたクラスの読み込み元
{
  const lines = readFileSync(join(DATA, "class-load.csv"), "utf-8").trim().split("\n");
  const rows = new Map(lines.map((l) => l.split(",")).map(([m, ...v]) => [m, v.map(Number)]));
  writeSvg(
    join(OUT, "class-sources.svg"),
    stackedBarChart({
      title: "読み込まれたクラスの数と、読み込み元",
      unit: "個",
      labelWidth: 200,
      segments: [
        { name: "共有アーカイブ", slot: 1 },
        { name: "JDKのモジュール", slot: 2 },
        { name: "実行中に生成", slot: 3 },
        { name: "アプリのJAR", slot: 4 },
      ],
      rows: modes.map(([m, name]) => {
        const [total, cds, jrt, app, gen] = rows.get(m)!;
        return { label: name, values: [cds, jrt, gen, app], total: `${fmt(total)} 個` };
      }),
    })
  );
}
