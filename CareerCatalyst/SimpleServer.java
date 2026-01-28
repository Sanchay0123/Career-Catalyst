import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleServer {
    public static void main(String[] args) {
        try {
            // Create HTTP server on port 5000
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 5000), 0);
            
            // Create context for root path
            server.createContext("/", exchange -> {
                String response = "<html><body><h1>Career Planner Test Server</h1><p>Server is running successfully!</p></body></html>";
                
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            
            // Start the server
            server.start();
            System.out.println("Simple web server started on port 5000");
            System.out.println("Access the server at: http://localhost:5000");
            
        } catch (IOException e) {
            System.err.println("Error starting web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}