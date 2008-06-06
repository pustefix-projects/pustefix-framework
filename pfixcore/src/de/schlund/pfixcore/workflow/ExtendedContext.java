package de.schlund.pfixcore.workflow;

import de.schlund.pfixcore.exception.PustefixApplicationException;
import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.SPDocument;

/**
 * Implemented by a Context instance that provides request handling.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ExtendedContext extends Context {
    SPDocument handleRequest(PfixServletRequest preq) throws PustefixApplicationException, PustefixCoreException;
}