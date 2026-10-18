# Javaのコードを動作させるためのイメージ
FROM eclipse-temurin:21-jdk

# ライブラリのJARファイルを、Maven Centralから /opt/lib に入れる
ARG MAVEN=https://repo1.maven.org/maven2
ADD ${MAVEN}/org/junit/platform/junit-platform-console-standalone/6.1.3/junit-platform-console-standalone-6.1.3.jar /opt/lib/
ADD ${MAVEN}/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37.jar /opt/lib/
ADD ${MAVEN}/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar /opt/lib/
ADD ${MAVEN}/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar /opt/lib/
ADD ${MAVEN}/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar /opt/lib/
# URLから ADD したファイルは、所有者だけが読める（600）ので、誰でも読めるようにする
RUN chmod 644 /opt/lib/*.jar

WORKDIR /work
