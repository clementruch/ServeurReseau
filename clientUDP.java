import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class clientUDP {
    public static void main(String[] args){
        try (DatagramSocket client = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {
            
            // envoi de la requête
            InetAddress adresse = InetAddress.getLocalHost();

            boolean continueCom = true;
            int portCommunication = 2345;

            while(continueCom){
                System.out.print("Entrez votre message : ");
                String envoi = scanner.nextLine();

                if(envoi.toUpperCase().equals("END") ){
                    continueCom = false;
                    break;
                }

                byte[] buffer = envoi.getBytes();
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, adresse, portCommunication);
                client.send(packet);
                
                // réception de la réponse 
                byte[] buffer2 = new byte[8192];
                DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
                client.receive(packet2);


                
                // affichage
                String reponse = new String(packet2.getData()).trim();
                if( reponse.contains("nouvelle connection crée au port") ){
                    portCommunication = Integer.parseInt(reponse.split("PM")[1]);
                    System.out.println(" port de communication = " + portCommunication);
                }

                System.out.println("Réponse du serveur : " + reponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ;
    }      
}
