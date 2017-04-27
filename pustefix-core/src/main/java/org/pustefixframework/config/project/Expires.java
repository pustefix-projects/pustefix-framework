package org.pustefixframework.config.project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Expires {

    private static Pattern EXPIRES_PATTERN = Pattern.compile("(A|M)?(\\d+)");

    public enum Base {ACCESS, MODIFICATION};

    private final Base base;
    private final long seconds;

    public Expires(Base base, long seconds) {
        this.base = base;
        this.seconds = seconds;
    }

    public Base getBase() {
        return base;
    }

    public long getSeconds() {
        return seconds;
    }

    public static Expires valueOf(String value) {
        Matcher matcher = EXPIRES_PATTERN.matcher(value);
        if(matcher.matches()) {
            Base base = Base.ACCESS;
            if("M".equals(matcher.group(1))) {
                base = Base.MODIFICATION;
            }
            return new Expires(base, Long.valueOf(matcher.group(2)));
        } else {
            throw new IllegalArgumentException("Expires value doesn't match pattern '" +
                    EXPIRES_PATTERN + "': " + value);
        }
    }

}
