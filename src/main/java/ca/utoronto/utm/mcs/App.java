package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {   
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", 
                                            AuthTokens.basic("neo4j", "password"));
        
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/api/v1/addActor", new AddActor(driver));
        server.createContext("/api/v1/addMovie", new AddMovie(driver));
        server.createContext("/api/v1/addRelationship", new AddRelationship(driver));
        
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
