package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class HasRelationship implements HttpHandler{
  
  Driver driver;
  
  public HasRelationship(Driver driver) {
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
      String movieId = deseralized.getString("movieId");
      getActor(actorId, movieId, exchange);
    } catch (Exception e) {
      exchange.sendResponseHeaders(400, 0);
      e.printStackTrace();
    }
  }
  
  public void getActor(String actorId, String movieId, HttpExchange exchange) throws IOException {
    try (Session session = driver.session()) {
    	String response = session.writeTransaction( new TransactionWork<String>()
        {
            @Override
            public String execute( Transaction tx )
            {
            	JSONObject json = new JSONObject();
            	StatementResult exists = tx.run(String.format("RETURN EXISTS( (a:actor {actorId: '%s'})-[:ACTED_IN]-(:movie {moveId: %s}) )", actorId));
            	Record record;
            	try {
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
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
                return json.toString();
            }
        } );
    	if(response.contains("\"name\":")) {
          exchange.sendResponseHeaders(200, response.length());
          
    	}
    	else {
    	  response = "";
    	  exchange.sendResponseHeaders(400, response.length());
    	}
    	OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    } catch (Exception e) {
    	e.printStackTrace();
    	exchange.sendResponseHeaders(500, 0);
    }
  }
}
