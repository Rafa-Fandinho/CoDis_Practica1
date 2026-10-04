package org.dropproject.samples.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadEmisora implements Runnable {
    private final AtomicBoolean conectado;
    private final String username;
    private final String disconnect;
    private final Object Cerrojo1;
    private final MulticastSocket socket;
    private final int port;
    private final InetAddress multicast;

    public ThreadEmisora(AtomicBoolean conectado, String disconnect, String username, Object Cerrojo1, MulticastSocket socket, int port, InetAddress multicast) {
        this.conectado = conectado;
        this.username = username;
        this.disconnect = disconnect;
        this.Cerrojo1 = Cerrojo1;
        this.socket = socket;
        this.port = port;
        this.multicast = multicast;
    }


    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(conectado.get()){
            String contido;
            try{
                contido = scanner.nextLine();
            } catch (Exception e) {
                break;
            }
            //Ignoramos as mensaxes baleiras
            if (contido.trim().isEmpty()) { continue; }
            //Identificamos o comando de desconexión e o xestionamos
            if (contido.equalsIgnoreCase(disconnect)){
                conectado.set(false);
                try{
                    socket.close();
                } catch (Exception e) {
                    //Ignoramos esta excepción
                }
                break;
            }
            //Xeramos a mensaxe completa, ca cabeceira indicada
            String mensaxe = username + ":" + contido;
            byte [] m = mensaxe.getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, multicast, port);
            //Finalmente, enviámolo
            try {
                socket.send(messageOut);
            } catch (IOException e) {
                synchronized (Cerrojo1) {
                    System.out.println("Erro enviando a mensaxe: " + e.getMessage());
                }
                break;
            }
        }
    }
}