// 実行: npx tsx data/java-memory-06/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";
import { readCsv } from "../../scripts/lib/nmt.js";

const DATA = "data/java-memory-06";
const OUT = "site/public/images/java-memory-06";

// 1. クラス数と Metaspace
{
  const rows = readCsv(readFileSync(join(DATA, "many-classes.csv"), "utf-8").replace(/^define,/gm, "").replace(/^after-gc,.*$/m, "").replace(/^phase,/, ""));
  writeSvg(
    join(OUT, "metaspace.svg"),
    lineChart({
      title: "ヒドゥンクラスを定義した数と Metaspace の使用量",
      xLabel: "定義したクラスの数",
      yLabel: "MB",
      xTicks: [0, 5000, 10000, 15000, 20000],
      yTicks: [0, 10, 20, 30],
      rightPad: 190,
      series: [
        { name: "Metaspace（全体）", slot: 1, markers: true, points: rows.map((r) => [r.classes, r.metaspace_kb / 1024]) },
        { name: "うち Compressed Class Space", endLabel: "うち Class Space", slot: 2, markers: true, points: rows.map((r) => [r.classes, r.class_space_kb / 1024]) },
      ],
    })
  );
}

// 2. Direct Buffer とヒープ・RSS
{
  const rows = readCsv(readFileSync(join(DATA, "direct-buffers.csv"), "utf-8"));
  writeSvg(
    join(OUT, "direct.svg"),
    lineChart({
      title: "Direct Buffer を確保したときのヒープと RSS",
      xLabel: "ByteBuffer.allocateDirect で確保した量（MB）",
      yLabel: "MB",
      xTicks: [0, 128, 256, 384, 512],
      yTicks: [0, 200, 400, 600],
      rightPad: 170,
      series: [
        { name: "RSS", slot: 1, markers: true, points: rows.map((r) => [r.allocated_mb, r.rss_mb]) },
        { name: "Direct Buffer（BufferPoolMXBean）", endLabel: "Direct Buffer", slot: 3, markers: true, points: rows.map((r) => [r.allocated_mb, r.direct_mb]) },
        { name: "ヒープ used", slot: 2, markers: true, points: rows.map((r) => [r.allocated_mb, r.heap_used_mb]) },
      ],
    })
  );
}

// 3. NMT の summary.diff（committed の増分が 500KB 以上の分類）
{
  const text = readFileSync(join(DATA, "nmt-diff.txt"), "utf-8");
  const rows = [...text.matchAll(/^-\s+(.+?) \(reserved=\d+KB(?: [+-]\d+KB)?, committed=\d+KB ([+-]\d+)KB/gm)]
    .map((m) => ({ name: m[1].trim(), kb: Number(m[2]) }))
    .filter((r) => r.kb >= 500)
    .sort((a, b) => b.kb - a.kb);
  writeSvg(
    join(OUT, "nmt-diff.svg"),
    hBarChart({
      title: "Direct Buffer 256MB とクラス1万個のあとの NMT の差分（committed）",
      unit: "MB",
      labelWidth: 200,
      xTicks: [0, 100, 200, 300],
      rows: rows.map((r) => ({ label: r.name, value: r.kb / 1024, valueLabel: `+${fmt(r.kb / 1024, 1)} MB`, slot: 1 })),
    })
  );
}
