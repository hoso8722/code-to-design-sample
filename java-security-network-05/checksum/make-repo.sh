#!/bin/bash
# 偽のリポジトリを作る: fake-lib 1.0 の jar と pom。jar の .sha1 だけ、わざと違う値にする
set -e
cd "$(dirname "$0")"
rm -rf repo work
dir=repo/example/fake-lib/1.0
mkdir -p "$dir" work
cat > "$dir/fake-lib-1.0.pom" <<'POM'
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>example</groupId>
    <artifactId>fake-lib</artifactId>
    <version>1.0</version>
</project>
POM
echo "ここに、中身が書き換えられた jar があるとする" > work/README.txt
jar --create --file "$dir/fake-lib-1.0.jar" -C work README.txt
for f in fake-lib-1.0.pom fake-lib-1.0.jar; do
    sha1sum "$dir/$f" | cut -d' ' -f1 | tr -d '\n' > "$dir/$f.sha1"
done
# jar の .sha1 を、別の内容のハッシュに差し替える(書き換えを模す)
echo -n "別の内容" | sha1sum | cut -d' ' -f1 | tr -d '\n' > "$dir/fake-lib-1.0.jar.sha1"
rm -rf work
echo "作成した: $dir"
ls "$dir"
