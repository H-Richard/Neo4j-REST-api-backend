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

public class AddMovie implements HttpHandler {

	Driver driver;

	public AddMovie(Driver driver) {
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
			String name = deseralized.getString("name");
			String movieId = deseralized.getString("movieId");
			String query = "CREATE (n:movie {name: \"%s\", movieId: \"%s\"})";
			Utils.queryCreate(this.driver, query, name, movieId, exchange);
		} catch (Exception e) {
			exchange.sendResponseHeaders(400, 0);
			e.printStackTrace();
		}
	}
}
