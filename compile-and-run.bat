@echo off
REM Script de compilation et d'exécution pour BMO Meet

REM Vérifier si Maven est installé
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Maven n'est pas installe. Veuillez installer Maven.
    exit /b 1
)

REM Vérifier si JavaFX est configuré
if "%PATH_TO_FX%" == "" (
    echo Variable PATH_TO_FX non definie. Veuillez definir le chemin vers JavaFX SDK.
    echo Example: set PATH_TO_FX=C:\path\to\javafx-sdk\lib
    exit /b 1
)

REM Compilation
echo Compilation du projet BMO Meet...
call mvn clean package

REM Vérifier si la compilation a réussi
if %ERRORLEVEL% NEQ 0 (
    echo Erreur lors de la compilation. Veuillez corriger les erreurs et reessayer.
    exit /b 1
)

REM Demander à l'utilisateur s'il veut démarrer le serveur, un client ou les deux
echo Que souhaitez-vous demarrer ?
echo 1. Serveur uniquement
echo 2. Client uniquement
echo 3. Serveur et client
set /p choice=Votre choix (1-3):

if "%choice%"=="1" (
    echo Demarrage du serveur BMO Meet...
    java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml,javafx.swing -cp target\bmot-1.0-SNAPSHOT.jar bahou.akandan.kassy.bmot.serveurs.ServerMain
) else if "%choice%"=="2" (
    echo Demarrage du client BMO Meet...
    java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml,javafx.swing -jar target\bmot-1.0-SNAPSHOT.jar
) else if "%choice%"=="3" (
    echo Demarrage du serveur BMO Meet...
    start "BMO Meet Server" java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml,javafx.swing -cp target\bmot-1.0-SNAPSHOT.jar bahou.akandan.kassy.bmot.serveurs.ServerMain

    echo Demarrage du client BMO Meet...
    java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml,javafx.swing -jar target\bmot-1.0-SNAPSHOT.jar
) else (
    echo Choix invalide.
    exit /b 1
)

echo Execution terminee.