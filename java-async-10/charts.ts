// 実行: npx tsx data/java-async-10/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { timelineChart, writeSvg } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-10";
const OUT = "site/public/images/java-async-10";

// fail-fast.txt の1回目を使う
const firstRun = readFileSync(join(DATA, "fail-fast.txt"), "utf-8").split("\n\n")[0];
const [structuredPart, allOfPart] = firstRun.split("== CompletableFuture.allOf");

function chart(part: string, title: string, file: string, mainLabel: string) {
  const at = (pattern: string) => {
    const m = part.match(new RegExp(`^\\s*(\\d+) ms  ${pattern}`, "m"));
    if (!m) throw new Error(`見つかりません: ${pattern}`);
    return Number(m[1]);
  };
  const t0 = at("A\\(100ms・失敗\\): 開始");
  const end = (name: string) => {
    const m = part.match(new RegExp(`^\\s*(\\d+) ms  ${name}: (成功した|失敗した|割り込まれて止まった)`, "m"))!;
    return { t: Number(m[1]) - t0, how: m[2] };
  };
  const lanes = ["A（100ms・失敗）", "B（300ms）", "C（500ms）", "main"];
  const names = ["A\\(100ms・失敗\\)", "B\\(300ms\\)", "C\\(500ms\\)"];
  const bars = names.map((n, i) => {
    const e = end(n);
    const slot = e.how === "成功した" ? 1 : e.how === "失敗した" ? 2 : 4;
    return { lane: lanes[i], start: Math.max(at(`${n}: 開始`) - t0, 0), end: e.t, slot, label: e.how === "割り込まれて止まった" ? "取り消し" : undefined };
  });
  const mainEnd = at(`main: ${mainLabel}`) - t0;
  bars.push({ lane: "main", start: 0, end: mainEnd, slot: 0, label: `${mainEnd} ms で例外を受け取る` });
  writeSvg(
    join(OUT, file),
    timelineChart({
      title,
      xLabel: "A が動き始めてからの時間（ミリ秒）",
      lanes,
      laneWidth: 140,
      xTicks: [0, 100, 200, 300, 400, 500, 600],
      legendItems: [
        { name: "成功", slot: 1 },
        { name: "失敗", slot: 2 },
        { name: "割り込まれて止まった", slot: 4 },
        { name: "main が待っている", slot: 0 },
      ].filter((item) => bars.some((b) => b.slot === item.slot)),  // 図に出てくる色だけ
      bars,
      markers: [{ x: end(names[0]).t, label: "A が失敗" }],
    })
  );
}

chart(structuredPart, "StructuredTaskScope.open()：子タスク A が失敗したとき（プレビュー機能）", "structured.svg", "join\\(\\) が FailedException");
chart(allOfPart, "CompletableFuture.allOf：A が失敗したとき", "allof.svg", "allOf が例外で終わった");
