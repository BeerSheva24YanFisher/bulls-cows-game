package telran.game;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import telran.net.Protocol;
import telran.net.Request;
import telran.net.Response;
import telran.net.ResponseCode;
import telran.queries.entities.Game;
import telran.queries.entities.Move;
import telran.queries.service.BullsCowsService;

public class ProtocolBullsCows implements Protocol {
    private final BullsCowsService service;

    public ProtocolBullsCows(BullsCowsService service) {
        this.service = service;
    }

    @Override
    public Response getResponse(Request request) {
        String type = request.requestType();
        String data = request.requestData();
        Response response = null;

        try {
            Method method = ProtocolBullsCows.class.getDeclaredMethod(type, String.class);
            method.setAccessible(true);
            response = (Response) method.invoke(this, data);
        } catch (NoSuchMethodException e) {
            response = new Response(ResponseCode.WRONG_TYPE, "Unknown command: " + type);
        } catch (InvocationTargetException e) {
            Throwable causeExc = e.getCause();
            String msg = causeExc == null ? e.getMessage() : causeExc.getMessage();
            response = new Response(ResponseCode.WRONG_DATA, msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private Response register(String data) {
        JSONObject jsonObject = new JSONObject(data);
        String username = jsonObject.getString("username");
        LocalDate birthdate = LocalDate.parse(jsonObject.getString("birthdate"));
        service.saveGamer(username, birthdate);
        return new Response(ResponseCode.OK, "");
    }

    private Response createGame(String data) {
        long gameId = service.createGame();
        return new Response(ResponseCode.OK, String.valueOf(gameId));
    }

    private Response joinGame(String data) {
        JSONObject jsonObject = new JSONObject(data);
        long gameId = jsonObject.getLong("gameId");
        String username = jsonObject.getString("username");
        service.joinGame(gameId, username);
        return new Response(ResponseCode.OK, "");
    }

    private Response startGame(String data) {
        JSONObject jsonObject = new JSONObject(data);
        long gameId = jsonObject.getLong("gameId");
        String username = jsonObject.getString("username");
        service.startGame(gameId, username);
        return new Response(ResponseCode.OK, "");
    }

    private Response getNotStartedGamesWithUser(String data) {
        String username = data;
        List<Game> games = service.getNotStartedGamesWithUser(username);
        JSONArray jsonArray = new JSONArray(games.stream().map(Game::getId).toList());
        return new Response(ResponseCode.OK, jsonArray.toString());
    }

    private Response getStartedGamesWithUser(String data) {
        String username = data;
        List<Game> games = service.getStartedGamesWithUser(username);
        JSONArray jsonArray = new JSONArray(games.stream().map(Game::getId).toList());
        return new Response(ResponseCode.OK, jsonArray.toString());
    }

    private Response makeMove(String data) {
        JSONObject jsonObject = new JSONObject(data);
        long gameId = jsonObject.getLong("gameId");
        String username = jsonObject.getString("username");
        String move = jsonObject.getString("move");
        BullsCowsService.MoveResult result = service.makeMove(gameId, username, move);

        JSONObject responseJson = new JSONObject();
        responseJson.put("bulls", result.bulls());
        responseJson.put("cows", result.cows());
        responseJson.put("sequence", result.sequence());

        return new Response(ResponseCode.OK, responseJson.toString());
    }

    private Response getMovesByGameId(String data) {
        long gameId = Long.parseLong(data);
        List<Move> moves = service.getMovesByGameId(gameId);

        JSONArray jsonArray = new JSONArray(moves.stream().map(move -> {
            JSONObject jsonMove = new JSONObject();
            jsonMove.put("sequence", move.getSequence());
            jsonMove.put("bulls", move.getBulls());
            jsonMove.put("cows", move.getCows());
            return jsonMove;
        }).toList());

        return new Response(ResponseCode.OK, jsonArray.toString());
    }
}
