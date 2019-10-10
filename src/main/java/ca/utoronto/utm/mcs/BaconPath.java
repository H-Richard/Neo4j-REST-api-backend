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

public class BaconPath implements HttpHandler{
  
  Driver driver;
  
  public BaconPath(Driver driver) {
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
      getPath(actorId, exchange);
    } catch (Exception e) {
      exchange.sendResponseHeaders(400, 0);
      e.printStackTrace();
    }
  }
  
  public void getPath(String actorId, HttpExchange exchange) throws IOException {
    try (Session session = driver.session()) {
    	String response = session.writeTransaction( new TransactionWork<String>()
        {
            @Override
            public String execute( Transaction tx )
            {
            	JSONObject json = new JSONObject();
            	StatementResult result = tx.run(String.format("MATCH p=shortestPath((n:actor {name: 'Kevin Bacon'})-[rel:ACTED_IN*]-(b:actor {actorId: '%s'})) RETURN rel;", actorId));
            	JSONArray array = new JSONArray();
            	
            	if(!result.hasNext()) {
            		return "";
            	}
            	else {
            		Record record = result.next();
            		int size = record.get(0).size();
            	}
            	//record.get(0).get(0).get("actorId", "")
            	try {
					json.put("baconPath", array);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            	return json.toString();
            }
        } );
    	if(!response.isEmpty()) {
          exchange.sendResponseHeaders(200, response.length()); 
    	}
    	else {
    	  exchange.sendResponseHeaders(404, response.length());
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
