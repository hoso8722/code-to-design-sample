// 実行: npx tsx data/java-memory-07/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, svgDoc, writeSvg, esc, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-memory-07";
const OUT = "site/public/images/java-memory-07";

interface Row { kind: string; count: number; depth: number; rss: number; heap: number }
function read(file: string, header: boolean): Row[] {
  const lines = readFileSync(join(DATA, file), "utf-8").trim().split("\n").slice(header ? 1 : 0);
  return lines.map((l) => {
    const [kind, count, depth, rss, heap] = l.split(",");
    return { kind, count: Number(count), depth: Number(depth), rss: Number(rss), heap: Number(heap) };
  });
}
const rows = [...read("threads.csv", true), ...read("threads-small.csv", false)];
/** 3回の平均 */
export function mean(kind: string, count: number, depth: number, key: "rss" | "heap"): number {
  const xs = rows.filter((r) => r.kind === kind && r.count === count && r.depth === depth).map((r) => r[key]);
  if (xs.length === 0) throw new Error(`データがありません: ${kind} ${count} ${depth}`);
  return xs.reduce((a, b) => a + b, 0) / xs.length;
}

// 1. スレッド数と RSS（深さ10、4,000本まで）
{
  const counts = [0, 100, 500, 1000, 2000, 4000];
  writeSvg(
    join(OUT, "threads-rss.svg"),
    lineChart({
      title: "待機しているスレッドの数と、増えたメモリ（呼び出しの深さ10）",
      xLabel: "スレッドの数",
      yLabel: "MB（スレッドを作る前との差）",
      xTicks: [0, 1000, 2000, 3000, 4000],
      yTicks: [0, 30, 60, 90, 120],
      rightPad: 180,
      series: [
        { name: "プラットフォームスレッド：RSS", endLabel: "プラットフォーム RSS", slot: 2, markers: true, points: counts.map((c) => [c, mean("platform", c, 10, "rss")]) },
        { name: "仮想スレッド：RSS", endLabel: "仮想 RSS", slot: 1, markers: true, points: counts.map((c) => [c, mean("virtual", c, 10, "rss")]) },
        { name: "仮想スレッド：ヒープ", endLabel: "仮想 ヒープ", slot: 3, markers: true, points: counts.map((c) => [c, mean("virtual", c, 10, "heap")]) },
      ],
    })
  );
}

// 2. 1本あたりのメモリ
{
  const perKB = (mb: number, n: number) => (mb * 1024) / n;
  const r = [
    { label: "プラットフォーム（-Xint）", note: "4,000本の RSS の増分から", value: perKB(380, 4000), slot: 2 },
    { label: "プラットフォーム", note: "4,000本の RSS の増分から", value: perKB(mean("platform", 4000, 10, "rss"), 4000), slot: 2 },
    { label: "仮想（深さ100）", note: "10万本のヒープの増分から", value: perKB(mean("virtual", 100000, 100, "heap"), 100000), slot: 1 },
    { label: "仮想（深さ10）", note: "10万本のヒープの増分から", value: perKB(mean("virtual", 100000, 10, "heap"), 100000), slot: 1 },
  ];
  writeSvg(
    join(OUT, "per-thread.svg"),
    hBarChart({
      title: "待機しているスレッド1本あたりのメモリ",
      unit: "KB / 本",
      labelWidth: 220,
      xTicks: [0, 25, 50, 75, 100],
      legendItems: [
        { name: "プラットフォームスレッド", slot: 2 },
        { name: "仮想スレッド", slot: 1 },
      ],
      rows: r.map((x) => ({ ...x, valueLabel: `${fmt(x.value, 1)} KB` })),
    })
  );
}

// 3. スタックの置き場所
{
  const parts: string[] = [];
  const title = "2種類のスレッドのスタックはどこにあるか";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const box = (x: number, y: number, w: number, h: number, label: string, sub: string, cls = "box") => {
    parts.push(`<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="6" class="${cls}"/>`);
    parts.push(`<text x="${x + 10}" y="${y + 20}" class="ink bold">${esc(label)}</text>`);
    if (sub) parts.push(`<text x="${x + 10}" y="${y + 38}" class="muted">${esc(sub)}</text>`);
  };
  // 左：プラットフォームスレッド
  parts.push(`<text x="0" y="56" class="ink bold">プラットフォームスレッド</text>`);
  box(0, 68, 330, 250, "ヒープの外（スレッドごとのスタック）", "1本につき -Xss（1MB）を予約");
  for (let i = 0; i < 3; i++) {
    const y = 120 + i * 62;
    parts.push(`<rect x="16" y="${y}" width="298" height="50" rx="4" class="box"/>`);
    parts.push(`<rect x="16" y="${y}" width="${[90, 26, 26][i]}" height="50" rx="4" class="f2"/>`);
    parts.push(`<text x="${16 + [90, 26, 26][i] + 8}" y="${y + 20}" class="ink">スレッド${i + 1}のスタック</text>`);
    parts.push(`<text x="${16 + [90, 26, 26][i] + 8}" y="${y + 38}" class="muted">${["色の部分だけ物理メモリを使う", "", ""][i]}</text>`);
  }
  parts.push(`<text x="0" y="338" class="muted">OSのスレッド1本に1つ。待機中もスタックはそのまま</text>`);
  // 右：仮想スレッド
  parts.push(`<text x="370" y="56" class="ink bold">仮想スレッド</text>`);
  box(370, 68, 350, 130, "ヒープ", "待機中のスタックは StackChunk オブジェクト");
  for (let i = 0; i < 6; i++) {
    parts.push(`<rect x="${386 + i * 54}" y="${130}" width="46" height="${[40, 40, 52, 40, 40, 40][i]}" rx="4" class="f1"/>`);
  }
  box(370, 238, 350, 80, "ヒープの外（キャリアスレッドのスタック）", "");
  parts.push(`<rect x="386" y="268" width="150" height="40" rx="4" class="box"/><text x="396" y="293" class="ink small">実行中の仮想スレッド</text>`);
  parts.push(`<rect x="552" y="268" width="150" height="40" rx="4" class="box"/><text x="562" y="293" class="ink small">実行中の仮想スレッド</text>`);
  parts.push(`<path d="M430,172 L430,264" class="edge" marker-end="url(#arrow)"/>`);
  parts.push(`<path d="M660,264 L660,176" class="edge" marker-end="url(#arrow)"/>`);
  parts.push(`<text x="438" y="222" class="muted small">再開するとき</text>`);
  parts.push(`<text x="652" y="222" text-anchor="end" class="muted small">待つとき</text>`);
  parts.push(`<text x="370" y="338" class="muted">キャリアは CPU の数ほどのプラットフォームスレッド</text>`);
  writeSvg(join(OUT, "stacks.svg"), svgDoc(720, 350, title, parts.join("\n")));
}
