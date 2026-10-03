// 実行: npx tsx data/java-async-07/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, svgDoc, writeSvg, esc, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-07";
const OUT = "site/public/images/java-async-07";

// 1. count++ が消える仕組み（2つのスレッドの読み書きの順番）
{
  const parts: string[] = [];
  const title = "count++ は「読む・足す・書く」の3手順。2つのスレッドが重なると1回ぶん消える";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const rows = [
    ["スレッドA", "count を読む（5）", "", "5 + 1 = 6", "", "count に書く（6）", ""],
    ["スレッドB", "", "count を読む（5）", "", "5 + 1 = 6", "", "count に書く（6）"],
  ];
  const colW = 104, left = 80, top = 44;
  rows.forEach((r, i) => {
    parts.push(`<text x="0" y="${top + 26 + i * 50}" class="ink bold">${esc(r[0])}</text>`);
    r.slice(1).forEach((cell, j) => {
      if (!cell) return;
      const x = left + j * colW;
      parts.push(`<rect x="${x}" y="${top + i * 50}" width="${colW - 6}" height="40" rx="5" class="f${i + 1}"/>`);
      parts.push(`<text x="${x + (colW - 6) / 2}" y="${top + 25 + i * 50}" text-anchor="middle" class="small" style="fill:#fff">${esc(cell)}</text>`);
    });
  });
  parts.push(`<path d="M${left},${top + 112} L${left + 6 * colW - 10},${top + 112}" class="edge" marker-end="url(#arrow)"/>`);
  parts.push(`<text x="${left}" y="${top + 130}" class="muted">時間 →　2回増やしたのに、count は 5 から 6 にしかならない</text>`);
  writeSvg(join(OUT, "lost-update.svg"), svgDoc(720, top + 142, title, parts.join("\n")));
}

// 2. 方法ごとの処理時間（スレッド数別）
{
  const [, ...lines] = readFileSync(join(DATA, "counters.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([m, n, ms]) => ({ m, n: Number(n), ms: Number(ms) }));
  const mean = (m: string, n: number) => {
    const xs = rows.filter((r) => r.m === m && r.n === n).map((r) => r.ms);
    return xs.reduce((a, b) => a + b, 0) / xs.length;
  };
  const threads = [1, 2, 4, 8, 16];
  const methods: [string, number][] = [["synchronized", 2], ["AtomicLong", 4], ["ReentrantLock", 5], ["LongAdder", 1]];
  writeSvg(
    join(OUT, "counters.svg"),
    lineChart({
      title: "カウンタを合計1,600万回増やすのにかかった時間（正しい結果になる4つの方法）",
      xLabel: "スレッドの数",
      yLabel: "処理時間（ミリ秒）",
      xTicks: [0, 1, 2, 3, 4],
      xFormat: (v) => fmt(2 ** v),
      yTicks: [0, 500, 1000, 1500, 2000],
      rightPad: 190,
      series: methods.map(([m, slot]) => ({
        name: m,
        endLabel: `${m} ${fmt(mean(m, 16))} ms`,
        slot,
        markers: true,
        points: threads.map((n) => [Math.log2(n), mean(m, n)] as [number, number]),
      })),
    })
  );
}

// 3. Map で数えた合計（3回の平均）
{
  const text = readFileSync(join(DATA, "map-count.txt"), "utf-8");
  const names = ["HashMap + get/put", "HashMap + merge", "ConcurrentHashMap + get/put", "ConcurrentHashMap + merge"];
  const rows = names.map((name) => {
    const re = new RegExp(`^${name.replace(/[+/]/g, (c) => "\\" + c)}\\s+キーの数\\s+\\d+、合計\\s+([\\d,]+)`, "gm");
    const xs = [...text.matchAll(re)].map((m) => Number(m[1].replace(/,/g, "")));
    const avg = xs.reduce((a, b) => a + b, 0) / xs.length;
    return { label: name, value: avg / 10_000, valueLabel: `${fmt(avg / 10_000)} 万`, slot: avg === 8_000_000 ? 1 : 2 };
  });
  writeSvg(
    join(OUT, "map-count.svg"),
    hBarChart({
      title: "8本のスレッドで合計800万回数えた結果（3回の平均）",
      unit: "万回（正しくは 800 万）",
      labelWidth: 220,
      xTicks: [0, 200, 400, 600, 800],
      legendItems: [
        { name: "正しい", slot: 1 },
        { name: "数え落としがある", slot: 2 },
      ],
      rows,
    })
  );
}
