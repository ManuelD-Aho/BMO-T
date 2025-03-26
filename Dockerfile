# Dockerfile pour BMO Meet
FROM openjdk:17-jdk-slim as build

# Répertoire de travail
WORKDIR /app

# Copier les fichiers Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Rendre le script Maven exécutable
RUN chmod +x ./mvnw

# Construire l'application
RUN ./mvnw clean package -DskipTests

# Image finale
FROM openjdk:17-jdk-slim

# Installation de JavaFX
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://download2.gluonhq.com/openjfx/17/openjfx-17-linux-x64_bin-sdk.zip && \
    unzip openjfx-17-linux-x64_bin-sdk.zip && \
    mv javafx-sdk-17 /opt/javafx-sdk-17 && \
    rm openjfx-17-linux-x64_bin-sdk.zip && \
    apt-get remove -y wget unzip && \
    apt-get autoremove -y && \
    apt-get clean

# Définir les variables d'environnement
ENV JAVAFX_HOME=/opt/javafx-sdk-17
ENV PATH_TO_FX=$JAVAFX_HOME/lib

WORKDIR /app

# Copier le JAR compilé
COPY --from=build /app/target/bmot-1.0-SNAPSHOT.jar /app/bmot.jar

# Ports exposés
EXPOSE 8888

# Point d'entrée pour le serveur
CMD ["java", "--module-path", "$PATH_TO_FX", "--add-modules", "javafx.controls,javafx.fxml,javafx.swing", "-jar", "bmot.jar", "--server"]