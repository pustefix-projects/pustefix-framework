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

package de.schlund.pfixxml.testenv;

import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


/**
 * This factory class is responsible for creating objects
 * of type {@link RecordManager}.
 * <br/>
 * Patterns: Singleton, Factory 
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class RecordManagerFactory {

    //~ Instance/static variables ..................................................................

    private static RecordManagerFactory instance = new RecordManagerFactory();

    /** Store the created RecordManager objects here */
    private Hashtable hash = new Hashtable();

    //~ Constructors ...............................................................................

    private RecordManagerFactory() {
    }

    //~ Methods ....................................................................................

    /**
     * Get the one and only instance of this singleton
     * @return the instance
     */
    public static RecordManagerFactory getInstance() {
        return instance;
    }

    /**
     * Create a RecordManager.
     * @param the path to the dependency configuration file. For one of 
     * these files, one RecordManager will be created.
     */
    public RecordManager createRecordManager(String depxml) throws ParserConfigurationException, SAXException, IOException  {
        RecordManager ret = null;
        if (hash.containsKey(depxml)) {
            ret = (RecordManager) hash.get(depxml);
        } else {
            ret = new RecordManager(depxml);
            hash.put(depxml, ret);
        }
        return ret;
    }
}