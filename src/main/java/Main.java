import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final int PORT = 8989;

    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));

        ObjectMapper mapper = new ObjectMapper();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");

            while (true) {

                try (Socket socket = serverSocket.accept();
                     BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter output = new PrintWriter(socket.getOutputStream(), true)
                ) {

                    var inputMsg = input.readLine();
                    var word = mapper.readValue(inputMsg, new TypeReference<String>(){});
                    System.out.println("Request: " + word);

                    var searchResult = engine.search(word);
                    var mappedSearchResult = mapper.writeValueAsString(searchResult);
                    output.println(mappedSearchResult);
                }
            }

        } catch (IOException ex) {
            System.out.println("Cannot start server");
            ex.printStackTrace();
        }
    }
}