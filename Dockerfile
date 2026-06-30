# =============================================================================
# 后端多阶段 Dockerfile
# 阶段1：用 JDK 21 + Maven 构建 jar（利用 BuildKit 缓存 ~/.m2 加速重复构建）
# 阶段2：JRE 21 运行；用 layered jar 拆出依赖层，业务代码变更时镜像层增量极小
# =============================================================================

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# 先只 copy pom.xml 以利用 Docker 缓存：依赖未变时 mvn dependency:go-offline 走缓存
COPY pom.xml ./
COPY .mvn/ .mvn/ 2>/dev/null || true
RUN apt-get update && apt-get install -y --no-install-recommends maven \
 && mvn -B -q -DskipTests dependency:go-offline

# 再拷贝源码并构建
COPY src ./src
RUN mvn -B -q -DskipTests package \
 && mkdir -p target/extracted \
 && java -Djarmode=layertools -jar target/SMBMS.jar extract --destination target/extracted

# ---------- 运行镜像 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 按层拷贝（Spring Boot 3 layered jar），改业务代码时只重建 application 层
COPY --from=builder /workspace/target/extracted/dependencies/         ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/   ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/          ./

EXPOSE 8080
# 用 Spring Boot Loader 启动；container env 优先注入 SMBMS_DB_* / SMBMS_JWT_SECRET
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
