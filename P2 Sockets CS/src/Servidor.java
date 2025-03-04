
import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

/**
 * Clase Servidor Simula una impresora que acepta una solicitud de impresión de
 * un cliente a la vez. Utiliza un semáforo para controlar el acceso concurrente
 * a la impresora y simula el proceso de impresión mientras gestiona la tinta
 * disponible de acuerdo con el contenido de los archivos enviados.
 */
public class Servidor {
// Puerto en el que el servidor escucha las conexiones entrantes de los clientes

    private static final int PUERTO = 12345;

    // Semáforo que controla el acceso exclusivo a la impresora
    private static final Semaphore impresoraDisponible = new Semaphore(1);

    // Porcentaje de tinta disponible en la impresora (inicia al 100%)
    private static double porcentajeTinta = 100.0;

    /**
     * Método principal que establece la conexión con el cliente y maneja la
     * solicitud de impresión.
     */
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            // El servidor acepta y procesa un cliente a la vez
            while (true) {
                // El servidor espera una nueva conexión de un cliente
                Socket socketCliente = serverSocket.accept();
                System.out.println("Cliente conectado: " + socketCliente.getInetAddress());

                // Procesar la solicitud del cliente de manera secuencial
                procesarSolicitud(socketCliente);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Este método procesa la solicitud de un cliente. Lee el nombre del archivo
     * y su contenido, simula la impresión y gestiona el uso de la tinta.
     */
    private static void procesarSolicitud(Socket socketCliente) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                PrintWriter out = new PrintWriter(socketCliente.getOutputStream(), true)) {

            // Leer el nombre del archivo
            String nombreArchivo = in.readLine();
            System.out.println("Archivo recibido para impresión: " + nombreArchivo);

            // Leer el contenido del archivo enviado por el cliente
            StringBuilder contenido = new StringBuilder();
            String linea;
            while ((linea = in.readLine()) != null) {
                if (linea.equals("<FIN>")) {
                    break; // Si se recibe "<FIN>", terminamos de leer el archivo
                }
                contenido.append(linea).append("\n");
            }

            // Obtener el número de caracteres del archivo
            int numCaracteres = contenido.length();
            // Calcular la tinta gastada según el número de caracteres
            double tintaGastada = calcularTintaGastada(numCaracteres);

            // Verificar si la impresora tiene suficiente tinta
            if (porcentajeTinta >= tintaGastada) {
                impresoraDisponible.acquire(); // Solicita acceso a la impresora

                try {
                    System.out.println("Imprimiendo archivo " + nombreArchivo + "...");
                    // Reducir el porcentaje de tinta usado
                    porcentajeTinta -= tintaGastada;
                    System.out.println("Impresión completa de " + nombreArchivo + ". Tinta disponible: " + porcentajeTinta + "%");

                    // Enviar mensaje al cliente indicando que la impresión fue exitosa
                    out.println("Impresión de " + nombreArchivo + " completada. Tinta restante: " + porcentajeTinta + "%");

                } finally {
                    impresoraDisponible.release(); // Libera la impresora para el siguiente cliente
                }
            } else {
                // Si no hay suficiente tinta, informamos al cliente
                out.println("No hay suficiente tinta para imprimir " + nombreArchivo + ". Tinta restante: " + porcentajeTinta + "%");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                socketCliente.close(); // Cerrar la conexión con el cliente
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Método que calcula el porcentaje de tinta gastada según el número de
     * caracteres del archivo.
     *
     * @param numCaracteres El número de caracteres en el contenido del archivo.
     * @return El porcentaje de tinta gastada en la impresión.
     */
    private static double calcularTintaGastada(int numCaracteres) {
        double tintaGastada = 0.0;
        // Si el archivo tiene menos de 50 caracteres, gasta 0.5% de tinta
        if (numCaracteres < 50) {
            tintaGastada = 0.5;
        } // Si el archivo tiene entre 50 y 100 caracteres, gasta 0.7% de tinta
        else if (numCaracteres >= 50 && numCaracteres < 100) {
            tintaGastada = 0.7;
        } // Si el archivo tiene más de 100 caracteres, gasta 1% de tinta
        else if (numCaracteres >= 100) {
            tintaGastada = 1.0;
        }
        return tintaGastada;
    }
}
