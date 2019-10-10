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

public class GetMovie implements HttpHandler{
  
  Driver driver;
  
  public GetMovie(Driver driver) {
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
      String movieId = deseralized.getString("movieId");
      getMovie(movieId, exchange);
    } catch (Exception e) {
      exchange.sendResponseHeaders(400, 0);
      e.printStackTrace();
    }
  }
  
  public void getMovie(String movieId, HttpExchange exchange) throws IOException {
    try (Session session = driver.session()) {
    	String response = session.writeTransaction( new TransactionWork<String>()
        {
            @Override
            public String execute( Transaction tx )
            {
            	JSONObject json = new JSONObject();
            	StatementResult name = tx.run(String.format("MATCH (n:movie) WHERE n.movieId = '%s' RETURN n.name", movieId));
            	StatementResult actors = tx.run(String.format("match (m:movie {movieId: '%s'})<-[r:ACTED_IN]-(a:actpr) return a.actorId;", movieId));
            	Record record;
            	try {
	            	json.put("movieId", movieId);
	            	while(name.hasNext()) {
	            		record = name.next();
	            		json.put("name", record.get("n.name", ""));
	            	}
	            	JSONArray array = new JSONArray();
	            	while(actors.hasNext()) {
	            		record = actors.next();
	            		array.put(record.get("a.actorId", ""));
	            	}
	            	json.put("actors", array);
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
                return json.toString();
            }
        } );
    	System.out.println(response);
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
