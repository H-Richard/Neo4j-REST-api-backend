package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class BaconPath implements HttpHandler {

	Driver driver;

	public BaconPath(Driver driver) {
		this.driver = driver;
	}

	public void handle(HttpExchange exchange) throws IOException {
		try {
			if (exchange.getRequestMethod().equals("GET")) {
				handleGet(exchange);
			}
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(400, 0);
			Utils.sendEmptyBody(exchange);
		}
	}

	public void handleGet(HttpExchange exchange) throws IOException, JSONException {
		String body = Utils.convert(exchange.getRequestBody());
		JSONObject deseralized = new JSONObject(body);
		try {
			String actorId = deseralized.getString("actorId");

			if (Utils.exists(driver, "MATCH (n:actor) WHERE n.actorId = '%s' return n.actorId", actorId)) {
				getPath(actorId, exchange);
			} else {
				exchange.sendResponseHeaders(400, 0);
				Utils.sendEmptyBody(exchange);
			}
		} catch (Exception e) {
			exchange.sendResponseHeaders(400, 0);
			Utils.sendEmptyBody(exchange);
			e.printStackTrace();
		}
	}

	public void getPath(String actorId, HttpExchange exchange) throws IOException {
		try (Session session = driver.session()) {
			String response = session.writeTransaction(new TransactionWork<String>() {
				@Override
				public String execute(Transaction tx) {
					JSONObject json = new JSONObject();
					StatementResult result = tx.run(String.format(
							"MATCH p=shortestPath((n:actor {name: 'Kevin Bacon'})-[rel:ACTED_IN*]-(b:actor {actorId: '%s'})) RETURN extract(n IN nodes(p)) AS extracted;",
							actorId));

					if (!result.hasNext()) {
						return "";
					} else {
						Record record = result.next();
						ArrayList<JSONObject> tmp = new ArrayList<JSONObject>();
						JSONObject o1 = new JSONObject();
						JSONObject o2 = new JSONObject();
						try {
							o1.put("actorId", record.get(0).get(0).get("actorId", ""));
							o1.put("movieId", record.get(0).get(1).get("movieId", ""));
							o2.put("actorId", record.get(0).get(2).get("actorId", ""));
							o2.put("movieId", record.get(0).get(1).get("movieId", ""));

						} catch (JSONException e) {
							e.printStackTrace();
						}
						tmp.add(o1);
						tmp.add(o2);

						int size = record.get(0).size();

						for (int i = 3; i < size; i += 2) {
							JSONObject o = new JSONObject();
							try {
								o.put("actorId", record.get(0).get(i + 1).get("actorId", ""));
								o.put("movieId", record.get(0).get(i).get("movieId", ""));
							} catch (JSONException e) {
								e.printStackTrace();
							}
							tmp.add(o);
						}
						Collections.reverse(tmp);
						try {
							json.put("baconNumber", (size - 1) / 2);
							// System.out.println(size);
							json.put("baconPath", tmp);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					return json.toString();
				}
			});
			if (!response.isEmpty()) {
				exchange.sendResponseHeaders(200, response.length());
			} else {
				exchange.sendResponseHeaders(404, response.length());
			}
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, 0);
			Utils.sendEmptyBody(exchange);
		}
	}
}
