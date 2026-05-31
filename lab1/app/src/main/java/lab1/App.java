package lab1;

public class App {

    private static final String ATTRIBUTES_FLAG = "--attributes";

    public static void main(String[] args) throws Exception {

        if (args.length != 3 && args.length != 4) {
            printUsage();
            return;
        }

        String mode = args[0];
        String input = args[1];
        String output = args[2];
        boolean includeAttributes = args.length == 4 && ATTRIBUTES_FLAG.equalsIgnoreCase(args[3]);

        if (args.length == 4 && !includeAttributes) {
            printUsage();
            return;
        }

        if (mode.equalsIgnoreCase("obfuscate")) {
            XmlProcessor.process(input, output, true, includeAttributes);
        } else if (mode.equalsIgnoreCase("deobfuscate")) {
            XmlProcessor.process(input, output, false, includeAttributes);
        } else {
            System.out.println("Unknown mode");
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: obfuscate|deobfuscate input.xml output.xml [--attributes]");
        System.out.println("Add --attributes to process XML attribute values too.");
    }
}
