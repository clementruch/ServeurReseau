import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap; // Concurrent pour la sécurité des threads


public class serveurUDP {
    private static ConcurrentHashMap<String, ClientInfo> clientsConnectes = new ConcurrentHashMap<>();
    private static int portClients = 2346;

    public static void main(String[] args) {
        try {
            // connexion
            DatagramSocket server = new DatagramSocket(2345, InetAddress.getLocalHost());
			System.out.println("Serveur démarré sur le port " + server.getLocalPort());

            while (true) {
                // réception d'une requête du client
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                server.receive(packet);

                // traitement
                String str = new String(packet.getData()).trim();
                System.out.println("Reçu de la part de " + packet.getAddress() + " sur le port " + packet.getPort() + " : " + str);
                
                if (str.equals("LISTE")) {
                    gererListe(packet);
                } else if (str.startsWith("CONNECT ")) {
                    String clientId = str.split(" ")[1];
                    gererConnexion(clientId, packet);
                } else if (str.startsWith("IDENTIFY ")){
                    gererNouvelleConnexion(str, packet);
                } else if (str.startsWith("DISCONNECT ")) {
                    String clientId = str.split(" ")[1];
                    gererDeconnexion(clientId, packet);
                } else {
                    envoyerReponse(packet.getAddress(), packet.getPort(), "Commande non reconnue.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void gererListe(DatagramPacket packet) throws IOException {
        String listeClients = String.join(", ", clientsConnectes.keySet());
        envoyerReponse(packet.getAddress(), packet.getPort(), "Clients connectés: " + listeClients);
    }
    
    private static void gererConnexion(String clientId, DatagramPacket packet) throws IOException {
        String[] commande = new String(packet.getData()).trim().split(" ");
        
        if (commande.length < 2) {
            envoyerReponse(packet.getAddress(), packet.getPort(), "Commande CONNECT incorrecte.");
            return;
        }
        
        String clientIdDemandeur = clientId; // Client qui demande la connexion
        String clientIdCible = commande[1]; // Client cible avec lequel se connecter
        ClientInfo clientDemandeur = clientsConnectes.get(clientIdDemandeur);
        ClientInfo clientCible = clientsConnectes.get(clientIdCible);
    
        if (clientCible != null && clientDemandeur != null) {
            // Envoyer au demandeur le port du client cible
            String messagePourDemandeur = "CONNECT " + clientIdCible + " " + clientCible.getPort();
            envoyerReponse(packet.getAddress(), clientDemandeur.getPort(), messagePourDemandeur);
    
            // Envoyer au client cible le port du demandeur
            String messagePourCible = "CONNECT " + clientIdDemandeur + " " + clientDemandeur.getPort();
            envoyerReponse(packet.getAddress(), clientCible.getPort(), messagePourCible);
        } else {
            envoyerReponse(packet.getAddress(), packet.getPort(), "Un des clients n'a pas été trouvé.");
        }
    }
    
    

    private static void gererDeconnexion(String clientId, DatagramPacket packet) throws IOException {
        if (clientsConnectes.containsKey(clientId)) {
            ClientInfo clientInfo = clientsConnectes.get(clientId);
            clientInfo.setEstConnecte(false); // Mettre à jour l'état de connexion
            clientsConnectes.remove(clientId);
            envoyerReponse(packet.getAddress(), packet.getPort(), "Vous êtes déconnecté.");
        }
    }

    private static void gererNouvelleConnexion(String str, DatagramPacket packet) throws IOException {
        // Extrait l'identifiant du client à partir de la commande reçue
        String clientId = str.substring("IDENTIFY ".length()); // Extrait l'identifiant du client de la commande
        
        // Vérifie si l'identifiant est déjà utilisé
        if (!clientsConnectes.containsKey(clientId)) {
            int clientPort = portClients++;
            ClientInfo newClient = new ClientInfo(clientPort, clientId);
            clientsConnectes.put(clientId, newClient);

            // Démarrer un thread de Communication pour ce client
            /*Communication communication = new Communication(clientPort);
            new Thread(communication).start();*/
            
            // Informer le client de son identifiant et de son port de communication
            String response = "Votre Identifiant est : \"" + clientId + "\" et votre port de communication est : " + clientPort;
            envoyerReponse(packet.getAddress(), packet.getPort(), response);
        } else {
            envoyerReponse(packet.getAddress(), packet.getPort(), "Identifiant déjà utilisé.");
        }
    }

    private static void envoyerReponse(InetAddress address, int port, String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, address, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(responsePacket);
        socket.close();
    }
}
