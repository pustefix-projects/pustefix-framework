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
package org.pustefixframework.webservices.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import org.pustefixframework.webservices.BaseTestCase;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import static org.junit.Assert.*;

/**
 * 
 * @author mleidig
 *
 */
public class ConfigurationTest extends BaseTestCase {
    @Test
    public void serialization() throws Exception {
        FileResource file=ResourceUtil.getFileResourceFromDocroot("conf"+"/"+"webservice.conf.xml");
        Configuration conf=ConfigurationReader.read(file);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        ConfigurationReader.serialize(conf,out);
        ByteArrayInputStream in=new ByteArrayInputStream(out.toByteArray());
        Configuration refConf=ConfigurationReader.deserialize(in);
        assertEquals(conf,refConf);
    }
    
}
