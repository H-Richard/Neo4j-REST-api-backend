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
            	StatementResult actor = tx.run(String.format("MATCH (n:actor) WHERE n.actorId = '%s' RETURN n.name", actorId));
            	StatementResult movie = tx.run(String.format("MATCH (n:movie) WHERE n.movieId = '%s' RETURN n.name", movieId));
            	StatementResult exists = tx.run(String.format("RETURN EXISTS( (:actor {actorId: '%s'})-[:ACTED_IN]-(:movie {moveId: %s}) ) AS a", actorId, movieId));
            	if(actor.hasNext() && movie.hasNext()) {
            		try {
    	            	json.put("actorId", actorId);
    	            	json.put("movieId", movieId);
    	            	json.put("hasRelationship", new Boolean(exists.next().get("a", "")));
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
            		return json.toString();
            	}
            	else {
            		return "";
            	}
            }
        } );
    	if(!response.isEmpty()) {
          exchange.sendResponseHeaders(200, response.length()); 
    	}
    	else {
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
