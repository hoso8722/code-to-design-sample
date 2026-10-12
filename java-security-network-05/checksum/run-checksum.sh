#!/bin/bash
# 偽のリポジトリを起動して、チェックサムの検証を、既定(警告)と厳格(失敗)で確かめる
cd "$(dirname "$0")"
./make-repo.sh > /dev/null
java RepoServer.java repo 8099 > server.log 2>&1 &
SERVER=$!
sleep 3
for option in "" "-C"; do
    rm -rf "$TMPDIR_LOCAL" ; TMPDIR_LOCAL=$(mktemp -d)
    echo "\$ mvn -f consumer/pom.xml dependency:resolve $option"
    mvn -ntp -B -f consumer/pom.xml -Dmaven.repo.local="$TMPDIR_LOCAL" dependency:resolve $option 2>&1 \
        | grep -E "Checksum|checksum|fake-lib|BUILD|ERROR" | sed "s#$TMPDIR_LOCAL#<ローカルリポジトリ>#g" | grep -v "^    at " | cut -c1-300
done
echo '$ サーバーのログ'
cat server.log
kill $SERVER
rm -rf "$TMPDIR_LOCAL" server.log
