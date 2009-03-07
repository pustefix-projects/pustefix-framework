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

package de.schlund.pfixxml.targets;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;


/**
 * Factory bean providing an adapter to {@link TargetGeneratorFactory}.
 * Returns an instance of {@link TargetGenerator} for the configuration
 * file set using the {@link #setConfigFile(String)} method.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetGeneratorFactoryBean {
    
    private FileResource configFile;

    /**
     * Returns the TargetGenerator instance for the configured configuration
     * file path.
     * 
     * @return TargetGenerator instance
     * @throws Exception if TargetGenerator cannot be created
     */
    public Object getObject() throws Exception {
        return TargetGeneratorFactory.getInstance().createGenerator(configFile);
    }
    
    /**
     * Set the path to the configuration file for the TargetGenerator.
     * Has to be a URI understood by 
     * {@link ResourceUtil#getFileResource(String)}.
     * 
     * @param path URI of configuration file
     */
    public void setConfigFile(String path) {
        configFile = ResourceUtil.getFileResource(path);
    }
    
}