package de.schlund.pfixcore.oxm.bean;

/**
 * Simple test bean for OXM
 * 
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class BooleanPropertyBean {
    protected boolean admin = true;
    protected Boolean deleted = new Boolean(false);

    public boolean isAdmin() {
        return admin;
    }
    public Boolean getDeleted() {
        return deleted;
    }
}