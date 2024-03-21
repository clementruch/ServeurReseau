import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import static java.lang.Thread.currentThread;

public class Communication implements Runnable {
    DatagramSocket socket;
    int num_id;

    static int numero_communication = 1;

    int portDeCom;
    boolean continuerCommunication = true;

    public Communication(int port) {
        num_id = numero_communication;
        numero_communication++;

        try {
            socket =new DatagramSocket(port, InetAddress.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("creation communication");
        portDeCom = socket.getLocalPort();
        continuerCommunication = true;

    }

    public int getPortDeCom() {
        return portDeCom;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        try {

            while(continuerCommunication){
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // traitement
                String str = new String(packet.getData()).trim();
                System.out.println("Reçu de la part de " + packet.getAddress() + " sur le port " + packet.getPort() + " : " + str);
                System.out.println("reception par la communication de  id numero : " + num_id);
                socket.send(packet);

                if(str.toUpperCase().equals("END") ){
                    socket.close();
                    currentThread().stop();
                    continuerCommunication = false;
                }

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
