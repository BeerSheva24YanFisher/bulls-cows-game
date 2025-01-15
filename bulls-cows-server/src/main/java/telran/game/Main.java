package telran.game;

import java.util.HashMap;
import java.util.Scanner;

import org.hibernate.jpa.HibernatePersistenceProvider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;

import telran.net.Protocol;
import telran.net.TcpServer;
import telran.queries.config.BullsCowsPersistenceUnitInfo;
import telran.queries.repo.BullsCowsRepositoryJpaImpl;
import telran.queries.service.BullsCowsServiceImpl;

public class Main {
    public static void main(String[] args) {
        EntityManager em = createEntityManager();
        BullsCowsServiceImpl service = new BullsCowsServiceImpl(new BullsCowsRepositoryJpaImpl(em));
        
        Protocol protocol = new ProtocolBullsCows(service);
        TcpServer server = new TcpServer(
            protocol, 
            ServerConfigProperties.PORT,
            ServerConfigProperties.BAD_RESPONSES,
            ServerConfigProperties.REQUEST_PER_SECOND,
            ServerConfigProperties.TOTAL_TIMEOUT, 0
        );

        Thread serverThread = new Thread(server);
        serverThread.start();

        // Shutdown Logic
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("To shutdown server input \"shutdown\": ");
            String command = scanner.nextLine();
            if ("shutdown".equalsIgnoreCase(command)) {
                server.shutdown();
                break;
            }
        }
        scanner.close();
    }

    private static EntityManager createEntityManager() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        PersistenceUnitInfo persistenceUnit = new BullsCowsPersistenceUnitInfo();
        HibernatePersistenceProvider provider = new HibernatePersistenceProvider();
        EntityManagerFactory emf = provider.createContainerEntityManagerFactory(persistenceUnit, properties);
        return emf.createEntityManager();
    }
}