package io.turnbase.games.sevenwondersduel.playthrough;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(20)
class DevServerTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final HttpClient client = HttpClient.newHttpClient();
  private DevGame game;
  private DevServer server;

  @BeforeEach
  void start() throws IOException {
    game = new DevGame(7);
    server = new DevServer(game, 0);
    server.start();
  }

  @AfterEach
  void stop() {
    server.stop();
  }

  private URI uri(String path) {
    return URI.create("http://127.0.0.1:" + server.port() + path);
  }

  private HttpResponse<String> get(String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(uri(path)).build(), HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> post(String path, String body) throws Exception {
    return client.send(
        HttpRequest.newBuilder(uri(path)).POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString());
  }

  /** Opens a stream and reads its events on a thread, as the host page's EventSource does. */
  private List<JsonNode> listen(String viewer) throws Exception {
    List<JsonNode> events = new ArrayList<>();
    HttpResponse<java.io.InputStream> response =
        client.send(
            HttpRequest.newBuilder(uri("/api/stream/" + viewer)).build(),
            HttpResponse.BodyHandlers.ofInputStream());
    Thread reader =
        new Thread(
            () -> {
              try (BufferedReader in =
                  new BufferedReader(
                      new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                  if (line.startsWith("data: ")) {
                    synchronized (events) {
                      events.add(MAPPER.readTree(line.substring(6)));
                    }
                  }
                }
              } catch (IOException closed) {
                // the server stopped
              }
            });
    reader.setDaemon(true);
    reader.start();
    return events;
  }

  private static List<JsonNode> await(List<JsonNode> events, int count)
      throws InterruptedException {
    long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (System.nanoTime() < until) {
      synchronized (events) {
        if (events.size() >= count) {
          return List.copyOf(events);
        }
      }
      Thread.sleep(20);
    }
    synchronized (events) {
      return List.copyOf(events);
    }
  }

  private String draftAction(String moverViewer) throws Exception {
    JsonNode view = MAPPER.readTree(get("/api/resync/" + moverViewer).body()).get("view");
    for (JsonNode placed : view.get("visible")) {
      if (placed.get("zone").asText().equals("draft")) {
        return "{\"type\":\"draftWonder\",\"component\":\""
            + placed.get("component").asText()
            + "\"}";
      }
    }
    throw new AssertionError("nothing to draft");
  }

  private String mover() throws Exception {
    String active = MAPPER.readTree(get("/api/resync/1").body()).get("view").get("active").asText();
    return active.endsWith("1") ? "1" : "2";
  }

  @Test
  void resyncServesTheViewersMessageAsJson() throws Exception {
    JsonNode message = MAPPER.readTree(get("/api/resync/1").body());

    assertThat(message.get("kind").asText()).isEqualTo("view");
    assertThat(message.get("catalog").get("entries")).isNotEmpty();
  }

  @Test
  void anActionReachesEveryStreamAndASpectatorSeesTheBroadcastOnly() throws Exception {
    List<JsonNode> one = listen("1");
    List<JsonNode> two = listen("2");
    List<JsonNode> spectator = listen("spectator");
    String mover = mover();

    assertThat(post("/api/action/" + mover, draftAction(mover)).statusCode()).isEqualTo(204);

    assertThat(await(one, 2)).hasSize(2);
    assertThat(await(two, 2)).hasSize(2);
    assertThat(await(spectator, 1)).hasSize(1);
    assertThat(spectator.get(0).get("channel").asText()).isEqualTo("game_event");
    assertThat(spectator.get(0).get("payload").get("broadcast").asBoolean()).isTrue();
    assertThat(one.get(1).get("payload").get("broadcast").asBoolean()).isFalse();
  }

  @Test
  void anIllegalActionIsRefusedOnTheActorsStreamOnly() throws Exception {
    List<JsonNode> actor = listen("1");
    List<JsonNode> other = listen("2");
    String wrong = mover().equals("1") ? "2" : "1";
    List<JsonNode> wrongStream = wrong.equals("1") ? actor : other;
    List<JsonNode> quiet = wrong.equals("1") ? other : actor;

    post("/api/action/" + wrong, draftAction(wrong));

    assertThat(await(wrongStream, 1)).hasSize(1);
    assertThat(wrongStream.get(0).get("payload").get("kind").asText()).isEqualTo("rejected");
    Thread.sleep(200);
    assertThat(quiet).isEmpty();
  }

  @Test
  void newAndAdvanceTellEveryStreamToResync() throws Exception {
    List<JsonNode> one = listen("1");

    post("/api/advance?steps=20", "");
    post("/api/new?seed=9", "");

    List<JsonNode> got = await(one, 2);
    assertThat(got).extracting(e -> e.get("channel").asText()).containsExactly("reset", "reset");
    assertThat(game.seed()).isEqualTo(9);
    assertThat(game.moves()).isZero();
    assertThat(MAPPER.readTree(get("/api/state").body()).get("seed").asLong()).isEqualTo(9);
  }

  @Test
  void aSpectatorsActionIsBadRequest() throws Exception {
    assertThat(post("/api/action/spectator", draftAction("1")).statusCode()).isEqualTo(400);
  }
}
