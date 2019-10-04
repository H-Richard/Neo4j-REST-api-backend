package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class AddActor implements HttpHandler{
  
  Driver driver;
  
  public AddActor(Driver driver) {
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
      String name = deseralized.getString("name");
      String actorId = deseralized.getString("actorId");
      addActor(name, actorId, exchange);
      exchange.sendResponseHeaders(200, 0);
    } catch (Exception e) {
      exchange.sendResponseHeaders(400, 0);
      e.printStackTrace();
    }
  }
  
  public void addActor(String name, String actorID, HttpExchange exchange) throws IOException {
    try (Session session = driver.session()) {
      session.run(String.format("CREATE (n:actor {name: \"%s\", actorId: \"%s\"})", name, actorID));
    } catch (Exception e) {
    	exchange.sendResponseHeaders(500, 0);
    }
  }
}
