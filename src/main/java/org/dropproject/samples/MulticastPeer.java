/*
A entrega está estructurada como un proxecto de Maven porque é o ambiente
co que estou acostumbrado a traballar. Pódese executar desde a raíz facendo
mvn clean compile package
e posteriormente introducindo o seguinte comando
java -cp target/classes org.dropproject.samples.MulticastPeer
 */

package org.dropproject.samples;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dropproject.samples.threads.*;

public class MulticastPeer{

private static final String MULTICAST_ADDR = "239.1.2.3";
private static final int PORT = 6000;
//Comando para a desconexión
private static final String DISCONNECT = "disconnect";
//O nome de usuario, socket e grupo multicast teñen que ser variables globais
private static String username;
private static InetAddress multicast;
private static MulticastSocket socket;
//Pechadura para a sección crítica
private static final Object Cerrojo1 = new Object();

//Introducín esta variable para a desconexión
private static final AtomicBoolean conectado = new AtomicBoolean(true);

public static void main(String[] args) {
    try {
        username = rexistrarUsuario();
        multicast = InetAddress.getByName(MULTICAST_ADDR);
        socket = new MulticastSocket(PORT);
        socket.joinGroup(multicast);
        System.out.println("======================================");
        System.out.println(" Chat Grupal Descentralizado");
        System.out.println("======================================");
        System.out.println("Acabas de entrar ao canal multicast, co usuario: " + username);
        System.out.println("Para escribir mensaxes, introdúceos na consola e pulsa ENTER.");
        System.out.println("Escribe '" + DISCONNECT + "' para sair do canal.");
        System.out.println("======================================");

        ThreadReceptora receptora = new ThreadReceptora(conectado,username,Cerrojo1,socket);
        Thread threadReceptora = new Thread(receptora, "ThreadReceptora");
        threadReceptora.start();
        //Pasamos o fío principal á emisión de mensaxes
        ThreadEmisora emisora = new ThreadEmisora(conectado, DISCONNECT, username, Cerrojo1, socket, PORT, multicast);
        emisora.run();

        threadReceptora.join();
    } catch (SocketException e) { System.out.println("Socket: " + e.getMessage()); }
    catch (IOException e) { System.out.println("IO: " + e.getMessage()); }
    catch (InterruptedException e) { Thread.currentThread().interrupt();
        synchronized (Cerrojo1) {System.out.println("Interrumpiuse a thread principal");} }
    finally {
        desconexion();
    }
}

/*Aquí dinme conta de que hai un potencial problema con usernames duplicados.
Como non se pide xestionalo e a complexidade é relativamente elevada (pódense manter listas locais
pero non garantizan que isto funcione ben nun entorno asíncrono, decidín ignoralo).
*/
private static String rexistrarUsuario() {

    Scanner scanner = new Scanner(System.in);
    String nome;
    while (true) {
        System.out.print("Introduce o teu nome de usuario: ");
        nome = scanner.nextLine().trim();
        if(nome.isEmpty()){
            System.out.println("O nome non pode estar baleiro");
        } else if (nome.contains(":")) {
            System.out.println("O nome non pode conter o caracter ':'");
        } else {
            System.out.print( "¿Confirmar o nome de usuario '" + nome + "'? (s/n): " );
            String resposta = scanner.nextLine().trim();
            if (resposta.equalsIgnoreCase("s")){
                return nome;
            }
            System.out.println("Nome de usuario descartado. Inténtao de novo.");
        }
    }
}

private static void desconexion() {
    conectado.set(false);
    if(socket != null && !socket.isClosed()){
        try{
            if (multicast != null){
                socket.leaveGroup(multicast);
            }
        } catch (IOException e) {
            synchronized (Cerrojo1) {System.out.println("IO: " + e.getMessage());}
        } finally {
            socket.close();
        }
    }
    synchronized (Cerrojo1) {
        System.out.println("Desconectado do chat. Ata logo!");
    }
}

}


/*
Vale, o código debería ir tal que así:
Primeiro, un método bloqueante que obriga ao usuario a rexistrarse cun nome (pedir confirmación)
Despois, un método de rexistro na rede, Canal Multicast 239.1.2.3 Porto: 6000
A partir diso, crear unha thread que esté escoitando constantemente a consola e envíe as mensaxes
E outra thread que esté escoitando o socket, e cando recibe algunha mensaxe crea outra thread que desempaquete a mensaxe e volve a escoitar o socket
A thread que desempaqueta é a responsable de escribir visualmente a mensaxe. Como a consola é unha zona crítica, debe usar un candado
Finalmente, usando unha palabra clave (p.e. disconnect) ou algo do tipo, desconectarse da rede multicast e rematar o programa.
 */

/*
Para cada tipo de thread, usar interfaces distintas segundo esta guía dos slides:
public interface Runnable{
public void run(){}
}
public class MiHilo extends OtraClase implements Runnable{//Variables locales
public MiHilo(String name){
super(name);
}
public void run(){
// Código
}
public class PruebaMiHilo {
public static void mainString[] args){
// Creación del objeto
MiHilo NuevoHilo = new MiHilo(name);
Thread NuevoThread = new Thread(MiHilo);//Se lanza el hilo
NuevoThread.start();
}
}

public class PruebaMiHilo {
public static void mainString[] args){
// Creación del objeto y ejecución
new Thread(new MiHilo()).start();
}
}
 */