package de.schlund.pfixcore.util.email;


/**
 * Class for handling errors for EmailSender.
 *
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */

public class EmailSenderException extends Exception {
    
    private String message_;
    
    public EmailSenderException(String message) {
        super(message);
        this.message_=message;
    }
    
    public String getMessage() {
        return message_;
    }

}
