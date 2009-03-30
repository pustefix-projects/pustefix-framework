#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.contextresources;

import ${package}.User;

import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextUser extends ContextResource {
    public void setUser(User user);
    public User getUser();
}
