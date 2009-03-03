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
package org.pustefixframework.config.project;

import java.net.URL;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ProjectInfo {
    
    private URL projectConfigURL;
    private String projectName;
    
    public ProjectInfo(URL projectConfigURL) {
        this.projectConfigURL = projectConfigURL;
        projectName = extractProjectName(projectConfigURL);
    }
    
    public URL getProjectConfigURL() {
        return projectConfigURL;
    }
  
    public String getProjectName() {
        return projectName;
    }

    private String extractProjectName(URL url) {
        String s = url.toString();
        int i = s.lastIndexOf('/');
        if (i>0) {
            s = s.substring(0, i);
            i = s.lastIndexOf('/');
            if (i>0) {
                s = s.substring(0, i);
                i = s.lastIndexOf('/');
                if (i>0) {
                    s = s.substring(i+1);
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("Can't extract project name from config URL: "+url);
    }
    
}
