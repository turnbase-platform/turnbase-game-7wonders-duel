package io.turnbase.games.sevenwondersduel.playthrough;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.turnbase.games.sevenwondersduel.playthrough.DevGame.Delivery;
import io.turnbase.games.sevenwondersduel.wire.WireAction;
import io.turnbase.serialization.GdkSerialization;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * The backend of the frontend dev harness: {@link DevGame} behind a loopback HTTP server, for the
 * Vite host page in {@code dev/}. Test scope, never packaged.
 *
 * <ul>
 *   <li>{@code GET /api/resync/{viewer}} the viewer's current view, as {@code onResync} gives it
 *   <li>{@code GET /api/stream/{viewer}} server-sent events: {@code {"channel", "payload"}} per
 *       delivery, and {@code {"channel":"reset"}} when the position was replaced
 *   <li>{@code POST /api/action/{viewer}} a {@link WireAction}; answers arrive on the streams
 *   <li>{@code POST /api/new?seed=N} a new game from that seed
 *   <li>{@code POST /api/advance?steps=K} plays K random legal moves silently, then resets
 *   <li>{@code GET /api/state} seed and number of moves, for the host page's status line
 * </ul>
 */
public final class DevServer {

  private static final GdkSerialization JSON = new GdkSerialization();

  private record Stream(String viewer, OutputStream out) {}

  private final DevGame game;
  private final HttpServer http;
  private final List<Stream> streams = new CopyOnWriteArrayList<>();

  DevServer(DevGame game, int port) throws IOException {
    this.game = game;
    this.http = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
    http.setExecutor(Executors.newCachedThreadPool());
    http.createContext("/api/", this::route);
  }

  public static void main(String[] args) throws IOException {
    int port = args.length > 0 ? Integer.parseInt(args[0]) : 8787;
    long seed = args.length > 1 ? Long.parseLong(args[1]) : 7;
    new DevServer(new DevGame(seed), port).start();
    System.out.println("dev game on http://127.0.0.1:" + port + " (seed " + seed + ")");
  }

  void start() {
    http.start();
  }

  void stop() {
    http.stop(0);
  }

  int port() {
    return http.getAddress().getPort();
  }

  private void route(HttpExchange exchange) throws IOException {
    try {
      String[] path = exchange.getRequestURI().getPath().split("/");
      String method = exchange.getRequestMethod();
      String what = path.length > 2 ? path[2] : "";
      String viewer = path.length > 3 ? path[3] : "";
      switch (method + " " + what) {
        case "GET resync" ->
            reply(exchange, 200, JSON.writePayload(game.resync(viewer), Long.MAX_VALUE));
        case "GET stream" -> stream(exchange, viewer);
        case "GET state" ->
            reply(exchange, 200, "{\"seed\":" + game.seed() + ",\"moves\":" + game.moves() + "}");
        case "POST action" -> {
          String body =
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          publish(game.act(viewer, JSON.readAction(body, WireAction.class)));
          reply(exchange, 204, "");
        }
        case "POST new" -> {
          game.restart(Long.parseLong(query(exchange, "seed", String.valueOf(game.seed()))));
          reset();
          reply(exchange, 204, "");
        }
        case "POST advance" -> {
          game.advance(Integer.parseInt(query(exchange, "steps", "1")));
          reset();
          reply(exchange, 204, "");
        }
        default -> reply(exchange, 404, "no such endpoint");
      }
    } catch (IllegalArgumentException e) {
      reply(exchange, 400, String.valueOf(e.getMessage()));
    }
  }

  private static String query(HttpExchange exchange, String key, String fallback) {
    String query = exchange.getRequestURI().getQuery();
    if (query != null) {
      for (String pair : query.split("&")) {
        if (pair.startsWith(key + "=")) {
          return pair.substring(key.length() + 1);
        }
      }
    }
    return fallback;
  }

  private static void reply(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, status == 204 ? -1 : bytes.length);
    if (status != 204) {
      exchange.getResponseBody().write(bytes);
    }
    exchange.close();
  }

  private void stream(HttpExchange exchange, String viewer) throws IOException {
    if (!DevGame.VIEWERS.contains(viewer)) {
      reply(exchange, 404, "no such viewer");
      return;
    }
    exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
    exchange.getResponseHeaders().set("Cache-Control", "no-cache");
    exchange.sendResponseHeaders(200, 0);
    OutputStream out = exchange.getResponseBody();
    out.write(": connected\n\n".getBytes(StandardCharsets.UTF_8));
    out.flush();
    streams.add(new Stream(viewer, out));
  }

  private void publish(List<Delivery> deliveries) {
    for (Delivery d : deliveries) {
      String payload = JSON.writePayload(d.payload(), Long.MAX_VALUE);
      send(d.viewer(), "{\"channel\":\"" + d.channel() + "\",\"payload\":" + payload + "}");
    }
  }

  private void reset() {
    DevGame.VIEWERS.forEach(v -> send(v, "{\"channel\":\"reset\"}"));
  }

  private void send(String viewer, String json) {
    byte[] event = ("data: " + json + "\n\n").getBytes(StandardCharsets.UTF_8);
    for (Stream s : streams) {
      if (s.viewer().equals(viewer)) {
        try {
          s.out().write(event);
          s.out().flush();
        } catch (IOException gone) {
          streams.remove(s);
        }
      }
    }
  }
}
