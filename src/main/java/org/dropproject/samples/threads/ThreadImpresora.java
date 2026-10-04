package org.dropproject.samples.threads;

public class ThreadImpresora implements Runnable {
    private final String mensaxe;
    private final String username;
    private final Object Cerrojo1;

    public ThreadImpresora(String mensaxe, String username, Object Cerrojo1) {
        this.mensaxe = mensaxe;
        this.username = username;
        this.Cerrojo1 = Cerrojo1;
    }

    @Override
    public void run() {
        int separador = mensaxe.indexOf(':');
        //Se non hai username ou ':', a mensaxe non é válida
        if (separador <= 0) {
            return;
        }
        String emisor = mensaxe.substring(0,separador);
        String contido = mensaxe.substring(separador+1);
        //Temos tamén que filtrar as mensaxes enviadas por nós mesmos
        if(emisor.equals(username)) {
            return;
        }
        //Noutro caso, imprimimos a mensaxe seguindo o formato indicado
        synchronized (Cerrojo1){
            System.out.println("[" + emisor + "] dice: " + contido);
        }
    }
}
