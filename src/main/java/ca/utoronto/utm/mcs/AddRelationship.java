package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class AddRelationship implements HttpHandler{
  
  Driver driver;
  
  public AddRelationship(Driver driver) {
    this.driver = driver;
  }
  
  
  public void handle(HttpExchange exchange) {
    try {
      if (exchange.getRequestMethod().equals("PUT")) {
        handlePut(exchange);
    }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void handlePut(HttpExchange exchange) throws IOException, JSONException {
    String body = Utils.convert(exchange.getRequestBody());
    JSONObject deseralized = new JSONObject(body);
    try {
      String actorId = deseralized.getString("actorId");
      String movieId = deseralized.getString("movieId");
      addRelationship(actorId, movieId, exchange);
      exchange.sendResponseHeaders(200, 0);
    } catch (Exception e) {
      exchange.sendResponseHeaders(400, 0);
      e.printStackTrace();
    }
  }
  
  public void addRelationship(String actorId, String movieId, HttpExchange exchange) {
    try (Session session = driver.session()) {
      String query = "match (a:actor), (b:movie)\r\n" + 
          " where a.actorId = '%s' AND b.movieId = '%s'\r\n" + 
          " create (a)-[rel:ACTED_IN]->(b)";
      session.run(String.format(query, actorId, movieId));
    }
  }
}
