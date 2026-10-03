// 実行: npx tsx data/java-collections-07/charts.ts
import { join } from "node:path";
import { hBarChart, svgDoc, writeSvg, esc } from "../../scripts/lib/svg-chart.js";
import { nsLabel, readJmhCsv } from "../../scripts/lib/jmh.js";

const DATA = "data/java-collections-07";
const OUT = "site/public/images/java-collections-07";
const rows = readJmhCsv(join(DATA, "jmh-StreamVsLoop.csv"));
const get = (b: string, n: string) => rows.find((r) => r.benchmark === b && r.params.n === n)!.score;
const log = Math.log10;

// 1. 合計の時間（要素数 100 と 1,000,000。横軸は対数）
{
  const benches: [string, string, number][] = [
    ["sumForArray", "int[] を for", 1],
    ["sumIntStream", "int[] を IntStream", 3],
    ["sumMapToLong", "List を mapToLong して sum", 4],
    ["sumBoxedStream", "List を Stream<Integer> で reduce", 2],
  ];
  writeSvg(
    join(OUT, "sum.svg"),
    hBarChart({
      title: "合計を求める1回の時間（JMH、横軸は対数目盛り）",
      unit: "1回あたりの時間",
      labelWidth: 250,
      xTicks: [1, 2, 3, 4, 5, 6, 7],
      format: (v) => nsLabel(10 ** v),
      groups: { 0: "要素数 100", 4: "要素数 1,000,000" },
      rows: ["100", "1000000"].flatMap((n) =>
        benches.map(([b, label, slot]) => {
          const ns = get(b, n) * 1000;  // µs → ns
          return { label, value: log(ns), valueLabel: nsLabel(ns), slot };
        })
      ),
    })
  );
}

// 2. 1回あたりに確保したメモリ（要素数 1,000,000、-prof gc）
{
  const benches: [string, string][] = [
    ["sumForArray", "int[] を for"],
    ["sumIntStream", "int[] を IntStream"],
    ["sumMapToLong", "List を mapToLong して sum"],
    ["filterMapStream", "List を filter・mapToLong・sum"],
    ["sumBoxedStream", "List を Stream<Integer> で reduce"],
  ];
  const bytes = (b: string) => rows.find((r) => r.benchmark === `${b}:gc.alloc.rate.norm` && r.params.n === "1000000")!.score;
  const label = (v: number) => (v >= 1e6 ? `${(v / 1e6).toFixed(1)} MB` : `${Math.round(v)} B`);
  writeSvg(
    join(OUT, "alloc.svg"),
    hBarChart({
      title: "要素数 1,000,000 で1回の処理に確保したメモリ（JMH の -prof gc、横軸は対数目盛り）",
      unit: "バイト",
      labelWidth: 250,
      xTicks: [0, 2, 4, 6, 8],
      format: (v) => (v === 0 ? "1 B" : v === 2 ? "100 B" : v === 4 ? "10 KB" : v === 6 ? "1 MB" : "100 MB"),
      rows: benches.map(([b, l]) => ({ label: l, value: log(Math.max(bytes(b), 1)), valueLabel: label(bytes(b)), slot: b === "sumBoxedStream" ? 2 : 1 })),
    })
  );
}

// 3. 要素がパイプラインを流れる順（Lazy.java の2の出力を図にしたもの）
{
  const parts: string[] = [];
  const title = "ストリームの要素は、1つずつ filter → map → toList を最後まで流れる";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const stages = ["numbers", "filter(偶数)", "map(×10)", "toList"];
  const colX = [40, 200, 360, 520];
  stages.forEach((s, i) => parts.push(`<text x="${colX[i] + 50}" y="50" text-anchor="middle" class="ink bold small">${esc(s)}</text>`));
  [1, 2, 3, 4, 5, 6].forEach((v, row) => {
    const y = 64 + row * 34;
    const pass = v % 2 === 0;
    const box = (i: number, text: string, cls: string) => {
      parts.push(`<rect x="${colX[i] + 20}" y="${y}" width="60" height="24" rx="4" class="${cls}"/>`);
      parts.push(`<text x="${colX[i] + 50}" y="${y + 16}" text-anchor="middle" class="small" style="fill:${cls === "neutral" ? "var(--ink)" : "#fff"}">${esc(text)}</text>`);
    };
    box(0, String(v), "f1");
    parts.push(`<path d="M${colX[0] + 82},${y + 12} L${colX[1] + 16},${y + 12}" class="edge" marker-end="url(#arrow)"/>`);
    box(1, pass ? "通す" : "捨てる", pass ? "f1" : "neutral");
    if (pass) {
      parts.push(`<path d="M${colX[1] + 82},${y + 12} L${colX[2] + 16},${y + 12}" class="edge" marker-end="url(#arrow)"/>`);
      box(2, String(v * 10), "f1");
      parts.push(`<path d="M${colX[2] + 82},${y + 12} L${colX[3] + 16},${y + 12}" class="edge" marker-end="url(#arrow)"/>`);
      box(3, "追加", "f3");
    }
  });
  parts.push(`<text x="0" y="284" class="muted">上から順に処理される。1つの要素が最後まで流れてから、次の要素が流れ始める（Lazy.java の出力の順）</text>`);
  writeSvg(join(OUT, "pipeline.svg"), svgDoc(720, 296, title, parts.join("\n")));
}
