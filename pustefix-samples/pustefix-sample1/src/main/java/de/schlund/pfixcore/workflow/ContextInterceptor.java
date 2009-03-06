package de.schlund.pfixcore.workflow;

import de.schlund.pfixxml.PfixServletRequest;

/**
 * Describe interface ContextInterceptor here.
 *
 *
 * Created: Thu Apr  7 21:26:50 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public interface ContextInterceptor {
    void process(Context context, PfixServletRequest preq);
}

