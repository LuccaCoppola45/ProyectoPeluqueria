# Stage 1: Build Angular
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY peluqueria-frontend/package*.json ./
RUN npm ci
COPY peluqueria-frontend/ ./
RUN npm run build -- --configuration production

# Stage 2: Build Spring Boot
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
# Copiar el build de Angular a los recursos estáticos
RUN mkdir -p /app/src/main/resources/static
COPY --from=frontend-build /app/frontend/dist/peluqueria-frontend/browser/ /app/src/main/resources/static/
RUN ls -la /app/src/main/resources/static/ | head -25
RUN mvn clean package -DskipTests

# Stage 3: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
