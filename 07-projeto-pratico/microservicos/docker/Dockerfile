# PP13: imagem simples (JRE 25 + jar executável). O mesmo Dockerfile serve aos dois serviços (ARG JAR).
FROM eclipse-temurin:25-jre
COPY app.jar /app.jar
RUN useradd -r app
USER app
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app.jar"]
