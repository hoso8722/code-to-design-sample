// 実行: npx tsx data/jvm-execution-05/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { lineChart, timelineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/jvm-execution-05";
const OUT = "site/public/images/jvm-execution-05";

// 1. 1回ずつの処理時間（trace.csv：ログを取らずに実行したもの。ログを取ると標準出力に記録が混ざるため）
{
  const [, ...lines] = readFileSync(join(DATA, "trace.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([call, , , us]) => [Number(call), Math.log10(Number(us))] as [number, number]);
  writeSvg(
    join(OUT, "trace.svg"),
    lineChart({
      title: "total() を600回呼んだときの、1回あたりの時間（301回目から Rect が混ざる）",
      xLabel: "何回目の呼び出しか",
      yLabel: "1回あたりの時間（対数目盛り）",
      xTicks: [0, 100, 200, 300, 400, 500, 600],
      yTicks: [0, 1, 2, 3, 4],
      yFormat: (v) => (10 ** v >= 1000 ? `${fmt(10 ** v / 1000)} ms` : `${fmt(10 ** v)} µs`),
      rightPad: 40,
      endLabels: false,
      series: [{ name: "total()", slot: 1, points: rows }],
      markers: [{ x: 300.5, label: "ここから Rect も混ざる" }],
    })
  );
}

// 2. total() のコンパイル済みコードが使われていた期間と、脱最適化
{
  const lines = readFileSync(join(DATA, "print-compilation-total.txt"), "utf-8").split("\n").filter((l) => l.includes("Deopt::total"));
  const events = lines.map((l) => {
    const m = l.match(/^\s*(\d+)\s+(\d+)\s+(.{6})\s*(\d)\s+Deopt::total/)!;
    return { t: Number(m[1]), id: Number(m[2]), osr: m[3].includes("%"), tier: Number(m[4]), retired: l.includes("made not entrant") };
  });
  const deopts = readFileSync(join(DATA, "deopt-log.txt"), "utf-8")
    .split("\n")
    .filter((l) => l.includes("Deopt.total"))
    .map((l) => ({ t: Math.round(Number(l.match(/^\[([\d.]+)s\]/)![1]) * 1000), reason: l.includes("class_check") ? "class_check" : "unstable_if" }));
  const end = 280;
  const bars = events
    .filter((e) => !e.retired)
    .map((e) => {
      const r = events.find((x) => x.retired && x.id === e.id);
      return { lane: e.osr ? "total（OSR）" : "total", start: e.t, end: r ? r.t : end, slot: e.tier === 4 ? 1 : 2, label: `#${e.id}` };
    });
  const first = (reason: string) => deopts.find((d) => d.reason === reason)!.t;
  writeSvg(
    join(OUT, "deopt-timeline.svg"),
    timelineChart({
      title: "total() のティア4のコードと、脱最適化（uncommon trap）の時点",
      xLabel: "JVMの起動からの時間（ミリ秒）",
      lanes: ["total（OSR）", "total"],
      laneWidth: 110,
      xTicks: [0, 40, 80, 120, 160, 200, 240, 280],
      bars,
      markers: [
        { x: first("unstable_if"), label: "unstable_if" },
        { x: first("class_check"), label: "class_check" },
      ],
    })
  );
}
