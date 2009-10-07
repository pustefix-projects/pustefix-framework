package org.pustefixframework.extension;

import java.util.List;

import de.schlund.pfixcore.auth.AuthConstraint;

/**
 *  Extension for a {@link AuthConstraintExtensionPoint}.  
 * 
 * @author mleidig@schlund.de
 *
 */
public interface AuthConstraintExtension extends Extension {

	public List<AuthConstraint> getAuthConstraints();
	
}
