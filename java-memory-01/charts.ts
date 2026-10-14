// 実行: npx tsx data/java-memory-01/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, stackedBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";
import { parseNmt, readCsv } from "../../scripts/lib/nmt.js";

const DATA = "data/java-memory-01";
const OUT = "site/public/images/java-memory-01";
const mb = (kb: number) => kb / 1024;

// 1. 起動直後の3つの量（プロセス全体とヒープ）
const idle = parseNmt(readFileSync(join(DATA, "nmt-hold-0.txt"), "utf-8"));
const heap = idle.categories.get("Java Heap")!;
writeSvg(
  join(OUT, "three-sizes.svg"),
  hBarChart({
    title: "何もしていないJavaプロセスの「メモリの大きさ」",
    unit: "MB",
    labelWidth: 230,
    groups: { 0: "プロセス全体（NMT と /proc）", 3: "そのうちヒープ（NMT の Java Heap）" },
    rows: [
      { label: "reserved（予約）", value: mb(idle.totalReservedKB), slot: 1 },
      { label: "committed（確保）", value: mb(idle.totalCommittedKB), slot: 1 },
      { label: "RSS（物理メモリ）", value: mb(idle.rssKB!), slot: 1 },
      { label: "reserved（= 上限 -Xmx）", value: mb(heap.reservedKB), slot: 2 },
      { label: "committed", value: mb(heap.committedKB), slot: 2 },
    ],
    format: (v) => fmt(v),
    legendItems: [
      { name: "プロセス全体", slot: 1 },
      { name: "ヒープ", slot: 2 },
    ],
  })
);

// 2. 確保量を増やしたときの used / committed / RSS
const steps = readCsv(readFileSync(join(DATA, "alloc-steps.csv"), "utf-8"));
writeSvg(
  join(OUT, "alloc-steps.svg"),
  lineChart({
    title: "保持するデータを16MBずつ増やしたときのヒープとRSS",
    xLabel: "プログラムが保持しているデータ（MB）",
    yLabel: "MB",
    xTicks: [0, 128, 256, 384, 512],
    yMax: 800,
    series: [
      { name: "ヒープ committed", slot: 2, step: true, points: steps.map((r) => [r.allocated_mb, r.committed_mb]) },
      { name: "RSS", slot: 1, points: steps.map((r) => [r.allocated_mb, r.rss_mb]) },
      { name: "ヒープ used", slot: 3, points: steps.map((r) => [r.allocated_mb, r.used_mb]) },
    ],
  })
);

// 3. NMT の committed の内訳
const hold = parseNmt(readFileSync(join(DATA, "nmt-hold-256.txt"), "utf-8"));
const names = ["Java Heap", "GC", "Shared class space", "Code"];
const breakdown = (s: typeof idle) => {
  const main = names.map((n) => mb(s.categories.get(n)?.committedKB ?? 0));
  const rest = mb(s.totalCommittedKB) - main.reduce((a, b) => a + b, 0);
  return [...main, rest];
};
writeSvg(
  join(OUT, "nmt-breakdown.svg"),
  stackedBarChart({
    title: "NMT で見た committed の内訳",
    unit: "MB",
    labelWidth: 170,
    segments: [
      { name: "Java Heap", slot: 1 },
      { name: "GC", slot: 2 },
      { name: "Shared class space", slot: 3 },
      { name: "Code", slot: 4 },
      { name: "その他", slot: 5 },
    ],
    rows: [
      { label: "何も保持しない", note: `RSS ${fmt(mb(idle.rssKB!))} MB`, values: breakdown(idle) },
      { label: "256MB 保持", note: `RSS ${fmt(mb(hold.rssKB!))} MB`, values: breakdown(hold) },
    ],
  })
);
