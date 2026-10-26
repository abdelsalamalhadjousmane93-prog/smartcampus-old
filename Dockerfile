FROM eclipse-temurin:17-jre

WORKDIR /app

# Non-root user + folder for persistent data (mounted as a volume)
RUN groupadd -r app && useradd -r -g app app \
 && mkdir /data && chown app:app /data

# The JAR produced by Jenkins (downloaded into target/)
COPY target/smartcampus-1.0.0.jar app.jar

USER app
ENV PORT=8080 DATA_DIR=/data
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD ["java", "-cp", "app.jar", "com.smartcampus.HealthCheck"]

ENTRYPOINT ["java", "-jar", "app.jar"]
