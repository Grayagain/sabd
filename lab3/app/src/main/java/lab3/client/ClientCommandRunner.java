package lab3.client;

import lab3.model.MessageResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ClientCommandRunner implements ApplicationRunner {

    private final MutualTlsClient client;
    private final ApplicationContext applicationContext;

    public ClientCommandRunner(MutualTlsClient client, ApplicationContext applicationContext) {
        this.client = client;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.getNonOptionArgs().isEmpty()) {
            return;
        }

        String command = args.getNonOptionArgs().get(0).toLowerCase();
        if ("server".equals(command)) {
            return;
        }

        if ("client".equals(command)) {
            String message = args.getNonOptionArgs().size() >= 2 ? args.getNonOptionArgs().get(1) : "Hello from mutual TLS client";
            String url = args.getNonOptionArgs().size() >= 3 ? args.getNonOptionArgs().get(2) : null;
            printResponse(url == null ? client.send(message) : client.send(message, url));
            shutdown();
            return;
        }

        if ("demo".equals(command)) {
            String message = args.getNonOptionArgs().size() >= 2 ? args.getNonOptionArgs().get(1) : "Demo mutual TLS message";
            printResponse(client.send(message));
            shutdown();
            return;
        }

        printUsage();
        shutdown();
    }

    private void printResponse(MessageResponse response) {
        System.out.println("Status : " + response.status());
        System.out.println("Echo : " + response.echo());
        System.out.println("Client Principal : " + response.clientPrincipal());
        System.out.println("Client Subject : " + response.clientSubject());
    }

    private void printUsage() {
        System.out.println("Usage:");
        System.out.println("  server");
        System.out.println("  client <message> [url]");
        System.out.println("  demo [message]");
    }

    private void shutdown() {
        int exitCode = org.springframework.boot.SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }
}
