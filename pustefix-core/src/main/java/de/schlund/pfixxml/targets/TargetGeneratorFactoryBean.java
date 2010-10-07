/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml.targets;

import java.io.File;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.FileUtils;


/**
 * Factory bean providing an adapter to {@link TargetGeneratorFactory}.
 * Returns an instance of {@link TargetGenerator} for the configuration
 * file set using the {@link #setConfigFile(String)} method.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetGeneratorFactoryBean implements ServletContextAware {
    
    private ServletContext servletContext;
    private FileResource configFile;

    /**
     * Returns the TargetGenerator instance for the configured configuration
     * file path.
     * 
     * @return TargetGenerator instance
     * @throws Exception if TargetGenerator cannot be created
     */
    public Object getObject() throws Exception {
        TargetGenerator generator;
        if(servletContext.getRealPath("/") == null) {
            File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
            File cacheDir = new File(tmpDir, "pustefix-xsl-cache");
            if(cacheDir.exists()) {
                FileUtils.delete(cacheDir);
                cacheDir.mkdir();
            }
            FileResource cacheRes = ResourceUtil.getFileResource(cacheDir.toURI());
            generator = TargetGeneratorFactory.getInstance().createGenerator(configFile, cacheRes);
        } else {
            generator = TargetGeneratorFactory.getInstance().createGenerator(configFile);
        }
        return generator;
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
 
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
}