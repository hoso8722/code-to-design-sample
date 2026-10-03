// 実行: npx tsx data/java-memory-03/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, svgDoc, writeSvg, esc, fmt } from "../../scripts/lib/svg-chart.js";
import { readCsv } from "../../scripts/lib/nmt.js";

const DATA = "data/java-memory-03";
const OUT = "site/public/images/java-memory-03";

// 1. オブジェクトのレイアウト（JOL の出力 jol-layouts.txt と同じ並び。JDK 21・64bit・圧縮参照あり）
{
  type Cell = { from: number; size: number; label: string; kind: "header" | "field" | "pad" };
  const header: Cell[] = [
    { from: 0, size: 8, label: "mark", kind: "header" },
    { from: 8, size: 4, label: "class", kind: "header" },
  ];
  const objects: { name: string; cells: Cell[] }[] = [
    { name: "Object", cells: [...header, { from: 12, size: 4, label: "", kind: "pad" }] },
    { name: "Integer", cells: [...header, { from: 12, size: 4, label: "int value", kind: "field" }] },
    {
      name: "Long",
      cells: [...header, { from: 12, size: 4, label: "", kind: "pad" }, { from: 16, size: 8, label: "long value", kind: "field" }],
    },
    {
      name: "Point(int x, int y)",
      cells: [
        ...header,
        { from: 12, size: 4, label: "int x", kind: "field" },
        { from: 16, size: 4, label: "int y", kind: "field" },
        { from: 20, size: 4, label: "", kind: "pad" },
      ],
    },
    {
      name: "String",
      cells: [
        ...header,
        { from: 12, size: 4, label: "hash", kind: "field" },
        { from: 16, size: 1, label: "", kind: "field" },
        { from: 17, size: 1, label: "", kind: "field" },
        { from: 18, size: 2, label: "", kind: "pad" },
        { from: 20, size: 4, label: "value", kind: "field" },
      ],
    },
  ];
  const left = 160;
  const unit = 19; // 1バイトの幅
  const parts: string[] = [];
  const title = "オブジェクトのレイアウト（JDK 21・64bit・圧縮参照あり）";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const legendItems: [string, string][] = [["ヘッダ", "f1"], ["フィールド", "f3"], ["パディング（すき間）", "pad"]];
  let lx = 0;
  for (const [name, cls] of legendItems) {
    parts.push(cls === "pad"
      ? `<rect x="${lx}" y="34" width="12" height="12" rx="2" class="box dash"/>`
      : `<rect x="${lx}" y="34" width="12" height="12" rx="2" class="${cls}"/>`);
    parts.push(`<text x="${lx + 18}" y="44">${esc(name)}</text>`);
    lx += 18 + name.length * 13 + 20;
  }
  for (const b of [0, 8, 12, 16, 20, 24]) {
    parts.push(`<text x="${left + b * unit}" y="76" text-anchor="middle" class="muted">${b}</text>`);
  }
  objects.forEach((o, i) => {
    const y = 86 + i * 48;
    const total = o.cells.reduce((m, c) => Math.max(m, c.from + c.size), 0);
    parts.push(`<text x="${left - 12}" y="${y + 21}" text-anchor="end" class="ink">${esc(o.name)}</text>`);
    for (const c of o.cells) {
      const x = left + c.from * unit + 1;
      const w = c.size * unit - 2;
      if (c.kind === "pad") parts.push(`<rect x="${x}" y="${y}" width="${w}" height="30" rx="3" class="box dash"/>`);
      else parts.push(`<rect x="${x}" y="${y}" width="${w}" height="30" rx="3" class="${c.kind === "header" ? "f1" : "f3"}"/>`);
      if (c.label) parts.push(`<text x="${x + w / 2}" y="${y + 20}" text-anchor="middle" style="fill:#fff" class="small">${esc(c.label)}</text>`);
    }
    parts.push(`<text x="${left + total * unit + 10}" y="${y + 21}" class="ink bold">${total} バイト</text>`);
  });
  parts.push(`<text x="0" y="${86 + objects.length * 48 + 14}" class="muted">String の 16〜17 バイト目は coder（byte）と hashIsZero（boolean）。value は中身の byte[] への参照</text>`);
  writeSvg(join(OUT, "layout.svg"), svgDoc(720, 86 + objects.length * 48 + 24, title, parts.join("\n")));
}

// 2. データ構造ごとの1要素あたりのバイト数（retained.txt）
{
  const rows = readFileSync(join(DATA, "retained.txt"), "utf-8")
    .trim()
    .split("\n")
    .map((line) => {
      const m = line.match(/^(\S+)\s+([\d.]+) MB\s+([\d.]+) bytes/);
      if (!m) throw new Error(`読めない行: ${line}`);
      return { label: m[1], value: Number(m[3]) };
    });
  writeSvg(
    join(OUT, "per-element.svg"),
    hBarChart({
      title: "100万個の int を入れたときの、1要素あたりのバイト数",
      unit: "バイト / 要素",
      labelWidth: 210,
      xTicks: [0, 20, 40, 60, 80],
      rows: rows.map((r) => ({ label: r.label, value: r.value, valueLabel: fmt(r.value, 1), slot: 1 })),
    })
  );
}

// 3. 文字列の長さとバイト数
{
  const rows = readCsv(readFileSync(join(DATA, "string-size.csv"), "utf-8"));
  writeSvg(
    join(OUT, "string-size.svg"),
    lineChart({
      title: "文字列1個の大きさ（String + 中の byte[]）",
      xLabel: "文字数",
      yLabel: "バイト",
      xTicks: [0, 20, 40, 60, 80, 100],
      yTicks: [0, 50, 100, 150, 200, 250],
      series: [
        { name: "日本語（\"あ\" の繰り返し）", endLabel: "日本語", slot: 2, markers: true, points: rows.map((r) => [r.length, r.japanese_bytes]) },
        { name: "ASCII（\"a\" の繰り返し）", endLabel: "ASCII", slot: 1, markers: true, points: rows.map((r) => [r.length, r.latin1_bytes]) },
      ],
    })
  );
}
