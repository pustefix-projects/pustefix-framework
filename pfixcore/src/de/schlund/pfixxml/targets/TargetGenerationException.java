package de.schlund.pfixxml.targets;

/**
 * Exception subclass which represents errors during generation of a target.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TargetGenerationException extends Exception {
    
    private String targetkey;
    private Throwable cause;
       
    public TargetGenerationException() {
        super();
    }
    
    public TargetGenerationException(String msg) {
        super(msg);
    }
    
    public TargetGenerationException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    public TargetGenerationException(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getNestedException() {
        return cause;    
    }
     
    /**
     * @return
     */
    public String getTargetkey() {
        return targetkey;
    }

    /**
     * @param string
     */
    public void setTargetkey(String key) {
        targetkey = key;
    }


}
