package banking;

public class test {
    public static void main(String[] args) {
        String pattern = "DDA";
        String text = "DDABCD";
        long patternHash = hash(pattern);
        long substringHash = hash(text.substring(text.length() - pattern.length()));
        char[] t = text.toCharArray();
        for (int i = t.length - pattern.length(); i >= 0; i--) {
            System.out.print(substringHash + " ");
            if (substringHash == patternHash) {
                if (pattern.equals(text.substring(i, i + pattern.length()))) {
                    System.out.print("equals");
                } else {
                    System.out.print("not equals");
                }
            }
            System.out.println();
            if (i != 0) {
                substringHash = rollingHash(substringHash, t[i - 1], t[i + pattern.length() - 1]);
            }
        }
    }

    private static long hash(String str) {
        long hash = 0;
        int a = 3;
        long powA = 1;
        int m = 11;
        char[] c = str.toCharArray();
        for (char value : c) {
            hash += ((value - 'A' + 1) * powA) % m;
            powA *= a;
        }
        return hash % m;
    }

    private static long rollingHash(long prevHash, char newChar, char oldChar) {
        long newHash = ((prevHash - (oldChar - 'A' + 1) * 9) * 3 + (newChar - 'A' + 1)) % 11;
        if (newHash < 0) {
            newHash += 11;
        }
        return newHash;
    }
}
