package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddRelationship implements HttpHandler {

	Driver driver;

	public AddRelationship(Driver driver) {
		this.driver = driver;
	}

	public void handle(HttpExchange exchange) throws IOException {
		try {
			if (exchange.getRequestMethod().equals("PUT")) {
				handlePut(exchange);
			}
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(400, 0);
			Utils.sendEmptyBody(exchange);
		}
	}

	public void handlePut(HttpExchange exchange) throws IOException, JSONException {
		String body = Utils.convert(exchange.getRequestBody());
		JSONObject deseralized = new JSONObject(body);
		try {
			String actorId = deseralized.getString("actorId");
			String movieId = deseralized.getString("movieId");
			String query = "MATCH (a:actor),(b:movie)" + "WHERE a.actorId = '%1$s' AND b.movieId = '%2$s'"
					+ "MERGE (a)-[rel:ACTED_IN { actorId: '%1$s', movieId: '%2$s' } ]->(b)";
			Utils.queryCreate(this.driver, query, actorId, movieId, exchange);
		} catch (Exception e) {
			exchange.sendResponseHeaders(400, 0);
			e.printStackTrace();
		}
	}
}
