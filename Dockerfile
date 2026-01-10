# ============================================
# ステージ1: ビルド（Maven + JDK）
# ============================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

# Maven キャッシュレイヤー最適化
# pom.xml をコピーして依存関係をダウンロード
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 依存関係をダウンロード（キャッシュレイヤー）
RUN ./mvnw dependency:resolve dependency:resolve-plugins -q

# ソースコードをコピーしてビルド
COPY src src

# プロジェクトをビルド（テストスキップ）
RUN ./mvnw clean package -DskipTests -q && \
    ls -lh target/*.jar

# ============================================
# ステージ2: 実行（軽量JRE）
# ============================================
FROM eclipse-temurin:21-jre-jammy

# タイムゾーン設定
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# アプリケーション用ディレクトリ作成
WORKDIR /app

# セキュリティ: 非rootユーザーを作成
RUN groupadd -r appuser && useradd -r -g appuser appuser

# ビルドステージから JAR ファイルをコピー
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R appuser:appuser /app

# 非rootユーザーで実行
USER appuser

# ポート公開
EXPOSE 8080

# ヘルスチェック（オプション）
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher -Dhealth=true || exit 1

# JVM メモリオプション付きでアプリケーション起動
# コンテナ起動時に環境変数 JAVA_OPTS でカスタマイズ可能
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
