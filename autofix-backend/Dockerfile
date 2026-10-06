# La imagen se construye a partir del .jar ya generado (mvn package), igual que en clases.
FROM eclipse-temurin:17-jre
ARG JAR_FILE=target/autofix-backend.jar
COPY ${JAR_FILE} autofix-backend.jar
EXPOSE 8090
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /autofix-backend.jar"]
