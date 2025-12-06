FROM eclipse-temurin:21-jdk
WORKDIR /app

# Maven wrapper ve pom.xml
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .

# Dependency indir (cache)
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Kaynak kodları kopyala
COPY src/ ./src/

# Build et
RUN ./mvnw package -DskipTests

# Çıkan jar’ı final imaja kopyala
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
