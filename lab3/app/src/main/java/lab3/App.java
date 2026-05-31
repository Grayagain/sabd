package lab3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(App.class);

        if (isClientMode(args)) {
            application.setWebApplicationType(WebApplicationType.NONE);
        }

        application.run(args);
    }

    private static boolean isClientMode(String[] args) {
        if (args.length == 0) {
            return false;
        }

        String command = args[0].toLowerCase();
        return "client".equals(command) || "demo".equals(command);
    }
}
