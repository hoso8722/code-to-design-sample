// 実行: npx tsx data/java-database-07/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-database-07";
const OUT = "site/public/images/java-database-07";

// PoolSizing の出力(プールの大きさ,所要時間ms,1秒あたりの検索数)を読む
const rows = readFileSync(join(DATA, "output-PoolSizing.txt"), "utf-8")
  .split("\n")
  .filter((l) => /^\d+,\d+,[\d.]+$/.test(l))
  .map((l) => l.split(","))
  .map(([size, millis, qps]) => ({ size: Number(size), millis: Number(millis), qps: Number(qps) }));
if (rows.length === 0) throw new Error("出力から値を読めません");

writeSvg(
  join(OUT, "pool-size.svg"),
  hBarChart({
    title: "プールの大きさと、1秒あたりに終えた検索の数（160回の検索、アプリケーションの同時処理は32）",
    rows: rows.map((r) => ({ label: `プール ${r.size} 本`, value: r.qps, valueLabel: `${r.qps.toFixed(1)}回`, note: `${r.millis} ms`, slot: 1 })),
    unit: "回／秒",
    labelWidth: 150,
    format: (v) => String(Math.round(v)),
  })
);
