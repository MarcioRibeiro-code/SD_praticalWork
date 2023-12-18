package Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{
    protected Socket socket;
    protected BufferedReader bufferedReader;
    protected BufferedWriter bufferedWriter;
    protected ArrayListSync<ClientHandler> clientHandlers;
    protected Server server;

    public ClientHandler(ArrayListSync<ClientHandler> clientHandlers,Socket socket, Server server) throws IOException {
        try{
            this.socket = socket;
            this.server = server;
            this.clientHandlers = clientHandlers;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        try {
            // Lidar com autenticação ou outras inicializações, se necessário
            // Exemplo: realizarLogin();

            // Agora, ficamos à espera de mensagens do cliente
            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                // Processar a mensagem recebida
                processClientMessage(clientMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Lidar com desconexão do cliente
            handleClientDisconnect();
        }

    }



    /**
     * Function to close BufferedWriter and BufferedReader of a Socket
     * @param socket conection established with a specific client
     * @param bufferedReader reader of a specific client
     * @param bufferedWriter writer of a specific client
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) bufferedReader.close();

            if (bufferedWriter != null) bufferedWriter.close();

            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
