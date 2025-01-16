package telran.game;


import telran.net.NetworkClient;
import telran.net.TcpClient;
import telran.queries.service.BullsCowsService;
import telran.view.InputOutput;
import telran.view.Item;
import telran.view.Menu;
import telran.view.StandardInputOutput;

public class Main {
    private static final String HOST = "localhost";
    //private static final String HOST = "16.171.22.97";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        InputOutput io = new StandardInputOutput();
        NetworkClient client = new TcpClient(HOST, PORT);
        BullsCowsService service = new BullsCowsNetProxy(client);
        Item[] items = BullsCowsItems.getUserMenu(service);
        Menu menu = new Menu("Bulls and Cows game", items);
        menu.perform(io);
        io.writeLine("Application is finished");
    }

}