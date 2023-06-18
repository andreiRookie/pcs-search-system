import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class TestClient {

    private static final int PORT = 8989;
    private static final String HOST = "127.0.0.1";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)
        ) {

            var testWord = "проектами стартапы задач";
            var outputMsg = MAPPER.writeValueAsString(testWord);
            System.out.println(outputMsg);

            output.println(outputMsg);

            String response = input.readLine();
            System.out.println("Response:\n" + response);

            List<PageEntry> entryList = MAPPER.readValue(response, new TypeReference<>(){});
            entryList.forEach(System.out::println);


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
