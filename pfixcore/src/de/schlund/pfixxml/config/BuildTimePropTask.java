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

package de.schlund.pfixxml.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class BuildTimePropTask extends Task {
    private File file;

    private String mode;

    private String machine;

    private String fqdn;

    private String uid;

    public void setFile(File file) {
        this.file = file;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        try {
            Properties props = new Properties();
            if (file.exists()) {
                props.load(new FileInputStream(file));
            } else {
                file.createNewFile();
            }

            props.setProperty("mode", mode);
            props.setProperty("machine", machine);
            props.setProperty("fqdn", fqdn);
            props.setProperty("uid", uid);
            
            Hashtable<String, Object> antProps = this.getProject().getProperties();
            for (String key : antProps.keySet()) {
                props.setProperty("__antprop_" + key, (String)antProps.get(key));
            }
            
            props.store(new FileOutputStream(file),
                    "Properties used at buildtime");
        } catch (IOException e) {
            throw new BuildException("Could not access property file \"" + file
                    + "\"!", e);
        }
    }

}
