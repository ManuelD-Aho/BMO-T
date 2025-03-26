package bahou.akandan.kassy.bmot.communication;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object contenu;
    private String expediteur;
    private LocalDateTime horodatage;

    public Message(MessageType type, Object contenu, String expediteur) {
        this.type = type;
        this.contenu = contenu;
        this.expediteur = expediteur;
        this.horodatage = LocalDateTime.now();
    }

    public MessageType getType() {
        return type;
    }

    public Object getContenu() {
        return contenu;
    }

    public String getExpediteur() {
        return expediteur;
    }

    public LocalDateTime getHorodatage() {
        return horodatage;
    }

    @Override
    public String toString() {
        return "Message [type=" + type + ", expediteur=" + expediteur +
                ", horodatage=" + horodatage + "]";
    }
}