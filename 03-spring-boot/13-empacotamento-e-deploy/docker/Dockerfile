FROM eclipse-temurin:25-jre AS builder
WORKDIR /builder
COPY app.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:25-jre
WORKDIR /application
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
RUN useradd -r app
USER app
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
