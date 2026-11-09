# CL09-12: o mesmo Dockerfile simples do doc (JRE 25 + JAR copiado); a imagem entra no k3s sem registry.
FROM eclipse-temurin:25-jre
COPY app.jar /app.jar
RUN useradd -r app
USER app
ENTRYPOINT ["java", "-jar", "/app.jar"]
