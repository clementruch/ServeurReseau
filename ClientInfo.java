public class ClientInfo {
    private int port;
    private String clientId;
    private boolean estConnecte;

    public ClientInfo(int port, String clientId) {
        this.port = port;
        this.clientId = clientId;
        this.estConnecte = true; // Un nouveau client est considéré comme connecté
    }

    public int getPort() {
        return port;
    }

    public String getClientId() {
        return clientId;
    }
    
    public boolean estConnecte() {
        return estConnecte;
    }

    public void setEstConnecte(boolean estConnecte) {
        this.estConnecte = estConnecte;
    }
}
