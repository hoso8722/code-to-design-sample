// 実行: npx tsx data/java-async-06/charts.ts
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { svgDoc, timelineChart, writeSvg, esc } from "../../scripts/lib/svg-chart.js";

const DATA = "data/java-async-06";
const OUT = "site/public/images/java-async-06";

const at = (text: string, pattern: string) => {
  const m = text.match(new RegExp(`^\\s*(\\d+) ms .*${pattern}`, "m"));
  if (!m) throw new Error(`見つかりません: ${pattern}`);
  return Number(m[1]);
};

// 1. 例外がどこまで伝わるか（propagation.txt の結果を図にする）
{
  const text = readFileSync(join(DATA, "propagation.txt"), "utf-8");
  const [first, second] = text.split("== 段3の前に");
  const ran = (part: string, n: number) => part.includes(`段${n} を実行した`);
  const parts: string[] = [];
  const title = "段2で例外が起きたとき、どの段が実行されたか";
  parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
  const box = (x: number, y: number, label: string, state: "ran" | "fail" | "skip" | "recover") => {
    const cls = { ran: "f1", fail: "f2", skip: "neutral", recover: "f3" }[state];
    parts.push(`<rect x="${x}" y="${y}" width="120" height="40" rx="6" class="${cls}"/>`);
    parts.push(`<text x="${x + 60}" y="${y + 25}" text-anchor="middle" class="small bold" style="fill:${state === "skip" ? "var(--ink)" : "#fff"}">${esc(label)}</text>`);
  };
  const arrow = (x1: number, x2: number, y: number) => parts.push(`<path d="M${x1},${y} L${x2 - 4},${y}" class="edge" marker-end="url(#arrow)"/>`);
  // (a) exceptionally なし
  parts.push(`<text x="0" y="56" class="ink bold">(a) そのままつないだ場合</text>`);
  const y1 = 68;
  const stateA = (n: number) => (n === 2 ? "fail" : ran(first, n) ? "ran" : "skip");
  [1, 2, 3, 4].forEach((n, i) => {
    box(i * 150, y1, n === 2 ? "段2（例外）" : ran(first, n) ? `段${n}` : `段${n}（飛ばす）`, stateA(n) as "ran" | "fail" | "skip");
    if (i > 0) arrow(i * 150 - 30, i * 150, y1 + 20);
  });
  box(600, y1, "join() で例外", "fail");
  arrow(570, 600, y1 + 20);
  // (b) exceptionally あり
  parts.push(`<text x="0" y="146" class="ink bold">(b) 段2のあとに exceptionally を入れた場合</text>`);
  const y2 = 158;
  box(0, y2, "段1", "ran");
  box(150, y2, "段2（例外）", "fail");
  box(300, y2, "exceptionally", "recover");
  box(450, y2, ran(second, 3) ? "段3（-1 を受け取る）" : "段3（飛ばす）", ran(second, 3) ? "ran" : "skip");
  box(600, y2, "結果: 0", "ran");
  [150, 300, 450, 600].forEach((x) => arrow(x - 30, x, y2 + 20));
  parts.push(`<text x="0" y="230" class="muted">青：実行された段　橙：例外が起きた段　緑：例外を受け取って値に置き換えた段　灰：飛ばされた段</text>`);
  writeSvg(join(OUT, "propagation.svg"), svgDoc(720, 242, title, parts.join("\n")));
}

// 2. orTimeout / completeOnTimeout
{
  const t = readFileSync(join(DATA, "timeouts.txt"), "utf-8");
  writeSvg(
    join(OUT, "timeouts.svg"),
    timelineChart({
      title: "300ミリ秒かかる処理に、100ミリ秒のタイムアウトを付けたとき",
      xLabel: "時間（ミリ秒）",
      lanes: ["A の処理", "main（A を待つ）", "B の処理", "main（B を待つ）"],
      laneWidth: 140,
      xTicks: [0, 200, 400, 600, 800],
      legendItems: [
        { name: "処理", slot: 1 },
        { name: "タイムアウト後も動いている", slot: 2 },
        { name: "main が待っている", slot: 0 },
      ],
      bars: [
        { lane: "A の処理", start: at(t, "A: 開始"), end: at(t, "TimeoutException"), slot: 1 },
        { lane: "A の処理", start: at(t, "TimeoutException"), end: at(t, "A: 最後まで"), slot: 2, label: "動き続ける" },
        { lane: "main（A を待つ）", start: at(t, "A: 開始"), end: at(t, "TimeoutException"), slot: 0, label: "orTimeout" },
        { lane: "B の処理", start: at(t, "B: 開始"), end: at(t, "B の結果"), slot: 1 },
        { lane: "B の処理", start: at(t, "B の結果"), end: at(t, "B: 最後まで"), slot: 2, label: "動き続ける" },
        { lane: "main（B を待つ）", start: at(t, "B: 開始"), end: at(t, "B の結果"), slot: 0, label: "completeOn…" },
      ],
    })
  );
}

// 3. allOf で1つが失敗したとき
{
  const t = readFileSync(join(DATA, "allof.txt"), "utf-8");
  writeSvg(
    join(OUT, "allof.svg"),
    timelineChart({
      title: "allOf(A, B, C)：A が100ミリ秒で失敗したとき",
      xLabel: "時間（ミリ秒）",
      lanes: ["A（100ms・失敗）", "B（300ms）", "C（500ms）", "main（allOf を待つ）"],
      laneWidth: 150,
      xTicks: [0, 100, 200, 300, 400, 500, 600],
      legendItems: [
        { name: "成功した処理", slot: 1 },
        { name: "失敗した処理", slot: 2 },
        { name: "main が待っている", slot: 0 },
      ],
      bars: [
        { lane: "A（100ms・失敗）", start: 0, end: at(t, "A\\(100ms, 失敗\\): 失敗した"), slot: 2 },
        { lane: "B（300ms）", start: 0, end: at(t, "B\\(300ms\\): 成功した"), slot: 1 },
        { lane: "C（500ms）", start: 0, end: at(t, "C\\(500ms\\): 成功した"), slot: 1 },
        { lane: "main（allOf を待つ）", start: 0, end: at(t, "allOf が例外で終わった"), slot: 0, label: "すべて終わるまで例外は届かない" },
      ],
      markers: [{ x: at(t, "A\\(100ms, 失敗\\): 失敗した"), label: "A が失敗" }],
    })
  );
}
