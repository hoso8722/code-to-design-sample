// 実行: npx tsx data/java-async-09/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { lineChart, svgDoc, timelineChart, writeSvg, esc } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-09";
const OUT = "site/public/images/java-async-09";

// 1. デッドロックの関係図（deadlock-thread-dump.txt の「Found one Java-level deadlock」を図にしたもの）
{
  const dump = readFileSync(join(DATA, "deadlock-thread-dump.txt"), "utf-8");
  if (!dump.includes("Found one Java-level deadlock")) throw new Error("デッドロックが記録されていません");
  const parts: string[] = [];
  const title = "デッドロック：2本のスレッドが、相手の持っているロックを待ち合う";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const node = (x: number, y: number, label: string, sub: string, cls: string) => {
    parts.push(`<rect x="${x}" y="${y}" width="170" height="54" rx="8" class="${cls}"/>`);
    parts.push(`<text x="${x + 85}" y="${y + 24}" text-anchor="middle" class="bold" style="fill:${cls === "box" ? "var(--ink)" : "#fff"}">${esc(label)}</text>`);
    parts.push(`<text x="${x + 85}" y="${y + 42}" text-anchor="middle" class="small" style="fill:${cls === "box" ? "var(--muted)" : "#fff"}">${esc(sub)}</text>`);
  };
  const edge = (d: string, label: string, lx: number, ly: number, dashed: boolean) => {
    parts.push(`<path d="${d}" class="edge${dashed ? " dash" : ""}" marker-end="url(#arrow)"/>`);
    parts.push(`<text x="${lx}" y="${ly}" class="muted small">${esc(label)}</text>`);
  };
  node(20, 110, "transfer-1", "BLOCKED", "f1");
  node(530, 110, "transfer-2", "BLOCKED", "f2");
  node(275, 44, "accountA のロック", "", "box");
  node(275, 176, "accountB のロック", "", "box");
  // transfer-1 → A（持っている）、transfer-1 → B（待っている）
  edge("M190,122 C230,100 240,80 271,72", "持っている", 196, 92, false);
  edge("M190,152 C230,175 240,195 271,202", "待っている", 196, 196, true);
  // transfer-2 → B（持っている）、transfer-2 → A（待っている）
  edge("M530,152 C490,175 480,195 449,202", "持っている", 474, 196, false);
  edge("M530,122 C490,100 480,80 449,72", "待っている", 474, 92, true);
  parts.push(`<text x="0" y="258" class="muted">実線：ロックを持っている　点線：ロックを待っている。待ち合いが輪になり、どちらも先へ進めない</text>`);
  writeSvg(join(OUT, "deadlock.svg"), svgDoc(720, 270, title, parts.join("\n")));
}

// 2. commonPool で待つ処理の数と、すぐ終わるはずの処理の遅れ
{
  const [, ...lines] = readFileSync(join(DATA, "common-pool-jam.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",").map(Number));
  const ns = [...new Set(rows.map((r) => r[0]))];
  const mean = (n: number) => {
    const xs = rows.filter((r) => r[0] === n).map((r) => r[2]);
    return xs.reduce((a, b) => a + b, 0) / xs.length;
  };
  writeSvg(
    join(OUT, "common-pool-jam.svg"),
    lineChart({
      title: "commonPool に1秒待つ処理を投入したあと、すぐ終わるはずの処理の結果が出るまで",
      xLabel: "先に投入した「1秒待つ処理」の数（commonPool の並列度は 15）",
      yLabel: "結果が出るまで（ミリ秒）",
      xTicks: [0, 15, 30, 45],
      yTicks: [0, 1000, 2000, 3000, 4000],
      rightPad: 90,
      series: [{ name: "すぐ終わるはずの処理", endLabel: `${Math.round(mean(45))} ms`, slot: 2, step: true, markers: true, points: ns.map((n) => [n, mean(n)]) }],
    })
  );
}

// 3. 30個投入したときの commonPool のタイムライン
{
  const lines = readFileSync(join(DATA, "common-pool-jam-30.txt"), "utf-8").split("\n").filter((l) => /^(slow|fast),/.test(l));
  const rows = lines.map((l) => l.split(",")).map(([kind, thread, s, e]) => ({ kind, thread: thread.replace("ForkJoinPool.commonPool-", ""), start: Number(s), end: Number(e) }));
  const lanes = [...new Set(rows.map((r) => r.thread))].sort((a, b) => Number(a.split("-")[1]) - Number(b.split("-")[1]));
  const t0 = Math.min(...rows.map((r) => r.start));
  writeSvg(
    join(OUT, "common-pool-timeline.svg"),
    timelineChart({
      title: "1秒待つ処理を30個投入してから、すぐ終わる処理を1つ投入したとき",
      xLabel: "時間（ミリ秒）",
      lanes,
      laneWidth: 90,
      xTicks: [0, 500, 1000, 1500, 2000, 2500],
      legendItems: [
        { name: "1秒待つ処理", slot: 1 },
        { name: "すぐ終わるはずの処理", slot: 2 },
      ],
      bars: rows.map((r) => (r.kind === "fast"
        ? { lane: r.thread, start: r.start - t0, end: r.start - t0 + 25, slot: 2 }
        : { lane: r.thread, start: r.start - t0, end: r.end - t0, slot: 1 })),
      markers: [{ x: rows.find((r) => r.kind === "fast")!.start - t0, label: "ここでやっと fast が動く" }],
    })
  );
}
