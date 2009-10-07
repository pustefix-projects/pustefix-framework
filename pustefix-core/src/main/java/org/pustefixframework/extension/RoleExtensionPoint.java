package org.pustefixframework.extension;

/**
 * An extension point that can be used to add roles to an
 * application.  
 * 
 * @author mleidig@schlund.de
 *
 */
public interface RoleExtensionPoint extends ExtensionPoint<RoleExtension> {

    void updateExtension(RoleExtension extension);

}
