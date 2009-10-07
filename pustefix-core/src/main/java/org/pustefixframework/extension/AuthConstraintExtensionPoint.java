package org.pustefixframework.extension;

/**
 * An extension point that can be used to add authconstraints to an
 * application.  
 * 
 * @author mleidig@schlund.de
 *
 */
public interface AuthConstraintExtensionPoint extends ExtensionPoint<AuthConstraintExtension> {

    void updateExtension(AuthConstraintExtension extension);

}
