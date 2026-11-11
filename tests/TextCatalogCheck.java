package tv.afnon.home;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** Compiled formatter output; the fixture was captured before catalog extraction. */
public final class TextCatalogCheck {
    public static void main(String[] names) throws Exception {
        for (boolean ar : new boolean[] {false, true}) {
            for (boolean eastern : new boolean[] {false, true}) {
                Text text = new Text(ar, eastern);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                for (String name : names) {
                    Method method = null;
                    for (Method candidate : Text.class.getDeclaredMethods()) {
                        if (candidate.getName().equals(name)) method = candidate;
                    }
                    if (method == null) throw new AssertionError("Missing text method: " + name);
                    for (String sample : new String[] {"sample 7 · 8", null}) {
                        Object[] params = new Object[method.getParameterCount()];
                        for (int i = 0; i < params.length; i++) {
                            if (method.getParameterTypes()[i] != String.class) throw new AssertionError("Unexpected parameter: " + name);
                            params[i] = sample;
                        }
                        Object value = method.invoke(text, params);
                        String result;
                        if (value instanceof String[]) {
                            String[] list = (String[]) value;
                            result = String.join("\0", list);
                            list[0] = "changed";
                            if (((String[]) method.invoke(text, params))[0].equals("changed")) throw new AssertionError("Shared mutable labels: " + name);
                        } else result = (String) value;
                        String encoded = Base64.getEncoder().encodeToString(result.getBytes(StandardCharsets.UTF_8));
                        digest.update((name + "\n" + encoded + "\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
                StringBuilder hash = new StringBuilder();
                for (byte b : digest.digest()) hash.append(String.format("%02x", b & 255));
                System.out.println(ar + "-" + eastern + ":" + hash);
            }
        }
    }
}
