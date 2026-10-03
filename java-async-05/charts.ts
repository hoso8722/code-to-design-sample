// 実行: npx tsx data/java-async-05/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { svgDoc, timelineChart, writeSvg, esc } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-05";
const OUT = "site/public/images/java-async-05";

// 1. つないだ処理が動いたスレッド（which-thread.txt の B）
{
  const text = readFileSync(join(DATA, "which-thread.txt"), "utf-8");
  const section = text.split("== B.")[1].split("== C.")[0];
  const steps = [...section.matchAll(/^ {2}(\S+)\s+(.+)$/gm)].map((m) => ({ thread: m[1], step: m[2].trim() }));
  const slotOf = (thread: string) => (thread.startsWith("io-") ? 1 : thread.startsWith("ForkJoinPool") ? 2 : 3);
  const parts: string[] = [];
  const title = "1段目を io プールで動かしたとき、つないだ各段が動いたスレッド";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const w = 160, gap = 20, y = 50;
  steps.forEach((s, i) => {
    const x = i * (w + gap);
    parts.push(`<rect x="${x}" y="${y}" width="${w}" height="54" rx="6" class="box"/>`);
    const [num, ...rest] = s.step.split(" ");
    parts.push(`<text x="${x + w / 2}" y="${y + 22}" text-anchor="middle" class="ink bold">${esc(num + " " + rest.join(" ").replace(/（.*/, ""))}</text>`);
    const note = s.step.match(/（(.*)）/)?.[1];
    if (note) parts.push(`<text x="${x + w / 2}" y="${y + 42}" text-anchor="middle" class="muted small">${esc(note)}</text>`);
    parts.push(`<rect x="${x}" y="${y + 70}" width="${w}" height="32" rx="6" class="f${slotOf(s.thread)}"/>`);
    parts.push(`<text x="${x + w / 2}" y="${y + 91}" text-anchor="middle" class="small" style="fill:#fff">${esc(s.thread.replace("ForkJoinPool.", ""))}</text>`);
    if (i > 0) parts.push(`<path d="M${x - gap + 2},${y + 27} L${x - 4},${y + 27}" class="edge" marker-end="url(#arrow)"/>`);
  });
  parts.push(`<text x="0" y="${y + 130}" class="muted">上の段：つないだ処理　下の段：その処理が動いたスレッド（青：io プール、橙：commonPool）</text>`);
  writeSvg(join(OUT, "which-thread.svg"), svgDoc(720, y + 142, title, parts.join("\n")));
}

// 2. 順番に取る・thenCombine・thenCompose のタイムライン
{
  const lines = readFileSync(join(DATA, "combine.txt"), "utf-8").split("\n").filter((l) => /^(sequential|combine|compose),/.test(l));
  const rows = lines.map((l) => l.split(",")).map(([mode, name, thread, s, e]) => ({ mode, name, thread, start: Number(s), end: Number(e) }));
  const slot = { sequential: 1, combine: 2, compose: 3 } as Record<string, number>;
  writeSvg(
    join(OUT, "combine.svg"),
    timelineChart({
      title: "200ミリ秒かかる取得を2つ：順番に・thenCombine・thenCompose",
      xLabel: "時間（ミリ秒）",
      lanes: [...new Set(rows.map((r) => r.thread))],
      laneWidth: 210,
      xTicks: [0, 200, 400, 600, 800, 1000, 1200],
      legendItems: [
        { name: "順番に（main）", slot: 1 },
        { name: "thenCombine", slot: 2 },
        { name: "thenCompose", slot: 3 },
      ],
      bars: rows.map((r) => ({ lane: r.thread, start: r.start, end: r.end, slot: slot[r.mode], label: r.name })),
    })
  );
}
