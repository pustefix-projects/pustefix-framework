package de.schlund.pfixcore.workflow.context;

import de.schlund.pfixcore.exception.PustefixApplicationException;

/**
 * Exception thrown when a page is called passing an action
 * parameter value which isn't configured for the page.
 * 
 * @author mleidig@schlund.de
 *
 */
public class UnknownActionException extends PustefixApplicationException {

    private static final long serialVersionUID = 3346787036294866968L;

    private String action;
    private String page;
    
    public UnknownActionException(String action, String page) {
        this.action = action;
        this.page = page;
    }
    
    public String getAction() {
        return action;
    }
    
    public String getPage() {
        return page;
    }
    
    @Override
    public String getMessage() {
        return "Page '" + page + "' has been called with unknown action '" + action + "'.";
    }
    
}
