#!/bin/bash
# 実行: bash git-history.sh
# 秘密を誤ってコミットし、あとでファイルから消しても、履歴に残ることを確かめる(一時フォルダの中だけで行う)
set -e
work=$(mktemp -d)
cd "$work"
git init -q
git config user.email demo@example.com
git config user.name demo

echo 'db.password=abc123-demo-secret' > application.properties
git add application.properties
git commit -q -m "設定を追加する"

echo 'db.password=${DB_PASSWORD}' > application.properties
git add application.properties
git commit -q -m "パスワードを環境変数に変える"

echo '$ cat application.properties   (今のファイル)'
cat application.properties
echo
echo '$ git grep abc123 (今の作業ツリー)'
git grep abc123 || echo '(見つからない)'
echo
echo '$ git log -p --all -S abc123 -- application.properties   (履歴を検索する)'
git log -p --all -S abc123 --format='commit %h %s' -- application.properties | grep -E '^(commit|[-+]db.password)'
