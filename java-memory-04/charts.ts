// 実行: npx tsx data/java-memory-04/charts.ts
// pauses-*.csv は python3 data/java-memory-04/parse-gc-log.py data/java-memory-04/gc-<mode>.log で作る
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, svgDoc, writeSvg, esc, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-memory-04";
const OUT = "site/public/images/java-memory-04";

interface Pause {
  end: number; gc: number; type: string; before: number; after: number; committed: number; ms: number;
}
function readPauses(mode: string): Pause[] {
  const [, ...lines] = readFileSync(join(DATA, `pauses-${mode}.csv`), "utf-8").trim().split("\n");
  return lines.map((l) => {
    const c = l.split(",");
    return { end: Number(c[0]), gc: Number(c[1]), type: c[2], before: Number(c[3]), after: Number(c[4]), committed: Number(c[5]), ms: Number(c[6]) };
  });
}

// 1. ヒープ使用量の推移（のこぎり型）。GCの開始時点で before、終了時点で after
function sawtooth(mode: string, title: string, file: string) {
  const until = 1.0;
  const pauses = readPauses(mode).filter((p) => p.end <= until);
  const used: [number, number][] = [];
  for (const p of pauses) {
    used.push([(p.end - p.ms / 1000) * 1000, p.before]);
    used.push([p.end * 1000, p.after]);
  }
  const committed: [number, number][] = pauses.map((p) => [p.end * 1000, p.committed]);
  writeSvg(
    join(OUT, file),
    lineChart({
      title,
      xLabel: "起動からの時間（ミリ秒）",
      yLabel: "MB",
      xTicks: [0, 200, 400, 600, 800, 1000],
      yTicks: [0, 100, 200, 300, 400, 500],
      height: 340,
      series: [
        { name: "ヒープ committed", slot: 2, step: true, points: committed },
        { name: "ヒープ used", slot: 1, points: used },
      ],
    })
  );
}
sawtooth("short", "short：すぐ不要になるオブジェクトだけ（最初の1秒）", "sawtooth-short.svg");
sawtooth("long", "long：最近の10万個を持ち続ける（最初の1秒）", "sawtooth-long.svg");

// 2. 停止時間（種類ごとの平均と最大）
{
  const rows: { label: string; note: string; value: number; valueLabel: string; slot: number }[] = [];
  const groups: Record<number, string> = {};
  for (const [mode, name] of [["short", "short"], ["long", "long"], ["huge", "huge"]] as const) {
    const pauses = readPauses(mode);
    const total = pauses.reduce((s, p) => s + p.ms, 0);
    groups[rows.length] = `${name}（5秒間の停止の合計 ${fmt(total)} ms）`;
    const types = [...new Set(pauses.map((p) => p.type))];
    for (const t of types) {
      const ms = pauses.filter((p) => p.type === t).map((p) => p.ms);
      const avg = ms.reduce((a, b) => a + b, 0) / ms.length;
      rows.push({ label: t, note: `${ms.length} 回・最大 ${fmt(Math.max(...ms), 1)} ms`, value: avg, valueLabel: `${fmt(avg, 1)} ms`, slot: 1 });
    }
  }
  writeSvg(
    join(OUT, "pauses.svg"),
    hBarChart({
      title: "GCの停止時間（1回あたりの平均）",
      unit: "ミリ秒",
      labelWidth: 220,
      xTicks: [0, 4, 8, 12],
      groups,
      rows,
    })
  );
}

// 3. G1 のリージョンの模式図
{
  const parts: string[] = [];
  const title = "G1 のヒープ：同じ大きさのリージョンに役割を割り当てる";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const kinds: Record<string, [string, string]> = {
    E: ["Eden", "f1"], S: ["Survivor", "f3"], O: ["Old", "f2"], H: ["Humongous", "f5"], F: ["空き", "free"],
  };
  const layout = [
    "EEOFOHHFEO",
    "OSFEEOFFOE",
    "FOEFHHHOFE",
    "EOFOSFEEOF",
  ];
  const size = 38, gap = 5, left = 10, top = 70;
  let lx = 0;
  for (const k of ["E", "S", "O", "H", "F"]) {
    const [name, cls] = kinds[k];
    parts.push(cls === "free" ? `<rect x="${lx}" y="34" width="12" height="12" rx="2" class="box"/>` : `<rect x="${lx}" y="34" width="12" height="12" rx="2" class="${cls}"/>`);
    parts.push(`<text x="${lx + 18}" y="44">${esc(name)}</text>`);
    lx += 18 + name.length * 8 + 26;
  }
  layout.forEach((row, r) =>
    [...row].forEach((k, c) => {
      const x = left + c * (size + gap), y = top + r * (size + gap);
      const [, cls] = kinds[k];
      parts.push(cls === "free" ? `<rect x="${x}" y="${y}" width="${size}" height="${size}" rx="4" class="box"/>` : `<rect x="${x}" y="${y}" width="${size}" height="${size}" rx="4" class="${cls}"/>`);
      parts.push(`<text x="${x + size / 2}" y="${y + size / 2 + 5}" text-anchor="middle" class="small" style="fill:${cls === "free" ? "var(--muted)" : "#fff"}">${k === "F" ? "" : k}</text>`);
    })
  );
  const tx = left + 10 * (size + gap) + 16;
  const notes = [
    ["Eden", "新しいオブジェクトを作る場所"],
    ["Survivor", "Young GC を生き延びたもの"],
    ["Old", "何回も生き延びたもの"],
    ["Humongous", "リージョンの半分以上の"],
    ["", "大きなオブジェクト（連続して確保）"],
  ];
  notes.forEach(([k, v], i) => {
    if (k) parts.push(`<text x="${tx}" y="${top + 16 + i * 36}" class="ink bold">${esc(k)}</text>`);
    parts.push(`<text x="${tx}" y="${top + 34 + i * 36 - (k ? 0 : 18)}" class="muted">${esc(v)}</text>`);
  });
  parts.push(`<text x="0" y="${top + 4 * (size + gap) + 20}" class="muted">並びは模式的なもの。リージョンの大きさはヒープの大きさから決まる（この記事の -Xmx512m では 1MB）</text>`);
  writeSvg(join(OUT, "regions.svg"), svgDoc(720, top + 4 * (size + gap) + 32, title, parts.join("\n")));
}
