package lab1;

public class Obfuscator {

    private static final String SOURCE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final String TARGET =
            "Q5A8ZWS0XEDC6RFVT9GBY4HNU3J2MI1KO7LP" +
            "qwertyuiopasdfghjklzxcvbnm1234567890";

    public static String obfuscate(String s) {

        if (s == null) return null;

        char[] result = new char[s.length()];

        for (int i = 0; i < s.length(); i++) {

            char c = s.charAt(i);
            int index = SOURCE.indexOf(c);

            result[i] = (index >= 0)
                    ? TARGET.charAt(index)
                    : c;
        }

        return new String(result);
    }

    public static String deobfuscate(String s) {

        if (s == null) return null;

        char[] result = new char[s.length()];

        for (int i = 0; i < s.length(); i++) {

            char c = s.charAt(i);
            int index = TARGET.indexOf(c);

            result[i] = (index >= 0)
                    ? SOURCE.charAt(index)
                    : c;
        }

        return new String(result);
    }
}