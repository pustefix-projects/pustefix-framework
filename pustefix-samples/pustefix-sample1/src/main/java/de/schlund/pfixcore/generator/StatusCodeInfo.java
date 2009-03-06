package de.schlund.pfixcore.generator;

import de.schlund.util.statuscodes.StatusCode;

/**
 * Describe class StatusCodeInfo here.
 *
 *
 * Created: Mon Jul 25 11:06:07 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class StatusCodeInfo {
    private StatusCode scode;
    private String[]   args;
    private String     level;
    
    /**
     * Creates a new <code>StatusCodeInfo</code> instance.
     *
     * @param scode a <code>StatusCode</code> value
     */
    public StatusCodeInfo(StatusCode scode, String[] args, String level) {
        this.scode = scode;
        this.args  = args;
        this.level = level;
    }

    public StatusCode getStatusCode() {
        return scode;
    }

    public String[] getArgs() {
        return args;
    }

    public String getLevel() {
        return level;
    }

    public boolean equals(Object in) {
        if (!(in instanceof StatusCodeInfo)) {
            return false;
        }
        return this.hashCode() == ((StatusCodeInfo) in).hashCode();
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();
        tmp.append(scode.getStatusCodeId() + "|");
        if (args != null) {
            for (int i = 0; i < args.length ; i++) {
                tmp.append(args[i]);
            }
        }
        tmp.append("|");
        if (level != null) {
            tmp.append(level);
        }
        return tmp.toString();
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
}
