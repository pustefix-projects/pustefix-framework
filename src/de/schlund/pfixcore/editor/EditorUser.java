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
*
*/

package de.schlund.pfixcore.editor;

import de.schlund.pfixxml.*;
import org.apache.log4j.*;
import java.util.*;
import org.w3c.dom.*;

/**
 * EditorUser.java
 *
 *
 */

public class EditorUser {
    private String  id;
    private String  phone;
    private String  name;
    private String  sect;
    private String  pwd;
    private String  group;
    
    private static final String ADMINGRP = "wheel";
    
    protected EditorUser(String id) {
        this.id = id;
    }

    public String toString() {
        return getId() + ": " + getName();
    }

    public void insertStatus(ResultDocument resdoc, Element root) {
        Element user = resdoc.createSubNode(root, "user");
        user.setAttribute("id", getId());
        user.setAttribute("name", getName());
        user.setAttribute("group", getGroup());
        user.setAttribute("phone", getPhone());
        user.setAttribute("sect", getSect());
    }
        
    
    public boolean equals(Object o) {
        if (o instanceof EditorUser && ((EditorUser) o).getId().equals(getId())) {
            return true;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return getId().hashCode();
    }
    
    public boolean isAdmin() {
        if ((group != null) && group.equals(ADMINGRP)) {
            return true;
        } else {
            return false;
        }
    }

    public String getId() {return id;}
    public void   setId(String  v) {this.id = v;}
    
    public String getPhone() {return phone;}
    public void   setPhone(String  v) {this.phone = v;}

    public String getName() {return name;}
    public void   setName(String  v) {this.name = v;}
    
    public String getSect() {return sect;}
    public void   setSect(String  v) {this.sect = v;}
    
    public String getPwd() {return pwd;}
    public void   setPwd(String  v) {this.pwd = v;}

    public String getGroup() {return group;}
    public void   setGroup(String  v) {this.group = v;}
    
}
