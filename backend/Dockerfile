# Imagem de execucao da API Solidari.
# O jar e gerado antes pelo pipeline (./gradlew :backend:bootJar) e so copiado
# aqui: a imagem nao precisa de JDK nem de Gradle, so do JRE.
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN useradd --system --uid 1001 --no-create-home solidari

COPY build/libs/solidari-backend.jar /app/app.jar

USER solidari

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
