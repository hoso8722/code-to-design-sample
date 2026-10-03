// 実行: npx tsx data/java-collections-05/charts.ts
import { join } from "node:path";
import { svgDoc, writeSvg, esc } from "../../scripts/lib/svg-chart.js";

const OUT = "site/public/images/java-collections-05";

// Map に入れたあとでキーを変更すると、なぜ見つからないか（模式図）
const parts: string[] = [];
const title = "put したあとで、キーの name を alice から bob に変えたとき";
parts.push(`<text x="0" y="18" class="title">${esc(title)}</text>`);
// バケットの配列
const cells = 16, cw = 34, left = 140, top = 60;  // HashMap のデフォルトの大きさ 16
parts.push(`<text x="0" y="${top + 24}" class="ink bold">HashMap の配列</text>`);
for (let i = 0; i < cells; i++) {
  parts.push(`<rect x="${left + i * cw}" y="${top}" width="${cw - 4}" height="36" rx="4" class="box"/>`);
  parts.push(`<text x="${left + i * cw + (cw - 4) / 2}" y="${top + 23}" text-anchor="middle" class="muted small">${i}</text>`);
}
// エントリ（alice のハッシュ値で入ったまま）
const aliceIdx = 9, bobIdx = 4;  // "alice"・"bob" の hashCode から計算した位置（配列の大きさ 16 のとき）
const ex = left + aliceIdx * cw - 33;
parts.push(`<rect x="${ex}" y="${top + 70}" width="100" height="58" rx="6" class="f1"/>`);
parts.push(`<text x="${ex + 50}" y="${top + 92}" text-anchor="middle" class="small bold" style="fill:#fff">エントリ</text>`);
parts.push(`<text x="${ex + 50}" y="${top + 110}" text-anchor="middle" class="small" style="fill:#fff">キー：User(bob)</text>`);
parts.push(`<text x="${ex + 50}" y="${top + 124}" text-anchor="middle" class="small" style="fill:#fff">hash：alice の値</text>`);
parts.push(`<path d="M${left + aliceIdx * cw + 15},${top + 38} L${left + aliceIdx * cw + 15},${top + 66}" class="edge" marker-end="url(#arrow)"/>`);
// 探しに行く先
const note = (x: number, y: number, lines: string[]) => lines.forEach((l, i) => parts.push(`<text x="${x}" y="${y + i * 18}" class="muted small">${esc(l)}</text>`));
parts.push(`<path d="M${left + bobIdx * cw + 15},${top + 100} L${left + bobIdx * cw + 15},${top + 40}" class="edge dash" marker-end="url(#arrow)"/>`);
note(0, top + 118, ["get(user) は、今の name（bob）の", "ハッシュ値で 4 番を探す → 空"]);
note(left + aliceIdx * cw - 60, top + 160, ["get(new User(\"alice\")) は 9 番を探すが、", "キーの equals（bob ≠ alice）で一致しない"]);
parts.push(`<text x="0" y="${top + 214}" class="muted">エントリは、put したときのハッシュ値で入った場所に残り、反復すると見えるが、どのキーでも取り出せない</text>`);
writeSvg(join(OUT, "mutable-key.svg"), svgDoc(720, top + 226, title, parts.join("\n")));
