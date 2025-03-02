import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Clase Cliente
 * Simula una aplicación cliente que se conecta a un servidor para enviar un archivo
 * y solicitar su impresión. El cliente envía el nombre del archivo, su contenido y espera una respuesta
 * del servidor sobre el estado de la impresión.
 */
public class Cliente {

    // Dirección del servidor al que el cliente se conecta
    private final String servidor;
    
    // Puerto en el que el servidor escucha las solicitudes de los clientes
    private final int puerto;

    /**
     * Constructor de la clase Cliente. Inicializa la dirección del servidor y el puerto.
     * @param servidor Dirección del servidor al que se va a conectar el cliente.
     * @param puerto Puerto del servidor al que el cliente se va a conectar.
     */
    public Cliente(String servidor, int puerto) {
        this.servidor = servidor;
        this.puerto = puerto;
    }

    /**
     * Método que gestiona la conexión con el servidor, el envío del archivo y la espera de la respuesta.
     * Solicita al usuario el nombre del archivo, lo lee y lo envía al servidor para su impresión.
     */
    public void imprimir() {
        // Pedir al usuario la ruta del archivo a imprimir
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese la ruta del archivo a imprimir : ");
        String nombreArchivo = scanner.nextLine();

        // Verificar que el nombre del archivo no sea vacío o nulo
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            System.out.println("Error: El nombre del archivo no puede estar vacío.");
            return; // Salir si el nombre del archivo es inválido
        }

        // Verificar si el archivo existe en el sistema local
        File archivo = new File(nombreArchivo);
        if (!archivo.exists() || archivo.isDirectory()) {
            System.out.println("Error: El archivo " + nombreArchivo + " no existe o no es un archivo válido.");
            return; // Salir si el archivo no existe
        }

        try (Socket socket = new Socket(servidor, puerto);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Enviar el nombre del archivo al servidor
            out.println(archivo.getName());

            // Leer el contenido del archivo
            BufferedReader fileReader = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = fileReader.readLine()) != null) {
                out.println(linea); // Enviar el contenido del archivo al servidor
            }

            // Enviar señal de fin de transmisión
            out.println("<FIN>");  // Esto indica que el archivo ha terminado de enviarse

            // Cerrar el flujo del archivo
            fileReader.close();

            // Leer la respuesta del servidor
            String respuesta = in.readLine();
            System.out.println(respuesta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método principal que inicia el cliente. 
     * Establece la conexión con el servidor y solicita la impresión del archivo.
     * @param args Argumentos de línea de comandos (no utilizados en este caso).
     */
    public static void main(String[] args) {
        String servidor = "localhost"; // Dirección del servidor
        int puerto = 12345; // Puerto del servidor

        // Crear un cliente e intentar imprimir
        Cliente cliente1 = new Cliente(servidor, puerto);
        cliente1.imprimir();
    }
}
