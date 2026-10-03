// 実行: npx tsx data/java-memory-05/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, svgDoc, writeSvg, esc } from "../../scripts/lib/svg-chart.js";
import { readCsv } from "../../scripts/lib/nmt.js";

const DATA = "data/java-memory-05";
const OUT = "site/public/images/java-memory-05";

// 1. キャッシュ：GC後のヒープ使用量の推移
{
  const unbounded = readCsv(readFileSync(join(DATA, "cache-unbounded.csv"), "utf-8"));
  const bounded = readCsv(readFileSync(join(DATA, "cache-bounded.csv"), "utf-8"));
  writeSvg(
    join(OUT, "cache.svg"),
    lineChart({
      title: "リクエストを処理するたびに増える、GC後のヒープ使用量",
      xLabel: "処理したリクエスト数",
      yLabel: "GC後のヒープ使用量（MB）",
      xTicks: [0, 50_000, 100_000, 150_000],
      xFormat: (v) => (v === 0 ? "0" : `${v / 10_000}万`),
      yTicks: [0, 50, 100, 150, 200],
      rightPad: 150,
      series: [
        { name: "上限なし（HashMap）", endLabel: "上限なし", slot: 2, markers: true, points: unbounded.map((r) => [r.requests, r.used_after_gc_mb]) },
        { name: "上限1万件（LRU）", endLabel: "上限1万件", slot: 1, markers: true, points: bounded.map((r) => [r.requests, r.used_after_gc_mb]) },
      ],
    })
  );
}

// 2. ThreadLocal と WeakHashMap の結果
{
  const tl = readFileSync(join(DATA, "threadlocal.txt"), "utf-8");
  const after = [...tl.matchAll(/すべて処理した後: (\d+) MB/g)].map((m) => Number(m[1]));
  // threadlocal.txt は noremove ×2 → remove ×2 の順
  const weak = readFileSync(join(DATA, "weak.txt"), "utf-8");
  const w = (mode: string) => {
    const m = weak.match(new RegExp(`^${mode}: GC後=(\\d+)MB  size\\(\\)=(\\d+)  size\\(\\)のあとGC=(\\d+)MB`, "m"))!;
    return { gc: Number(m[1]), size: Number(m[2]), access: Number(m[3]) };
  };
  const hash = w("hash"), weakMap = w("weak"), wvk = w("weak-value-key");
  writeSvg(
    join(OUT, "threadlocal.svg"),
    hBarChart({
      title: "スレッドプール（16スレッド）で1,000件処理した後のGC後のヒープ使用量",
      unit: "MB",
      labelWidth: 230,
      xTicks: [0, 25, 50, 75, 100],
      rows: [
        { label: "remove() しない", value: after[0], valueLabel: `${after[0]} MB`, slot: 2 },
        { label: "finally で remove() する", value: after[2], valueLabel: `${after[2]} MB`, slot: 1 },
      ],
    })
  );
  writeSvg(
    join(OUT, "weakhashmap.svg"),
    hBarChart({
      title: "キー10万個のうち100個だけを残したときのヒープ使用量",
      unit: "MB",
      labelWidth: 230,
      xTicks: [0, 30, 60, 90, 120],
      groups: {
        0: `HashMap（size() = ${hash.size.toLocaleString()}）`,
        2: `WeakHashMap（size() = ${weakMap.size.toLocaleString()}）`,
        4: `WeakHashMap・値の中からキーを参照（size() = ${wvk.size.toLocaleString()}）`,
      },
      legendItems: [
        { name: "GCの直後", slot: 2 },
        { name: "size() で Map を操作してからGC", slot: 1 },
      ],
      rows: [
        { label: "GCの直後", value: hash.gc, valueLabel: `${hash.gc} MB`, slot: 2 },
        { label: "size() のあとGC", value: hash.access, valueLabel: `${hash.access} MB`, slot: 1 },
        { label: "GCの直後", value: weakMap.gc, valueLabel: `${weakMap.gc} MB`, slot: 2 },
        { label: "size() のあとGC", value: weakMap.access, valueLabel: `${weakMap.access} MB`, slot: 1 },
        { label: "GCの直後", value: wvk.gc, valueLabel: `${wvk.gc} MB`, slot: 2 },
        { label: "size() のあとGC", value: wvk.access, valueLabel: `${wvk.access} MB`, slot: 1 },
      ],
    })
  );
}

// 3. 強い参照・弱い参照とGCの図
{
  const parts: string[] = [];
  const title = "WeakHashMap のエントリと参照の種類";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  parts.push(`<line x1="0" y1="44" x2="28" y2="44" class="edge"/><text x="36" y="48">強い参照</text>`);
  parts.push(`<line x1="120" y1="44" x2="148" y2="44" class="edge dash"/><text x="156" y="48">弱い参照（GCはたどらない）</text>`);
  const box = (x: number, y: number, w: number, label: string, sub: string, cls = "box") => {
    parts.push(`<rect x="${x}" y="${y}" width="${w}" height="46" rx="6" class="${cls}"/>`);
    parts.push(`<text x="${x + w / 2}" y="${y + 20}" text-anchor="middle" class="ink bold">${esc(label)}</text>`);
    parts.push(`<text x="${x + w / 2}" y="${y + 37}" text-anchor="middle" class="muted">${esc(sub)}</text>`);
  };
  const edge = (d: string, dashed = false) => parts.push(`<path d="${d}" class="edge${dashed ? " dash" : ""}" marker-end="url(#arrow)"/>`);
  // 上段：ふつうの場合
  parts.push(`<text x="0" y="86" class="ink bold">(a) ほかにキーへの強い参照がない場合</text>`);
  box(0, 100, 120, "GCルート", "static など");
  box(170, 100, 130, "WeakHashMap", "の中の配列");
  box(350, 100, 110, "Entry", "");
  box(510, 70, 110, "Key", "→ 回収できる");
  box(510, 134, 110, "Value", "（1KB）");
  edge("M120,123 L166,123");
  edge("M300,123 L346,123");
  edge("M460,116 C485,105 490,93 506,93", true);
  edge("M460,130 C485,140 490,152 506,155");
  // 下段：値がキーを参照する場合
  parts.push(`<text x="0" y="226" class="ink bold">(b) 値の中からキーを参照している場合</text>`);
  box(0, 240, 120, "GCルート", "static など");
  box(170, 240, 130, "WeakHashMap", "の中の配列");
  box(350, 240, 110, "Entry", "");
  box(510, 210, 110, "Key", "→ 回収できない");
  box(510, 274, 110, "Value", "key フィールド");
  edge("M120,263 L166,263");
  edge("M300,263 L346,263");
  edge("M460,256 C485,245 490,233 506,233", true);
  edge("M460,270 C485,280 490,292 506,295");
  edge("M620,297 C690,297 690,233 624,233");
  parts.push(`<text x="0" y="346" class="muted">Entry から Value へは強い参照。Value が Key を参照していると、Key は GCルートから強い参照だけでたどれてしまう</text>`);
  writeSvg(join(OUT, "weak-refs.svg"), svgDoc(720, 358, title, parts.join("\n")));
}
