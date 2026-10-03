// 実行: npx tsx data/java-collections-03/charts.ts
import { join } from "node:path";
import { lineChart, svgDoc, writeSvg, esc } from "../../scripts/lib/svg-chart.js";
import { nsLabel, readJmhCsv } from "../../scripts/lib/jmh.js";

const DATA = "data/java-collections-03";
const OUT = "site/public/images/java-collections-03";
const log = Math.log10;

// 1. 要素数と get の時間（3種類のキー、両軸とも対数）
{
  const rows = readJmhCsv(join(DATA, "jmh-HashGet.csv"));
  const ns = [100, 1000, 10000];
  const s = (k: string, n: number) => rows.find((r) => r.params.keyType === k && r.params.n === String(n))!.score;
  const series = (k: string, name: string, slot: number) => ({
    name,
    endLabel: `${name} ${nsLabel(s(k, 10000))}`,
    slot,
    markers: true,
    points: ns.map((n) => [log(n), log(s(k, n))] as [number, number]),
  });
  writeSvg(
    join(OUT, "hash-get.svg"),
    lineChart({
      title: "HashMap の get の時間：同じハッシュ値を返すキー（比べられる／比べられない）とふつうのキー",
      xLabel: "要素数",
      yLabel: "get 1回の時間",
      xTicks: [2, 3, 4],
      xFormat: (v) => (10 ** v).toLocaleString("en-US"),
      yTicks: [0, 1, 2, 3, 4, 5],
      yFormat: (v) => nsLabel(10 ** v),
      rightPad: 170,
      series: [
        series("badPlain", "比べられない", 2),
        series("badComparable", "比べられる", 4),
        series("good", "ふつう", 1),
      ],
    })
  );
}

// 2. キーからバケットが決まるまで（模式図）
{
  const parts: string[] = [];
  const title = "HashMap がキーを入れるバケットを決めるまで（JDK 25 の実装）";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const step = (x: number, label: string, sub: string, cls = "box") => {
    parts.push(`<rect x="${x}" y="44" width="150" height="56" rx="6" class="${cls}"/>`);
    parts.push(`<text x="${x + 75}" y="68" text-anchor="middle" class="ink bold small">${esc(label)}</text>`);
    parts.push(`<text x="${x + 75}" y="88" text-anchor="middle" class="muted small">${esc(sub)}</text>`);
  };
  step(0, "key.hashCode()", "キーのハッシュ値");
  step(186, "h ^ (h >>> 16)", "上位のビットを混ぜる");
  step(372, "& (配列の大きさ − 1)", "下位のビットで位置を決める");
  step(558, "バケット", "配列の1つの要素");
  [150, 336, 522].forEach((x) => parts.push(`<path d="M${x + 2},72 L${x + 32},72" class="edge" marker-end="url(#arrow)"/>`));
  // バケットの中身
  const node = (x: number, y: number, cls: string, text: string) => {
    parts.push(`<rect x="${x}" y="${y}" width="46" height="26" rx="4" class="${cls}"/>`);
    parts.push(`<text x="${x + 23}" y="${y + 17}" text-anchor="middle" class="small" style="fill:#fff">${esc(text)}</text>`);
  };
  parts.push(`<text x="0" y="136" class="ink bold">衝突が少ないとき：連結リスト（Node）</text>`);
  node(0, 148, "f1", "A");
  parts.push(`<path d="M48,161 L66,161" class="edge" marker-end="url(#arrow)"/>`);
  node(70, 148, "f1", "B");
  parts.push(`<text x="130" y="166" class="muted small">同じバケットの要素を順にたどって equals で探す</text>`);
  parts.push(`<text x="0" y="206" class="ink bold">1つのバケットに8個を超え、配列が64以上のとき：赤黒木（TreeNode）</text>`);
  const tree: [number, number, string][] = [[85, 218, "M"], [30, 256, "D"], [140, 256, "T"], [0, 294, "A"], [56, 294, "G"], [112, 294, "P"], [168, 294, "X"]];
  const edges: [number, number][] = [[0, 1], [0, 2], [1, 3], [1, 4], [2, 5], [2, 6]];
  edges.forEach(([a, b]) => parts.push(`<line x1="${tree[a][0] + 23}" y1="${tree[a][1] + 26}" x2="${tree[b][0] + 23}" y2="${tree[b][1]}" class="edge"/>`));
  tree.forEach(([x, y, t]) => node(x, y, "f2", t));
  parts.push(`<text x="240" y="262" class="muted small">ハッシュ値、または compareTo（キーが Comparable のとき）で</text>`);
  parts.push(`<text x="240" y="280" class="muted small">左右に振り分けて探す。比べられないと、両方の枝を探すことになる</text>`);
  writeSvg(join(OUT, "buckets.svg"), svgDoc(720, 334, title, parts.join("\n")));
}
