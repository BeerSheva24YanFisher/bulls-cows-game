package telran.game;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

import telran.net.NetworkClient;
import telran.queries.entities.Game;
import telran.queries.entities.Gamer;
import telran.queries.entities.Move;
import telran.queries.service.BullsCowsService;

public class BullsCowsNetProxy implements BullsCowsService {
    NetworkClient client;

    public BullsCowsNetProxy(NetworkClient client) {
        this.client = client;
    }

    @Override
    public long createGame() {
        return Long.parseLong(client.sendAndReceive("createGame", ""));
    }

    @Override
    public Game getGame(long gameId) {
        String response = client.sendAndReceive("getGame", Long.toString(gameId));
        JSONObject jsonObject = new JSONObject(response);
        return new Game(jsonObject);
    }

    @Override
    public List<Move> getMovesByGameId(long gameId) {
        String response = client.sendAndReceive("getMovesByGameId", Long.toString(gameId));
        return resultsFromJSON(response, s -> new Move(new JSONObject(s)));
    }

    @Override
    public List<Game> getNotStartedGamesWithUser(String username) {
        String response = client.sendAndReceive("getNotStartedGamesWithUser", username);
        return resultsFromJSON(response, s -> new Game(new JSONObject(s)));
    }

    @Override
    public List<Game> getNotStartedGamesWithoutUser(String username) {
        String response = client.sendAndReceive("getNotStartedGamesWithoutUser", username);
        return resultsFromJSON(response, s -> new Game(new JSONObject(s)));
    }

    @Override
    public List<Game> getStartedGamesWithUser(String username) {
        String response = client.sendAndReceive("getStartedGamesWithUser", username);
        return resultsFromJSON(response, s -> new Game(new JSONObject(s)));
    }

    @Override
    public Gamer getUser(String username) {
        String response = client.sendAndReceive("getUser", username);
        return new Gamer(new JSONObject(response));
    }

    @Override
    public boolean isGameEmpty(long gameId, String username) {
        String response = client.sendAndReceive("isGameEmpty", gameId + "," + username);
        return Boolean.parseBoolean(response);
    }

    @Override
    public boolean isGameStarted(long gameId) {
        String response = client.sendAndReceive("isGameStarted", Long.toString(gameId));
        return Boolean.parseBoolean(response);
    }

    @Override
    public boolean isPlayerNotJoined(long gameId, String username) {
        String response = client.sendAndReceive("isPlayerNotJoined", gameId + "," + username);
        return Boolean.parseBoolean(response);
    }

    @Override
    public void joinGame(long gameId, String username) {
        client.sendAndReceive("joinGame", gameId + "," + username);
    }

    @Override
    public MoveResult makeMove(long gameId, String username, String move) {
        String response = client.sendAndReceive("makeMove", gameId + "," + username + "," + move);
        return new MoveResult(new JSONObject(response));
    }

    @Override
    public void saveGamer(String username, LocalDate birthDate) {
        client.sendAndReceive("saveGamer", username + "," + birthDate.toString());
    }

    @Override
    public void startGame(long gameId, String username) {
        client.sendAndReceive("startGame", gameId + "," + username);
    }

    private <T> List<T> resultsFromJSON(String res, Function<JSONObject, T> map) {
        JSONArray jsonArray = new JSONArray(res);
        return jsonArray.toList().stream()
            .map(obj -> map.apply(new JSONObject((Map<?, ?>) obj)))
            .toList();
    }
}
