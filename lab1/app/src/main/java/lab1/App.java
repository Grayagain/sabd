package lab1;

public class App {

    private static final String ATTRIBUTE_FLAG = "--attributes";

    public static void main(String[] args) throws Exception {

        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: obfuscate|deobfuscate input.xml output.xml [--attributes]");
            return;
        }

        String mode = args[0];
        String input = args[1];
        String output = args[2];
        boolean processAttributes = args.length == 4;

        if (processAttributes && !ATTRIBUTE_FLAG.equalsIgnoreCase(args[3])) {
            System.out.println("Unknown option: " + args[3]);
            return;
        }

        if (mode.equalsIgnoreCase("obfuscate")) {
            XmlProcessor.process(input, output, true, processAttributes);
        } else if (mode.equalsIgnoreCase("deobfuscate")) {
            XmlProcessor.process(input, output, false, processAttributes);
        } else {
            System.out.println("Unknown mode");
        }
    }
}
