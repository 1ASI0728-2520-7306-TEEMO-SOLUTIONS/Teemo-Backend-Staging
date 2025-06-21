FROM eclipse-temurin:17-jre AS builder

WORKDIR /app

COPY . .

# Fix Windows line endings and add execute permissions
RUN sed -i 's/\r$//' ./mvnw
RUN chmod +x ./mvnw

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]