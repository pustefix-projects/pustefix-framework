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
 */

package org.pustefixframework.config.build;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * Task that initializes the global configuration with a docroot string,
 * so that other tasks can use the {@link ResourceUtil}.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class InitResourceUtilTask extends Task {

    private String docroot;

    @Override
    public void execute() throws BuildException {
        try {
            GlobalConfigurator.setDocroot(docroot);
        } catch(IllegalStateException x) {
            //ignore if docroot is already set (which can happen if
            //ant is called with multiple targets)
        }
    }
    
    public void setDocroot(String docroot) {
        this.docroot = docroot;
    }
}
