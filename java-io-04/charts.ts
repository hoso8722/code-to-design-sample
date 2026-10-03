// 実行: npx tsx data/java-io-04/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, svgDoc, writeSvg, esc, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-io-04";
const OUT = "site/public/images/java-io-04";

// 1. 書いたデータが通る3つの場所
{
  const W = 720;
  const H = 250;
  const boxes = [
    { x: 20, title: "Javaのバッファ", sub: "BufferedWriter の中の配列", lost: "プログラムが異常終了すると", lost2: "失われる", slot: 2 },
    { x: 265, title: "OSのキャッシュ", sub: "OSが管理するメモリ", lost: "OSの異常終了や電源断で", lost2: "失われる", slot: 4 },
    { x: 510, title: "ディスク", sub: "電源を切っても残る場所", lost: "残る", lost2: "", slot: 3 },
  ];
  const bw = 190;
  const by = 78;
  const bh = 70;
  let body = `<text x="20" y="24" class="title">write したデータが、ディスクに届くまで</text>`;
  body += `<text x="20" y="${by - 26}" class="ink bold">write</text>`;
  body += `<path d="M 40 ${by - 20} L 40 ${by - 3}" class="edge" marker-end="url(#arrow)"/>`;
  for (const b of boxes) {
    body += `<rect x="${b.x}" y="${by}" width="${bw}" height="${bh}" rx="8" class="box"/>`;
    body += `<rect x="${b.x}" y="${by}" width="6" height="${bh}" rx="3" class="f${b.slot}"/>`;
    body += `<text x="${b.x + bw / 2}" y="${by + 30}" text-anchor="middle" class="ink bold">${esc(b.title)}</text>`;
    body += `<text x="${b.x + bw / 2}" y="${by + 52}" text-anchor="middle" class="muted">${esc(b.sub)}</text>`;
    body += `<text x="${b.x + bw / 2}" y="${by + bh + 34}" text-anchor="middle" class="muted">${esc(b.lost)}</text>`;
    body += `<text x="${b.x + bw / 2}" y="${by + bh + 52}" text-anchor="middle" class="muted">${esc(b.lost2)}</text>`;
  }
  const arrows = [
    { from: boxes[0].x + bw, to: boxes[1].x, label: "flush / close" },
    { from: boxes[1].x + bw, to: boxes[2].x, label: "force" },
  ];
  for (const a of arrows) {
    const y = by + bh / 2;
    body += `<path d="M ${a.from + 4} ${y} L ${a.to - 4} ${y}" class="edge" marker-end="url(#arrow)"/>`;
    body += `<text x="${(a.from + a.to) / 2}" y="${y - 10}" text-anchor="middle" class="ink bold small">${esc(a.label)}</text>`;
  }
  body += `<text x="20" y="${H - 14}" class="muted">force を呼ばなくても、OSは、キャッシュの内容を少しずつディスクに書き出す</text>`;
  writeSvg(join(OUT, "stages.svg"), svgDoc(W, H, "write したデータが、Javaのバッファ、OSのキャッシュ、ディスクの順に届く図", body));
}

// 2. force の間隔と、2,000件を書く時間（1回目の実行）
{
  const first = readFileSync(join(DATA, "force-cost.txt"), "utf-8").split("# 2回目")[0];
  const rows = first
    .split("\n")
    .filter((l) => /^(呼ばない|\d+件ごと),/.test(l))
    .map((l, i) => {
      const [label, forces, ms] = l.split(",");
      return {
        label: label === "呼ばない" ? "force を呼ばない" : `${label}に force（${fmt(Number(forces))}回）`,
        value: Number(ms),
        valueLabel: `${fmt(Number(ms), 1)} ms`,
        slot: label === "呼ばない" ? 1 : 2,
      };
    });
  writeSvg(
    join(OUT, "force-cost.svg"),
    hBarChart({ title: "2,000件を書く時間と、force を呼ぶ間隔", unit: "ミリ秒", labelWidth: 250, xTicks: [0, 1000, 2000, 3000, 4000, 5000], rows })
  );
}
