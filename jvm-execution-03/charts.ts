// 実行: npx tsx data/jvm-execution-03/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, timelineChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/jvm-execution-03";
const OUT = "site/public/images/jvm-execution-03";

// 1. Tiers のメソッドが、いつ・どのティアでコンパイルされたか（-XX:+PrintCompilation の記録から）
{
  const lines = readFileSync(join(DATA, "print-compilation.txt"), "utf-8").split("\n").filter((l) => l.includes("Tiers::"));
  interface Event { t: number; id: number; osr: boolean; tier: number; method: string; notEntrant: boolean }
  const events: Event[] = lines.map((l) => {
    const m = l.match(/^\s*(\d+)\s+(\d+)\s+(.{6})\s*(\d)\s+Tiers::(\w+)/)!;
    return { t: Number(m[1]), id: Number(m[2]), osr: m[3].includes("%"), tier: Number(m[4]), method: m[5], notEntrant: l.includes("made not entrant") };
  });
  const end = 240;  // 図の右端（処理が終わるころ）
  const laneOf = (e: Event) => (e.osr ? `${e.method}（OSR）` : e.method);
  const lanes = ["digitSum", "sumOfSquares", "sumOfSquares（OSR）", "main", "main（OSR）"];
  const bars = [];
  for (const e of events.filter((x) => !x.notEntrant)) {
    const retired = events.find((x) => x.notEntrant && x.id === e.id);
    bars.push({ lane: laneOf(e), start: e.t, end: retired ? retired.t : end, slot: e.tier === 4 ? 1 : 2, label: `ティア${e.tier}` });
  }
  writeSvg(
    join(OUT, "tiers-timeline.svg"),
    timelineChart({
      title: "Tiers のメソッドのコンパイル済みコードが使われていた期間（PrintCompilation から）",
      xLabel: "JVMの起動からの時間（ミリ秒）",
      lanes,
      laneWidth: 150,
      xTicks: [0, 50, 100, 150, 200, 250],
      legendItems: [
        { name: "ティア3（C1・プロファイルを集める）", slot: 2 },
        { name: "ティア4（C2）", slot: 1 },
      ],
      bars,
    })
  );
}

// 2. 3つのモードの処理時間（3回の平均）
{
  const [, ...lines] = readFileSync(join(DATA, "modes.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(","));
  const mean = (m: string) => {
    const xs = rows.filter(([mode]) => mode === m).map(([, , ms]) => Number(ms));
    return xs.reduce((a, b) => a + b, 0) / xs.length / 1000;
  };
  writeSvg(
    join(OUT, "modes.svg"),
    hBarChart({
      title: "同じ処理（Tiers）の処理時間（3回の平均）",
      unit: "秒",
      labelWidth: 280,
      xTicks: [0, 5, 10, 15, 20],
      rows: [
        { label: "通常（C1 → C2）", value: mean("normal"), valueLabel: `${fmt(mean("normal"), 2)} 秒`, slot: 1 },
        { label: "-XX:TieredStopAtLevel=1（C1だけ）", value: mean("c1"), valueLabel: `${fmt(mean("c1"), 2)} 秒`, slot: 2 },
        { label: "-Xint（インタプリタだけ）", value: mean("xint"), valueLabel: `${fmt(mean("xint"), 1)} 秒`, slot: 3 },
      ],
    })
  );
}
