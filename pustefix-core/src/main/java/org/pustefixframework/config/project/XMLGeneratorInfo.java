/*
 * Place license here
 */

package org.pustefixframework.config.project;


public class XMLGeneratorInfo {
    private String configurationFile;

    public XMLGeneratorInfo(String configurationFile) {
        this.configurationFile = configurationFile;
    }
    
    public String getConfigurationFile() {
        return this.configurationFile;
    }
}
