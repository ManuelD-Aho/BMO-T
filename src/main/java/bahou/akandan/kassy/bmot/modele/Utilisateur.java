package bahou.akandan.kassy.bmot.modele;

import java.io.Serializable;

public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;

    private String login;
    private String motDePasse;
    private String nom;
    private String prenom;
    private boolean estConnecte;
    private boolean estAdministrateur;

    public Utilisateur(String login, String motDePasse, String nom, String prenom) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.estConnecte = false;
        this.estAdministrateur = false;
    }

    // Getters et setters
    public String getLogin() {
        return login;
    }

    public boolean verifierMotDePasse(String motDePasse) {
        return this.motDePasse.equals(motDePasse);
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public boolean estConnecte() {
        return estConnecte;
    }

    public void setEstConnecte(boolean estConnecte) {
        this.estConnecte = estConnecte;
    }

    public boolean estAdministrateur() {
        return estAdministrateur;
    }

    public void setEstAdministrateur(boolean estAdministrateur) {
        this.estAdministrateur = estAdministrateur;
    }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + login + ")";
    }
}