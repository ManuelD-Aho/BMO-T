FROM openjdk:22-slim

# Installer wget, unzip et les dépendances pour JavaFX (GTK, X11, etc.)
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    libgtk-3-0 \
    libgl1-mesa-glx \
    libxi6 \
    libxrender1 \
    libxtst6 \
    libnss3 \
    && rm -rf /var/lib/apt/lists/*

# Télécharger JavaFX SDK 23.0.2 et l'extraire dans /opt/javafx
RUN wget https://download2.gluonhq.com/openjfx/23.0.2/openjfx-23.0.2_linux-x64_bin-sdk.zip -O openjfx.zip && \
    unzip openjfx.zip -d /opt/javafx && \
    rm openjfx.zip

WORKDIR /app

# Copier le JAR exécutable généré par Maven dans le conteneur
COPY target/BMO-T-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Exposer le port de l'application
EXPOSE 5002

# Lancer l'application en indiquant le module-path vers les librairies JavaFX
ENTRYPOINT ["java", "--module-path", "/opt/javafx/javafx-sdk-23.0.2/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]
