package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class MovieQuery implements HttpHandler{
  public void handle(HttpExchange exchange) {
    try {
      if (exchange.getRequestMethod().equals("GET")) {
          //handleGet(exchange);
      }
      else if (exchange.getRequestMethod().equals("PUT")) {
          handlePut(exchange);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void handlePut(HttpExchange exchange) throws IOException, JSONException {
    String body = Utils.convert(exchange.getRequestBody());
    JSONObject deseralized = new JSONObject(body);
    String movie;
    String movieId;
    try {
      movie = deseralized.getString("movie");
      movieId = deseralized.getString("movieId");
    } catch (Exception e) {
      exchange.sendResponseHeaders(400, 0);
      e.printStackTrace();
    }
  }
  

}
