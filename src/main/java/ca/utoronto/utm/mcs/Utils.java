package ca.utoronto.utm.mcs;

import static org.neo4j.driver.v1.Values.parameters;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import com.sun.net.httpserver.HttpExchange;

public class Utils {
  public static String convert(InputStream inputStream) throws IOException {

    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }
  
  public static boolean exists(Driver driver, String query, String p) {
	  try (Session session = driver.session()) {
	      String transaction = session.writeTransaction(new TransactionWork<String>() {
	        @Override
	        public String execute(Transaction tx) {
	          StatementResult result = tx.run(String.format(query, p));
	          return result.next().get("actorId", "");
	        }
	      });
	      if(transaction.isEmpty()) {
	    	  return false;
	      }
	      return true;
	    } catch (Exception e) {
	    	return false;
	    }
	  }
  
  public static void sendEmptyBody(HttpExchange exchange) throws IOException {
	  String response = "";
	  OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
  }

  public static void queryCreate(Driver driver, String query, String p1, String p2,
      HttpExchange exchange) throws IOException {
    try (Session session = driver.session()) {
      String transaction = session.writeTransaction(new TransactionWork<String>() {
        @Override
        public String execute(Transaction tx) {
          tx.run(String.format(query, p1, p2));
          return "";
        }
      });
      exchange.sendResponseHeaders(200, 0);
      OutputStream os = exchange.getResponseBody();
      os.write("".getBytes());
      os.close();
    } catch (Exception e) {
      exchange.sendResponseHeaders(500, 0);
    }
  }
}
