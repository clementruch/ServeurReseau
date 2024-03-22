import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client2 {
    private static volatile boolean initialisationTerminee = false;
    private static volatile boolean connected = false;
    private static volatile int portCible = -1;
    private static InetAddress adresse;
    private static DatagramSocket socketClient;
    private static Scanner scanner;

    public static void main(String[] args) {
        try {
            socketClient = new DatagramSocket();
            scanner = new Scanner(System.in);
            adresse = InetAddress.getLocalHost();
            String clientId = IdentifiantUnique.generateRandomAlphanumeric(3); // Génération d'un identifiant unique
            
            // Envoi de l'identifiant au serveur pour initialiser la connexion
            int portServeur = 2345; // Le port sur lequel le serveur écoute
            String messageIdentification = "IDENTIFY " + clientId;
            envoyerMessage(socketClient, adresse, portServeur, messageIdentification);

            while (!initialisationTerminee) {
                String reponse = recevoirMessage(socketClient);
                System.out.println(reponse);
                if (reponse.contains("Votre Identifiant est")) {
                    initialisationTerminee = true;
                }
            }

            // Lancer un thread pour écouter les messages entrants
            Thread ecouteThread = new Thread(Client2::ecouter);
            ecouteThread.start();

            // Boucle principale pour l'envoi des messages et des commandes
            while (!connected || (connected && portCible != -1)) {
                System.out.print("Entrez votre message ou une commande (LISTE, CONNECT <id>, DISCONNECT): ");
                String input = scanner.nextLine();

                // Gestion de la déconnexion
                if ("disconnect".equalsIgnoreCase(input)) {
                    connected = false; // Marquer comme déconnecté
                    envoyerMessage(socketClient, adresse, portServeur, "DISCONNECT " + clientId);
                    System.out.println("Déconnexion demandée.");
                    break; // Sortir de la boucle principale
                }

                // Gestion de l'envoi des messages ou des commandes
                if (connected) {
                    // Si connecté à un autre client, envoyer le message directement
                    envoyerMessage(socketClient, adresse, portCible, input);
                } else {
                    // Sinon, envoyer la commande au serveur
                    envoyerMessage(socketClient, adresse, portServeur, input);
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur client UDP: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socketClient != null && !socketClient.isClosed()) {
                socketClient.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static void ecouter() {
        try {
            while (true) {
                String reponse = recevoirMessage(socketClient);
                System.out.println("Réponse du serveur/client : " + reponse);

                if (reponse.startsWith("CONNECT")) {
                    // Récupérer le port du client cible pour la communication directe
                    portCible = Integer.parseInt(reponse.split(" ")[2]);
                    connected = true;
                    System.out.println("Connecté au client sur le port : " + portCible);
                }
                // Ajouter plus de conditions si nécessaire pour gérer différentes réponses
            }
        } catch (IOException e) {
            if (!connected) {
                System.out.println("Arrêt du thread d'écoute : " + e.getMessage());
                return; // Arrêter le thread si le client n'est plus connecté
            }
        }
    }

    private static void envoyerMessage(DatagramSocket socket, InetAddress adresse, int port, String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, port);
        socket.send(packet);
    }

    private static String recevoirMessage(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[8192];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength()).trim();
    }
}
