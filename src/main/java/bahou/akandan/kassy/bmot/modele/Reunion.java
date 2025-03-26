package bahou.akandan.kassy.bmot.modele;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reunion implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum EtatReunion {
        PLANIFIEE, EN_COURS, TERMINEE
    }

    public enum TypeReunion {
        STANDARD, PRIVEE, DEMOCRATIQUE
    }

    private int id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private EtatReunion etat;
    private TypeReunion type;
    private Utilisateur organisateur;
    private List<Utilisateur> participants;
    private Map<Utilisateur, Boolean> demandesPriseParole;
    private Utilisateur orateur;

    public Reunion(int id, String titre, String description, LocalDateTime dateDebut,
                   LocalDateTime dateFin, TypeReunion type, Utilisateur organisateur) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.etat = EtatReunion.PLANIFIEE;
        this.type = type;
        this.organisateur = organisateur;
        this.participants = new ArrayList<>();
        this.participants.add(organisateur);
        this.demandesPriseParole = new HashMap<>();
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public EtatReunion getEtat() {
        return etat;
    }

    public void setEtat(EtatReunion etat) {
        this.etat = etat;
    }

    public TypeReunion getType() {
        return type;
    }

    public Utilisateur getOrganisateur() {
        return organisateur;
    }

    public List<Utilisateur> getParticipants() {
        return participants;
    }

    public void ajouterParticipant(Utilisateur participant) {
        if (!participants.contains(participant)) {
            participants.add(participant);
        }
    }

    public boolean retirerParticipant(Utilisateur participant) {
        if (participant.equals(organisateur)) {
            return false; // L'organisateur ne peut pas être retiré
        }
        return participants.remove(participant);
    }

    public boolean demanderPriseParole(Utilisateur utilisateur) {
        if (participants.contains(utilisateur)) {
            demandesPriseParole.put(utilisateur, true);
            return true;
        }
        return false;
    }

    public void accorderPriseParole(Utilisateur utilisateur) {
        if (participants.contains(utilisateur)) {
            orateur = utilisateur;
            demandesPriseParole.remove(utilisateur);
        }
    }

    public Utilisateur getOrateur() {
        return orateur;
    }

    public Map<Utilisateur, Boolean> getDemandesPriseParole() {
        return demandesPriseParole;
    }

    public void demarrer() {
        this.etat = EtatReunion.EN_COURS;
    }

    public void terminer() {
        this.etat = EtatReunion.TERMINEE;
    }

    @Override
    public String toString() {
        return "Reunion #" + id + ": " + titre + " (" + etat + ")";
    }
}