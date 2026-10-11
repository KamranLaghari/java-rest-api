
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new java.net.InetSocketAddress(8080), 0
        );

        server.createContext("/students", student_api::handleStudents);

        server.start();

        System.out.println("API running at http://localhost:8080");
        System.out.println("Students endpoint: http://localhost:8080/students");
    }
}
