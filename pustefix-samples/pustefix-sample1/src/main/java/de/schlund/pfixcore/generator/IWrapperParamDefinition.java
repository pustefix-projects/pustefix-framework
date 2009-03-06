package de.schlund.pfixcore.generator;

/**
 * Describe interface IWrapperParamDefinition here.
 *
 *
 * Created: Fri Apr 29 16:46:47 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public interface IWrapperParamDefinition {
    String                   getName();
    String                   getType();
    String                   getOccurance();
    String                   getFrequency();
    IWrapperParamCaster      getCaster();
    IWrapperParamPreCheck[]  getPreChecks();
    IWrapperParamPostCheck[] getPostChecks();
}
