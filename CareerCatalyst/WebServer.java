import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer {
    public static void main(String[] args) {
        try {
            // Create HTTP server on port 5000
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 5000), 0);
            
            // Create context for root path
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String response = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <title>Career Planner Application</title>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: Arial, sans-serif;\n" +
                        "            max-width: 800px;\n" +
                        "            margin: 0 auto;\n" +
                        "            padding: 20px;\n" +
                        "            line-height: 1.6;\n" +
                        "        }\n" +
                        "        h1 {\n" +
                        "            color: #0066CC;\n" +
                        "            border-bottom: 2px solid #eee;\n" +
                        "            padding-bottom: 10px;\n" +
                        "        }\n" +
                        "        .container {\n" +
                        "            background: #f8f9fa;\n" +
                        "            border-radius: 5px;\n" +
                        "            padding: 20px;\n" +
                        "            margin-top: 20px;\n" +
                        "            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                        "        }\n" +
                        "        ul, ol {\n" +
                        "            margin-left: 20px;\n" +
                        "        }\n" +
                        "        li {\n" +
                        "            margin-bottom: 8px;\n" +
                        "        }\n" +
                        "        .status {\n" +
                        "            background-color: #27AE60;\n" +
                        "            color: white;\n" +
                        "            padding: 10px;\n" +
                        "            border-radius: 4px;\n" +
                        "            text-align: center;\n" +
                        "            margin-top: 20px;\n" +
                        "        }\n" +
                        "        code {\n" +
                        "            background: #e0e0e0;\n" +
                        "            padding: 2px 4px;\n" +
                        "            border-radius: 3px;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <h1>Career Planner Application</h1>\n" +
                        "    <div class='container'>\n" +
                        "        <p>This is a Java desktop application running in headless mode in Replit.</p>\n" +
                        "        <p>The Career Planner application would normally run as a JavaFX desktop application with:</p>\n" +
                        "        <ul>\n" +
                        "            <li><strong>Job application tracking</strong> - Record and monitor job applications with deadlines</li>\n" +
                        "            <li><strong>Resume builder</strong> - Create and export professional resumes as PDF</li>\n" +
                        "            <li><strong>Skills management</strong> - Track and update professional skills</li>\n" +
                        "            <li><strong>Goal setting</strong> - Set and track short-term and long-term career goals</li>\n" +
                        "            <li><strong>Career resource library</strong> - Access career development resources</li>\n" +
                        "        </ul>\n" +
                        "        <p>To run this desktop application locally:</p>\n" +
                        "        <ol>\n" +
                        "            <li>Download the project code</li>\n" +
                        "            <li>Make sure you have Java 17+ and JavaFX installed</li>\n" +
                        "            <li>Run with Maven: <code>mvn javafx:run</code></li>\n" +
                        "        </ol>\n" +
                        "        <div class='status'>\n" +
                        "            <p>Server is running successfully! Current time: " + new java.util.Date() + "</p>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>";
                    
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, response.length());
                    
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });
            
            // Start the server
            server.start();
            System.out.println("Web server started on port 5000");
            System.out.println("Access the server at: http://localhost:5000");
            
        } catch (IOException e) {
            System.err.println("Error starting web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}