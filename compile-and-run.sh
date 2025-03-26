#!/bin/bash
# Script de compilation et d'exécution pour BMO Meet

# Vérifier si Maven est installé
if ! command -v mvn &> /dev/null; then
    echo "Maven n'est pas installé. Veuillez installer Maven."
    exit 1
fi

# Vérifier si JavaFX est installé
if [ -z "$PATH_TO_FX" ]; then
    echo "Variable PATH_TO_FX non définie. Veuillez définir le chemin vers JavaFX SDK."
    echo "Example: export PATH_TO_FX=/path/to/javafx-sdk/lib"
    exit 1
fi

# Compilation
echo "Compilation du projet BMO Meet..."
mvn clean package

# Vérifier si la compilation a réussi
if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation. Veuillez corriger les erreurs et réessayer."
    exit 1
fi

# Demander à l'utilisateur s'il veut démarrer le serveur, un client ou les deux
echo "Que souhaitez-vous démarrer ?"
echo "1. Serveur uniquement"
echo "2. Client uniquement"
echo "3. Serveur et client"
read -p "Votre choix (1-3): " choice

case $choice in
    1)
        echo "Démarrage du serveur BMO Meet..."
        java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml,javafx.swing -cp target/bmot-1.0-SNAPSHOT.jar bahou.akandan.kassy.bmot.serveurs.ServerMain
        ;;
    2)
        echo "Démarrage du client BMO Meet..."
        java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml,javafx.swing -jar target/bmot-1.0-SNAPSHOT.jar
        ;;
    3)
        echo "Démarrage du serveur BMO Meet..."
        java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml,javafx.swing -cp target/bmot-1.0-SNAPSHOT.jar bahou.akandan.kassy.bmot.serveurs.ServerMain &
        SERVER_PID=$!

        echo "Démarrage du client BMO Meet..."
        java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml,javafx.swing -jar target/bmot-1.0-SNAPSHOT.jar

        # Arrêter le serveur quand le client est fermé
        kill $SERVER_PID
        ;;
    *)
        echo "Choix invalide."
        exit 1
        ;;
esac

echo "Exécution terminée."