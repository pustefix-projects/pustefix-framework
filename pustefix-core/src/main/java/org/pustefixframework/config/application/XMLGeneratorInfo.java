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

package org.pustefixframework.config.application;


public class XMLGeneratorInfo {
    
    private String configurationFile;
    private String targetGeneratorBeanName;
    private boolean checkModtime = true;
    
    public XMLGeneratorInfo() {
    }
    
    public XMLGeneratorInfo(String configurationFile, String targetGeneratorBeanName, boolean checkModtime) {
        this.configurationFile = configurationFile;
        this.targetGeneratorBeanName = targetGeneratorBeanName;
        this.checkModtime = checkModtime;
    }
    
    public String getConfigurationFile() {
        return this.configurationFile;
    }
    
    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }
    
    public String getTargetGeneratorBeanName() {
        return this.targetGeneratorBeanName;
    }
    
    public void setTargetGeneratorBeanName(String targetGeneratorBeanName) {
        this.targetGeneratorBeanName = targetGeneratorBeanName;
    }
    
    public boolean getCheckModtime() {
        return checkModtime;
    }
    
    public void setCheckModtime(boolean checkModtime) {
        this.checkModtime = checkModtime;
    }

}
