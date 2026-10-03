// 実行: npx tsx data/java-async-04/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { timelineChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-04";
const OUT = "site/public/images/java-async-04";

// 1. 投入した順に get() したときのタイムライン
{
  const [, ...lines] = readFileSync(join(DATA, "get-order.csv"), "utf-8").trim().split("\n");
  const rows = lines.map((l) => l.split(",")).map(([name, thread, s, e]) => ({ name, thread, start: Number(s), end: Number(e) }));
  const t0 = Math.min(...rows.map((r) => r.start));
  const lane = (r: (typeof rows)[number]) => (r.thread === "main" ? "main" : `${r.thread.replace("pool-1-", "")}：${r.name}`);
  const lanes = [...rows.filter((r) => r.thread !== "main").map(lane), "main"];
  writeSvg(
    join(OUT, "get-order.svg"),
    timelineChart({
      title: "300・100・200ミリ秒のタスクを投入し、投入した順に get() したとき",
      xLabel: "時間（ミリ秒）",
      lanes,
      laneWidth: 200,
      xTicks: [0, 100, 200, 300, 400],
      legendItems: [
        { name: "タスクの実行", slot: 1 },
        { name: "main が get() で待っている", slot: 0 },
      ],
      bars: rows.map((r) => ({
        lane: lane(r),
        start: r.start - t0,
        end: Math.max(r.end - t0, r.start - t0 + 2),
        slot: r.thread === "main" ? 0 : 1,
        label: r.thread === "main" ? r.name.replace(/\(.*/, "") + " の結果を待つ" : undefined,
      })),
    })
  );
}

// 2. get(timeout) と cancel(true)
{
  const text = readFileSync(join(DATA, "timeout-cancel.txt"), "utf-8").split("\n\n")[0];
  const at = (pattern: string) => {
    const m = text.match(new RegExp(`^\\s*(\\d+) ms .*${pattern}`, "m"));
    if (!m) throw new Error(`見つかりません: ${pattern}`);
    return Number(m[1]);
  };
  const cancelAt = at("cancel\\(true\\) する");
  writeSvg(
    join(OUT, "timeout-cancel.svg"),
    timelineChart({
      title: "get(100ミリ秒) でタイムアウトしたタスクと、cancel(true) したタスク",
      xLabel: "時間（ミリ秒）",
      lanes: ["main", "slow（sleep 300ms）", "polite（sleep 1秒）", "stubborn（計算 1秒）"],
      laneWidth: 170,
      xTicks: [0, 300, 600, 900, 1200, 1500],
      legendItems: [
        { name: "タスクの実行", slot: 1 },
        { name: "main が待っている", slot: 0 },
        { name: "取り消し後も動いている", slot: 2 },
      ],
      bars: [
        { lane: "main", start: at("slow: 開始"), end: at("TimeoutException"), slot: 0, label: "get(100ms)" },
        { lane: "slow（sleep 300ms）", start: at("slow: 開始"), end: at("slow: 最後まで"), slot: 1, label: "タイムアウト後も動く" },
        { lane: "polite（sleep 1秒）", start: at("polite: 開始"), end: at("polite: 割り込まれた"), slot: 1 },
        { lane: "stubborn（計算 1秒）", start: at("stubborn: 開始"), end: cancelAt, slot: 1 },
        { lane: "stubborn（計算 1秒）", start: cancelAt, end: at("stubborn: 最後まで"), slot: 2, label: "状態は CANCELLED なのに動き続ける" },
        { lane: "main", start: cancelAt, end: at("プールを閉じ終わった"), slot: 0, label: "close() で待つ" },
      ],
      markers: [
        { x: at("TimeoutException"), label: "TimeoutException" },
        { x: cancelAt, label: "cancel(true)" },
      ],
    })
  );
}
