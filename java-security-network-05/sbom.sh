#!/bin/bash
# SBOMを出力して、中身を確かめる(sbom/ の中で実行する)
cd "$(dirname "$0")/sbom"
echo '$ mvn org.cyclonedx:cyclonedx-maven-plugin:2.9.3:makeBom -DoutputFormat=json -DoutputName=bom'
mvn -ntp -B -q org.cyclonedx:cyclonedx-maven-plugin:2.9.3:makeBom -DoutputFormat=json -DoutputName=bom 2>&1 | grep -v "WARNING\] Unknown keyword"
echo '$ jq -c ".components[] | {name, version, purl}" target/bom.json'
jq -c '.components[] | {name, version, purl}' target/bom.json
echo '$ jq -r ".components[0] | .name, (.hashes[] | select(.alg==\"SHA-1\") | .content)" target/bom.json'
jq -r '.components[0] | .name, (.hashes[] | select(.alg=="SHA-1") | .content)' target/bom.json
echo '$ curl -s https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-core/2.25.5/log4j-core-2.25.5.jar.sha1'
curl -s https://repo.maven.apache.org/maven2/org/apache/logging/log4j/log4j-core/2.25.5/log4j-core-2.25.5.jar.sha1
echo
