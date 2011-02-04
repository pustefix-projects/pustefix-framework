package org.pustefixframework.agent;

/**
 * Holder of basic POM information (groupId, artifactId, version)
 * 
 * @author mleidig@schlund.de
 *
 */
public class POMInfo {
    
    private String groupId;
    private String artifactId;
    private String version;
    
    public POMInfo() {}
    
    public POMInfo(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public boolean isComplete() {
        return !(groupId == null || artifactId == null || version == null);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof POMInfo) {
            return ((POMInfo)obj).toString().equals(toString());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

}
