# data/

記事の計測に使ったコードと、計測結果（生データ）を記事ごとに置く。

```text
data/
├── environment.txt        計測環境（コードで見るJavaのメモリ）
├── environment-java-async.txt  計測環境（コードで見るJavaの非同期処理）
├── environment-java-exceptions.txt  計測環境（コードで学ぶJavaの例外設計）
├── environment-java-io.txt  計測環境（コードで見るJavaの入出力とネットワーク）
└── <記事id>/
    ├── src/*.java         計測に使ったプログラム
    ├── *.csv / *.txt      計測結果（jcmd や GC ログの出力を含む）
    └── charts.ts          計測結果から図（site/public/images/<記事id>/*.svg）を作るスクリプト
```

図の作り直し：

```bash
npx tsx data/<記事id>/charts.ts
```

計測値は環境で変わる。記事に載せる数値は、このディレクトリにある結果と一致させる。
計測のときは `javac` でコンパイルしてから `java -cp` で実行する（ソースファイルモードの `java Foo.java` はコンパイラも同じプロセスで動くため、メモリの値が大きく出る）。
