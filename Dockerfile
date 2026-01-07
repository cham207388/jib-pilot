FROM eclipse-temurin:21-jdk

WORKDIR /workspace

# Copy wrapper and gradle files to allow wrapper execution (overridden by bind mount at runtime)
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle gradle

RUN chmod +x gradlew && ./gradlew --no-daemon --version

EXPOSE 8085
# Source will be bind-mounted for hot reload; bootRun runs inside container
CMD ["./gradlew", "--no-daemon", "bootRun"]
