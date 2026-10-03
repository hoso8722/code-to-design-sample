// 実行: npx tsx data/java-memory-08/charts.ts
// pauses.csv は python3 parse-safepoints.py safepoint、stalls.txt は GC ログの Allocation Stall の合計（data/java-memory-08 で実行）
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, stackedBarChart, svgDoc, writeSvg, esc, fmt, textWidth } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-memory-08";
const OUT = "site/public/images/java-memory-08";

const csv = (file: string) => {
  const [header, ...lines] = readFileSync(join(DATA, file), "utf-8").trim().split("\n");
  const keys = header.split(",");
  return lines.map((l) => Object.fromEntries(l.split(",").map((v, i) => [keys[i], v])));
};
const avg = (xs: number[]) => xs.reduce((a, b) => a + b, 0) / xs.length;

const gcs = [
  { id: "serial", label: "Serial" },
  { id: "parallel", label: "Parallel" },
  { id: "g1", label: "G1" },
  { id: "zgc", label: "ZGC" },
  { id: "zgc-gen", label: "ZGC（世代別）" },
];
const bench = csv("gc-bench.csv");
const pauses = csv("pauses.csv");
const stallText = readFileSync(join(DATA, "stalls.txt"), "utf-8");
export const summary = gcs.map((g) => {
  const elapsed = avg(bench.filter((r) => r.gc === g.id).map((r) => Number(r.elapsed_ms)));
  const p = pauses.filter((r) => r.gc === g.id);
  const pauseTotal = avg(p.map((r) => Number(r.total_ms)));
  const pauseMax = avg(p.map((r) => Number(r.max_ms)));
  const stalls = [...stallText.matchAll(new RegExp(`^${g.id}-\\d stall_total_ms=(\\d+)`, "gm"))].map((m) => Number(m[1]));
  const stall = stalls.length ? avg(stalls) : 0;
  return { ...g, elapsed, pauseTotal, pauseMax, stall };
});

// 1. 処理時間の内訳（秒）
writeSvg(
  join(OUT, "gc-elapsed.svg"),
  stackedBarChart({
    title: "同じ処理にかかった時間とその内訳（ヒープ1GB・CPU 4）",
    unit: "秒",
    labelWidth: 130,
    format: (v) => fmt(v),
    segments: [
      { name: "GCの停止", slot: 2 },
      { name: "確保待ち（Allocation Stall）", slot: 4 },
      { name: "それ以外", slot: 1 },
    ],
    rows: summary.map((s) => ({
      label: s.label,
      values: [s.pauseTotal / 1000, s.stall / 1000, (s.elapsed - s.pauseTotal - s.stall) / 1000],
      total: `${fmt(s.elapsed / 1000, 1)} 秒`,
    })),
  })
);

// 2. 最大の停止時間（ミリ秒）
writeSvg(
  join(OUT, "gc-max-pause.svg"),
  hBarChart({
    title: "1回の停止の最大（3回の平均）",
    unit: "ミリ秒",
    labelWidth: 130,
    xTicks: [0, 100, 200, 300, 400, 500],
    rows: summary.map((s) => ({ label: s.label, value: s.pauseMax, valueLabel: s.pauseMax < 1 ? `${fmt(s.pauseMax, 2)} ms` : `${fmt(s.pauseMax)} ms`, slot: 1 })),
  })
);

// 3. コンテナのメモリ制限とヒープの上限
{
  const rows = csv("container-heap.csv").filter((r) => r.cpus === "2");
  const toMB = (m: string) => (m.endsWith("g") ? Number(m.slice(0, -1)) * 1024 : Number(m.slice(0, -1)));
  const series = (opt: string) => rows.filter((r) => r.option === opt).map((r) => [toMB(r.memory), Number(r.max_heap_mb)] as [number, number]);
  writeSvg(
    join(OUT, "container-heap.svg"),
    lineChart({
      title: "コンテナのメモリ制限と、ヒープの上限（MaxHeapSize）",
      xLabel: "コンテナのメモリ制限（MB）",
      yLabel: "ヒープの上限（MB）",
      xTicks: [0, 1024, 2048, 3072, 4096],
      yTicks: [0, 1024, 2048, 3072, 4096],
      rightPad: 200,
      series: [
        { name: "メモリ制限そのもの", endLabel: "制限そのもの（100%）", slot: 3, dashed: true, points: [[256, 256], [4096, 4096]] },
        { name: "-XX:MaxRAMPercentage=75", endLabel: "MaxRAMPercentage=75", slot: 2, markers: true, points: series("-XX:MaxRAMPercentage=75") },
        { name: "指定なし（デフォルト）", endLabel: "デフォルト（25%）", slot: 1, markers: true, points: series("default") },
      ],
    })
  );
}

// 4. コンテナのメモリの配分
{
  const parts: string[] = [];
  const title = "コンテナのメモリ制限の中身（模式図）";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const segs = [
    { w: 0.5, label: "ヒープ（-Xmx）", sub: "MaxRAMPercentage で決める", cls: "f1" },
    { w: 0.06, label: "", sub: "", cls: "f2" },
    { w: 0.06, label: "", sub: "", cls: "f3" },
    { w: 0.08, label: "", sub: "", cls: "f4" },
    { w: 0.06, label: "", sub: "", cls: "f5" },
    { w: 0.24, label: "余裕", sub: "", cls: "box" },
  ];
  const x0 = 0, width = 720, y = 50, h = 56;
  let x = x0;
  for (const s of segs) {
    const w = s.w * width;
    parts.push(`<rect x="${x + 1}" y="${y}" width="${w - 2}" height="${h}" rx="4" class="${s.cls}"/>`);
    if (s.label) parts.push(`<text x="${x + 12}" y="${y + 24}" class="bold" style="fill:${s.cls === "box" ? "var(--ink)" : "#fff"}">${esc(s.label)}</text>`);
    if (s.sub) parts.push(`<text x="${x + 12}" y="${y + 43}" class="small" style="fill:#fff">${esc(s.sub)}</text>`);
    x += w;
  }
  const notes = [
    ["f2", "Metaspace（クラスの情報）", "#06"],
    ["f3", "スレッドのスタック", "#07"],
    ["f4", "Direct Buffer など", "#06"],
    ["f5", "GC・JIT・JVM本体など", "#01"],
  ];
  notes.forEach(([cls, label, ref], i) => {
    const nx = (i % 2) * 360, ny = 140 + Math.floor(i / 2) * 28;
    parts.push(`<rect x="${nx}" y="${ny - 11}" width="12" height="12" rx="2" class="${cls}"/>`);
    parts.push(`<text x="${nx + 18}" y="${ny}" class="ink">${esc(label)}</text><text x="${nx + 18 + textWidth(label) + 8}" y="${ny}" class="muted">${esc(ref)}</text>`);
  });
  parts.push(`<text x="0" y="206" class="muted">ヒープ以外の大きさはアプリケーションによって変わる（幅は模式的なもの）</text>`);
  parts.push(`<text x="0" y="226" class="muted">合計が制限を超えると、プロセスごと強制終了される</text>`);
  writeSvg(join(OUT, "container-budget.svg"), svgDoc(720, 238, title, parts.join("\n")));
}
