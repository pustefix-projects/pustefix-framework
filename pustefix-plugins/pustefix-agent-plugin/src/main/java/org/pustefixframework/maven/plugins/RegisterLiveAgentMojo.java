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
package org.pustefixframework.maven.plugins;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.sun.tools.attach.VirtualMachine;

/**
 * Dynamically registers Pustefix LiveAgent using Sun's Attach API.
 *
 * @author mleidig@schlund.de
 *
 * @goal register
 * @phase package
 */
public class RegisterLiveAgentMojo extends AbstractMojo {
    
    /** 
      * @parameter expression="${plugin.artifacts}" 
      * @required 
      */ 
    private List<Artifact> pluginArtifacts; 
    
    public void execute() throws MojoExecutionException {
        
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        int ind = vmName.indexOf('@');
        String pid = vmName.substring(0, ind);
        File agentJar = null;
        for(Artifact a: pluginArtifacts) {
            if(a.getArtifactId().equals("pustefix-agent")) {
                agentJar = a.getFile();
            }
        }
        if(agentJar != null) {
            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                vm.loadAgent(agentJar.getAbsolutePath(), "");
                vm.detach();
            } catch(Exception x) {
                throw new MojoExecutionException("Error while registering Pustefix LiveAgent", x);
            }
        }
      
    }
    
}