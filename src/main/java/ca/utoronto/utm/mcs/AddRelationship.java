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
			String query = "MATCH (a:actor),(b:movie)" + "WHERE a.actorId = '%s' AND b.movieId = '%s'"
					+ "CREATE (a)-[rel:ACTED_IN]->(b)";
			Utils.queryCreate(this.driver, query, actorId, movieId, exchange);
		} catch (Exception e) {
			exchange.sendResponseHeaders(400, 0);
			e.printStackTrace();
		}
	}
}
