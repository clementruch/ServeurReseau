import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class serveurUDP {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            // connexion
            DatagramSocket server = new DatagramSocket(2345, InetAddress.getLocalHost());
			System.out.println("Serveur démarré sur le port " + server.getLocalPort());
            int portClients = 2345+1;
            while (true) {


                // réception d'une requête du client
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                server.receive(packet);

                // traitement
                String str = new String(packet.getData()).trim();
                System.out.println("Reçu de la part de " + packet.getAddress() + " sur le port " + packet.getPort() + " : " + str);
                Communication communicationLoc = new Communication(portClients);
                new Thread(communicationLoc).start();
                portClients++;
                //server.send(packet);
                // envoi de la réponse 
                //System.out.print("Entrez le message à envoyer : ");
                String response = " nouvelle connection crée au port PM" + communicationLoc.portDeCom + "PM ok" ;  // Lecture du message au clavier
                byte[] buffer2 = response.getBytes();
                DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, packet.getAddress(), packet.getPort());
                server.send(packet2);


            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close(); // On ferme le scanner
            }
        }
    }
}
