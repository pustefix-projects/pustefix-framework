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

package de.schlund.pfixcore.editor2.core.vo;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.pustefixframework.editor.common.dom.Project;


/**
 * Represents a person, who might use the editor.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditorUser implements Cloneable, Comparable<EditorUser> {
    private String username;

    private String cryptedPassword;

    private String fullname;

    private String sectionName;

    private String phoneNumber;

    private EditorGlobalPermissions globalPermissions;

    private LinkedHashMap<String, EditorProjectPermissions> projectsPermissions;

    /**
     * Contstructor setting all values (except username) to the empty string and
     * initializing all permissions disabled.
     * 
     * @param username
     *            Unique alpha-numeric string identifying the user
     */
    public EditorUser(String username) {
        this.username = username;
        this.cryptedPassword = "!";
        this.fullname = "";
        this.sectionName = "";
        this.phoneNumber = "";
        this.globalPermissions = new EditorGlobalPermissions();
        this.projectsPermissions = new LinkedHashMap<String, EditorProjectPermissions>();
    }

    /**
     * Return the username identifying the user.
     * 
     * @return Username for this user
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Returns the password in UNIX crypt() format.
     * 
     * @return Crypted password
     */
    public String getCryptedPassword() {
        return this.cryptedPassword;
    }

    /**
     * Sets the password
     * 
     * @param cryptedPassword
     *            Password as returned by the UNIX crypt() function
     */
    public void setCryptedPassword(String cryptedPassword) {
        this.cryptedPassword = cryptedPassword;
    }

    /**
     * Returns the full name (e.g. real name) of this user.
     * 
     * @return Full name describing this user
     */
    public String getFullname() {
        return this.fullname;
    }

    /**
     * Sets the full name.
     * 
     * @param fullname
     *            The full name to set, usually the real name of the person
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     * Returns the name of the section the person is part of.
     * 
     * @return Name of the section
     */
    public String getSectionName() {
        return this.sectionName;
    }

    /**
     * Sets the name of the section the person is part of.
     * 
     * @param sectionName
     *            Name of the section
     */
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    /**
     * Returns the phone number of the person
     * 
     * @return Phone number
     */
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    /**
     * Sets the phone number of the person
     * 
     * @param phoneNumber
     *            Phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns an EditorGlobalPermissions object specifying the global
     * permissions this user has. Please note that this method returns the
     * settings by value, not by reference.
     * 
     * @return Global permissions for this user.
     */
    public EditorGlobalPermissions getGlobalPermissions() {
        return (EditorGlobalPermissions) this.globalPermissions.clone();
    }

    /**
     * Sets the global permissions for this user.
     * 
     * @param globalPermissions
     *            The permissions to set
     */
    public void setGlobalPermissions(EditorGlobalPermissions globalPermissions) {
        this.globalPermissions = globalPermissions;
    }

    /**
     * Returns all projects this user object has ProjectPermissions objects for.
     * Note that this does not necessarily mean, that there is enabled any
     * permission for this user and the relevant project.
     * 
     * @return List of projects to be used as keys for
     *         {@link #getProjectPermissions(Project)}
     */
    public Collection<String> getProjectsWithPermissions() {
        synchronized (this.projectsPermissions) {
            return new HashSet<String>(this.projectsPermissions.keySet());
        }
    }

    /**
     * Returns an EditorProjectPermissions object specifying the permissions
     * this user has for the specified project. This method even returns an
     * object (with all permissions disabled) if there are no stored permissions
     * for the specified project. Please note that this method returns the
     * settings by value, not by reference.
     * 
     * @param project
     *            Project to get permissions for
     * @return Project permissions
     */
    public EditorProjectPermissions getProjectPermissions(String projectName) {
        synchronized (this.projectsPermissions) {
            EditorProjectPermissions permissions = this.projectsPermissions.get(projectName);
            if (permissions == null) {
                return new EditorProjectPermissions();
            }
            return (EditorProjectPermissions) permissions.clone();
        }
    }

    /**
     * Sets permissions for a specific project.
     * 
     * @param project
     *            Project to set permissions for
     * @param projectPermissions
     *            Permissions to set
     */
    public void setProjectPermissions(String projectName,
            EditorProjectPermissions projectPermissions) {
        if (projectName == null || projectPermissions == null) {
            throw new NullPointerException("Parameter may not be null!");
        }
        synchronized (this.projectsPermissions) {
            this.projectsPermissions.put(projectName, projectPermissions);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        EditorUser obj = new EditorUser(this.getUsername());
        obj.setCryptedPassword(this.getCryptedPassword());
        obj.setFullname(this.getFullname());
        obj.setGlobalPermissions(this.getGlobalPermissions());
        obj.setPhoneNumber(this.getPhoneNumber());
        synchronized (this.projectsPermissions) {
            for (String projectName : this.projectsPermissions.keySet()) {
                obj.setProjectPermissions(projectName, this
                        .getProjectPermissions(projectName));
            }
        }
        obj.setSectionName(this.getSectionName());
        return obj;
    }

    public int compareTo(EditorUser u2) {
        return this.getUsername().compareTo(u2.getUsername());
    }
}
