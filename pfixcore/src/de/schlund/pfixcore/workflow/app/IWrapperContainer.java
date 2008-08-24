/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.workflow.app;
import org.pustefixframework.config.contextxmlservice.StateConfig;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

/**
 * All classes which want to act as a container for classes
 * which implement the {@link IWrapper} interface must 
 * implement this interface. An IWrapperContainer is created
 * for each request.
 */

public interface IWrapperContainer {
    void    init(Context context, PfixServletRequest preq, ResultDocument resdoc, StateConfig stateConfig) throws Exception;
    void    handleSubmittedData() throws Exception;
    void    retrieveCurrentStatus(boolean all) throws Exception;
    void    addErrorCodes() throws Exception;
    void    addStringValues() throws Exception;
    void    addIWrapperStatus() throws Exception;
    boolean errorHappened() throws Exception;
}
