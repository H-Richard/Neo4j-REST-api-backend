package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class GetActor implements HttpHandler{
  
  Driver driver;
  
  public GetActor(Driver driver) {
    this.driver = driver;
  }
  
  
  public void handle(HttpExchange exchange) {
    try {
      if (exchange.getRequestMethod().equals("GET")) {
          handleGet(exchange);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void handleGet(HttpExchange exchange) throws IOException, JSONException {
    String body = Utils.convert(exchange.getRequestBody());
    JSONObject deseralized = new JSONObject(body);
    try {
      String actorId = deseralized.getString("actorId");
      getActor(actorId, exchange);
      exchange.sendResponseHeaders(200, 0);
    } catch (Exception e) {
      exchange.sendResponseHeaders(400, 0);
      e.printStackTrace();
    }
  }
  
  public void getActor(String actorId, HttpExchange exchange) throws IOException {
    try (Session session = driver.session()) {
    	JSONObject json = new JSONObject();
    	StatementResult name = session.run(String.format("MATCH (n:actor) WHERE n.actorId = '%s' RETURN n.name", actorId));
    	StatementResult movies = session.run(String.format("match (a:actor {actorId: '%s'})-[r:ACTED_IN]->(m:movie) return m.movieId;", actorId));
    	Record record;
    	json.put("actorId", actorId);
    	while(name.hasNext()) {
    		record = name.next();
    		json.put("name", record.get("n.name", ""));
    	}
    	JSONArray array = new JSONArray();
    	while(movies.hasNext()) {
    		record = movies.next();
    		array.put(record.get("m.movieId", ""));
    	}
    	json.put("movies", array);
    	OutputStream os = exchange.getResponseBody();
    	os.write(json.toString().getBytes());
    	os.close();
    } catch (Exception e) {
    	e.printStackTrace();
    	exchange.sendResponseHeaders(500, 0);
    }
  }
}
