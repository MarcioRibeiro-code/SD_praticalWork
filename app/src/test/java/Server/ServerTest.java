package Server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import Entity.MilitarType;
import Entity.User;
import Requests.UserRegister;
import utils.Requests.Request;
import utils.Requests.RequestType;

public class ServerTest {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 2048;
    protected static final int DATAGRAM_PORT = 4445;

    private Server server;

    @BeforeEach
    public void setUp() throws Exception {
        server = new Server(new ServerSocket(SERVER_PORT));
        new Thread(() -> server.startServer()).start();
        Thread.sleep(1000);
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.closeServerSocket();
    }

    @Test
    public void Register() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            UserRegister register = new UserRegister(new User("João", MilitarType.SOLDIER.name(), "123", "joao"));

            if (userAlreadyExists()) {
                System.out.println("The user already exists!, and was used via register test");
                return;
            }
            // Convert the register object to JSON
            String registerRequest = new Gson().toJson(new Request<>(RequestType.REGISTER, register));
            writer.write(registerRequest);
            writer.newLine();
            writer.flush();

            // Receive and print server response
            String registrationResponse = reader.readLine();
            System.out.println("Server response: " + registrationResponse);

            // Assert that the response contains the expected JSON elements
            assertTrue(registrationResponse.contains("\"status\":\"SUCCESS\""));
            assertTrue(registrationResponse.contains("\"type\":\"REGISTER\""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean userAlreadyExists() {
        try {
            // Read file content and convert it to an array of User objects
            List<User> users = new JsonFileHelper("files/").deserializeArray("users", User[].class);

            // Check if there's an equal object in the array
            UserRegister register = new UserRegister(new User("João", MilitarType.SOLDIER.name(), "123", "joao"));
            boolean userExists = users.stream()
                    .anyMatch(user -> user.equals(register.getUser()));
            return userExists;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
