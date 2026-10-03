// 実行: npx tsx data/jvm-execution-01/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { hBarChart, lineChart, stackedBarChart, writeSvg, fmt } from "../../scripts/lib/svg-chart.js";

const DATA = "data/jvm-execution-01";
const OUT = "site/public/images/jvm-execution-01";

// 1. 起動から終了までの時間（1回目を除く10回の平均）
{
  const [, ...lines] = readFileSync(join(DATA, "startup.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).filter(([, run]) => run !== "1");
  const mean = (p: string) => {
    const xs = rows.filter(([prog]) => prog === p).map(([, , ms]) => Number(ms));
    return xs.reduce((a, b) => a + b, 0) / xs.length;
  };
  const createVm = Number(readFileSync(join(DATA, "create-vm.txt"), "utf-8").match(/Create VM, ([\d.]+) secs/)![1]) * 1000;
  writeSvg(
    join(OUT, "startup.svg"),
    hBarChart({
      title: "java コマンドの実行から終了までの時間（10回の平均）",
      unit: "ミリ秒",
      labelWidth: 250,
      xTicks: [0, 200, 400, 600],
      rows: [
        { label: "（参考）JVMの作成（Create VM）", value: createVm, valueLabel: `${fmt(createVm)} ms`, slot: 3 },
        { label: "Hello（出力するだけ）", value: mean("Hello"), valueLabel: `${fmt(mean("Hello"))} ms`, slot: 1 },
        { label: "HttpHello（HttpClient を作る）", value: mean("HttpHello"), valueLabel: `${fmt(mean("HttpHello"))} ms`, slot: 2 },
      ],
    })
  );
}

// 2. 読み込まれたクラスの数（読み込み元ごと）
{
  const [, ...lines] = readFileSync(join(DATA, "class-load.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(","));
  writeSvg(
    join(OUT, "class-sources.svg"),
    stackedBarChart({
      title: "読み込まれたクラスの数と、読み込み元",
      unit: "個",
      labelWidth: 110,
      segments: [
        { name: "CDSのアーカイブ", slot: 1 },
        { name: "JDKのモジュール（jrt）", slot: 2 },
        { name: "実行中に生成", slot: 3 },
        { name: "アプリケーション", slot: 4 },
      ],
      rows: rows.map(([p, total, cds, jrt, app, gen]) => ({
        label: p,
        values: [Number(cds), Number(jrt), Number(gen), Number(app)],
        total: `${fmt(Number(total))} 個`,
      })),
    })
  );
}

// 3. HttpHello でクラスが読み込まれていく様子（累積）
{
  const log = readFileSync(join(DATA, "class-load-HttpHello.log"), "utf-8").trim().split("\n");
  const kinds = { cds: 0, jrt: 0, gen: 0 };
  const points: Record<keyof typeof kinds, [number, number][]> = { cds: [[0, 0]], jrt: [[0, 0]], gen: [[0, 0]] };
  for (const line of log) {
    const t = Number(line.match(/^\[([\d.]+)s\]/)![1]) * 1000;
    const kind: keyof typeof kinds | null = line.includes("shared objects file") ? "cds" : line.includes("source: jrt:/") ? "jrt" : line.includes("source: file:") ? null : "gen";
    if (!kind) continue;
    kinds[kind]++;
    points[kind].push([t, kinds[kind]]);
  }
  writeSvg(
    join(OUT, "class-timeline.svg"),
    lineChart({
      title: "HttpHello：起動してからクラスが読み込まれていく様子（累積）",
      xLabel: "JVMの起動からの時間（ミリ秒）",
      yLabel: "読み込んだクラスの数",
      xTicks: [0, 100, 200, 300, 400, 500],
      yTicks: [0, 300, 600, 900, 1200],
      rightPad: 170,
      series: [
        { name: "CDSのアーカイブから", endLabel: `CDS ${kinds.cds}`, slot: 1, points: points.cds },
        { name: "JDKのモジュール（jrt）から", endLabel: `jrt ${kinds.jrt}`, slot: 2, points: points.jrt },
        { name: "実行中に生成", endLabel: `生成 ${kinds.gen}`, slot: 3, points: points.gen },
      ],
    })
  );
}
