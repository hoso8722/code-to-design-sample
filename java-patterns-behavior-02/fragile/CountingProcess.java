// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// 子クラス：検査した行の数を数える（親が validate を、1行につき1回だけ呼ぶと考えている）
public class CountingProcess extends BaseProcess {
    public int validated;

    @Override
    protected boolean validate(String line) {
        validated++;
        return super.validate(line);
    }
}
