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

package de.schlund.pfixcore.editor2.core.spring;

import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.exception.EditorParsingException;

import de.schlund.pfixxml.targets.AuxDependency;

/**
 * Service providing methods to retrieve IncludeParts
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface IncludeFactoryService {
    /**
     * Creates an IncludeFile object using the specified filename
     * 
     * @param filename
     *            Relative path to the file
     * @return IncludeFile object corresponding to the filename
     * @throws EditorParsingException
     *             If an error occurs during parsing of the IncludeFile
     */
    IncludeFile getIncludeFile(String filename) throws EditorParsingException;

    /**
     * Create an IncludePartThemeVariant object using the information retrieved
     * from the supplied AuxDependency.
     * 
     * @param auxdep
     *            AuxDependency of type DependencyType.TEXT
     * @return IncludePartThemeVariant for auxdep
     * @throws EditorParsingException
     *             if a parsing error occurs
     * @throws RuntimeException
     *             if auxdep is not of type
     *             {@link de.schlund.pfixxml.targets.DependencyType.TEXT}
     */
    IncludePartThemeVariant getIncludePartThemeVariant(AuxDependency auxdep)
            throws EditorParsingException;
}
