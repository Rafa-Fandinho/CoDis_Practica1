package org.dropproject.samples.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadReceptora implements Runnable {
    private final AtomicBoolean conectado;
    private final String username;
    private final Object Cerrojo1;
    private final MulticastSocket socket;

    public ThreadReceptora(AtomicBoolean conectado, String username, Object Cerrojo1, MulticastSocket socket) {
        this.conectado = conectado;
        this.username = username;
        this.Cerrojo1 = Cerrojo1;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (conectado.get()) {
            byte[] buffer = new byte[1000];
            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(messageIn);
                String mensaxe = new String(messageIn.getData());
                ThreadImpresora impresora = new ThreadImpresora(mensaxe,username,Cerrojo1);
                Thread threadImpresora = new Thread(impresora, "ThreadImpresora");
                threadImpresora.start();
            } catch (SocketException e) {
                synchronized (Cerrojo1) {
                    System.out.println("Socket: " + e.getMessage());
                }
                break;
            } catch (IOException e) {
                synchronized (Cerrojo1) {
                    System.out.println("Erro enviando a mensaxe: " + e.getMessage());
                }
            }

        }
    }
}
