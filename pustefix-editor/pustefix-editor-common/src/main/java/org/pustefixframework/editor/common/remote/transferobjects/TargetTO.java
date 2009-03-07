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

package org.pustefixframework.editor.common.remote.transferobjects;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.pustefixframework.editor.common.dom.TargetType;



public class TargetTO implements Serializable {
    
    private static final long serialVersionUID = -7081358794051834368L;
    
    public String name;
    public List<String> pages = new LinkedList<String>();
    public List<String> auxDependencies = new LinkedList<String>();
    public Map<String, String> parameters = new LinkedHashMap<String, String>();
    public String parentXML;
    public String parentXSL;
    public List<String> themes = new LinkedList<String>();
    public TargetType type;
}
