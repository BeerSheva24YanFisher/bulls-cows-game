package telran.game;

import java.time.LocalDate;
import java.util.List;

import telran.queries.entities.Game;
import telran.queries.entities.Move;
import telran.queries.service.BullsCowsService;
import telran.view.InputOutput;
import telran.view.Item;
import telran.view.Menu;

public class BullsCowsItems {

    private static BullsCowsService service;

    public static Item[] getUserMenu(BullsCowsService service) {
        BullsCowsItems.service = service;
        return new Item[] {
            Item.of("Create User", BullsCowsItems::signUp),
            Item.of("Sign in", BullsCowsItems::signIn),
            Item.ofExit()
        };
    }

    private static Item[] getMenuItems(String username) {
        return new Item[] {
            Item.of("Create Game", BullsCowsItems::createGameWithRandomName),
            Item.of("Start Game", io -> startGame(io, username)),
            Item.of("Join Game", io -> joinGame(io,username)),
            Item.of("Play Game", io -> playGame(io, username)),
            Item.of("View not started games with user", io -> getNotStartedGamesWithUser(io, username)),
            Item.of("View not started games without user", io -> getNotStartedGamesWithoutUser(io, username)),
            Item.of("View started games with user", io -> viewStartedJoinedGames(io, username)),
            Item.ofExit()
        };
    }

    static void createGameWithRandomName(InputOutput io) {
        long gameId = service.createGame();
        io.writeLine("Game created with ID: " + gameId);
    }

    static void startGame(InputOutput io, String username) {
        io.writeLine("Enter the Game ID to start:");
        String gameIdStr = io.readString("");

        long gameId;
        try {
            gameId = Long.parseLong(gameIdStr);
        } catch (NumberFormatException e) {
            io.writeLine("Invalid Game ID. Please enter a valid number.");
            return;
        }

        if(service.isGameEmpty(gameId, username)){
            io.writeLine("the game cannot be started, you must join it first");
        } else{
            try {
                service.startGame(gameId, username);
                io.writeLine("Game with ID " + gameId + " has started.");
            } catch (Exception e) {
                io.writeLine("Error: " + e.getMessage());
            }
        }
    }

    static void joinGame(InputOutput io, String username) {
        io.writeLine("Enter the Game ID to join:");
        String gameIdStr = io.readString("");

        long gameId;
        
        try {
            gameId = Long.parseLong(gameIdStr);
        } catch (NumberFormatException e) {
            io.writeLine("Invalid Game ID. Please enter a valid number.");
            return;
        }

        if(service.isGameStarted(gameId)){
            io.writeLine("the game already started, you can not join");
            return;
        }

        try {
            service.joinGame(gameId, username);
            io.writeLine("Successfully joined the game with ID: " + gameId);
        } catch (Exception e) {
            io.writeLine("Error: " + e.getMessage());
        }
    }

    static void playGame(InputOutput io, String username) {
        io.writeLine("Enter the Game ID to play:");
        String gameIdStr = io.readString("");
        long gameId;
        try {
            gameId = Long.parseLong(gameIdStr);
        } catch (NumberFormatException e) {
            io.writeLine("Invalid Game ID. Please enter a valid number.");
            return;
        }

        if (service.isPlayerNotJoined(gameId, username)) {
            io.writeLine("You need to join the game first.");
            return;
        }

        Menu menu = new Menu("Game Menu", getGameMenuItems(gameId, username));
        menu.perform(io);
    }

    private static Item[] getGameMenuItems(long gameId, String username) {
        return new Item[] {
            Item.of("Make a Move", io -> makeMove(io, gameId, username)),
            Item.of("View Moves", io -> viewMoves(io, gameId)),
            Item.ofExit()
        };
    }
    
    private static void makeMove(InputOutput io, long gameId, String username) {
        if (service.getGame(gameId).isFinished()) {
            List<Move> moves = service.getMovesByGameId(gameId);
            for (Move move : moves) {
                if(move.getBulls()==4){
                    String winnerName = move.getGameGamer().getGamer().getUsername();
                    io.writeLine("Game finished, winner: " + winnerName);  
                    return;
                }

            }
            
        }

        io.writeLine("Enter your move:");
        String move = io.readString("");
    
        try {
            BullsCowsService.MoveResult result = service.makeMove(gameId, username, move);
            io.writeLine("Move result: " + result.toString());          
        } catch (Exception e) {
            io.writeLine("Error: " + e.getMessage());
        }
    }
    
    private static void viewMoves(InputOutput io, long gameId) {
        List<Move> moves = service.getMovesByGameId(gameId);
        if (moves.isEmpty()) {
            io.writeLine("No moves made in this game.");
        } else {
            io.writeLine("Moves made in the game:");
            for (Move move : moves) {
                io.writeLine(move.getSequence());
            }
        }
    }

    static void getNotStartedGamesWithUser(InputOutput io, String username) {
        List<Game> games = service.getNotStartedGamesWithUser(username);
        if (games.isEmpty()) {
            io.writeLine("No games available.");
        } else {
            for (Game game : games) {
                io.writeLine("Game ID: " + game.getId());
            }
        }
    }

    static void getNotStartedGamesWithoutUser(InputOutput io, String username) {
        List<Game> games = service.getNotStartedGamesWithoutUser(username);
        if (games.isEmpty()) {
            io.writeLine("No games available.");
        } else {
            for (Game game : games) {
                io.writeLine("Game ID: " + game.getId());
            }
        }
    }

    static void viewStartedJoinedGames(InputOutput io, String username) {
        List<Game> games = service.getStartedGamesWithUser(username);
        if (games.isEmpty()) {
            io.writeLine("No games available.");
        } else {
            for (Game game : games) {
                io.writeLine("Game id: " + game.getId() + " | Finished " + game.isFinished());
            }
        }
    }

    private static void signUp(InputOutput io) {
        String username = io.readString("Enter your username:");
        if (service.getUser(username) != null) {
            io.writeLine("A user with the same name already exists. Try a different name.");
            return;
        }
        String birthDateStr = io.readString("Enter your date of birth (format: YYYY-MM-DD):");
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(birthDateStr);
        } catch (NumberFormatException e) {
            io.writeLine("Invalid birthdate. Please enter a valid data.");
            return;
        }
        service.saveGamer(username, birthDate);
        io.writeLine("The user has been successfully registered!");
    }

    private static void signIn(InputOutput io) {
        String username = io.readString("Enter your username:");
        if (service.getUser(username) == null) {
            io.writeLine("User not found. Please register");
            return;
        }
        Menu menu = new Menu("Bulls and Cows Game", getMenuItems(username));
        menu.perform(io);
    }
}