package org.pustefixframework.extension;

import java.util.List;

import de.schlund.pfixcore.auth.Role;

/**
 *  Extension for a {@link RoleExtensionPoint}.  
 * 
 * @author mleidig@schlund.de
 *
 */
public interface RoleExtension extends Extension {

	public List<Role> getRoles();
	
}
