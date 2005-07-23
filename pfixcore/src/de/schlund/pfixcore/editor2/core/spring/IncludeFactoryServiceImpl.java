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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.spring.internal.IncludeFileImpl;
import de.schlund.pfixcore.editor2.core.spring.internal.IncludePartThemeVariantImpl;
import de.schlund.pfixxml.IncludeDocument;
import de.schlund.pfixxml.IncludeDocumentFactory;
import de.schlund.pfixxml.PathFactory;

/**
 * Implementation of IncludeFactoryService using Pustefix IncludeDocumentFactory
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludeFactoryServiceImpl implements IncludeFactoryService {
    private Hashtable cache;

    private ThemeFactoryService themefactory;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    private PathResolverService pathresolver;

    private FileSystemService filesystem;

    private ConfigurationService configuration;

    private SecurityManagerService securitymanager;

    public void setProjectFactoryService(ProjectFactoryService projectfactory) {
        this.projectfactory = projectfactory;
    }

    public void setThemeFactoryService(ThemeFactoryService themefactory) {
        this.themefactory = themefactory;
    }

    public void setVariantFactoryService(VariantFactoryService variantfactory) {
        this.variantfactory = variantfactory;
    }

    public void setIncludeFactoryService(IncludeFactoryService includefactory) {
        this.includefactory = includefactory;
    }

    public void setImageFactoryService(ImageFactoryService imagefactory) {
        this.imagefactory = imagefactory;
    }

    public void setPathResolverService(PathResolverService pathresolver) {
        this.pathresolver = pathresolver;
    }

    public void setFileSystemService(FileSystemService filesystem) {
        this.filesystem = filesystem;
    }

    public void setConfigurationService(ConfigurationService configuration) {
        this.configuration = configuration;
    }

    public void setSecurityManagerService(SecurityManagerService securitymanager) {
        this.securitymanager = securitymanager;
    }

    public IncludeFactoryServiceImpl() {
        this.cache = new Hashtable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService#createIncludeFile(java.lang.String)
     */
    public IncludeFile getIncludeFile(String filename)
            throws EditorParsingException {
        if (cache.containsKey(filename)) {
            return (IncludeFile) cache.get(filename);
        }

        synchronized (cache) {
            if (!cache.containsKey(filename)) {
                IncludeDocument incdoc = this
                        .getIncludeDocumentForFile(filename);
                IncludeFile incfile = new IncludeFileImpl(themefactory,
                        includefactory, filename, incdoc);
                cache.put(filename, incfile);
            }
        }
        return (IncludeFile) cache.get(filename);
    }

    public void refreshIncludeFile(String filename)
            throws EditorParsingException {
        synchronized (this.cache) {

            if (this.cache.containsKey(filename)) {
                IncludeFileImpl incFile = (IncludeFileImpl) this.cache
                        .get(filename);
                incFile.setPfixIncludeDocument(this
                        .getIncludeDocumentForFile(filename));
            } else {
                return;
            }
        }
    }

    private IncludeDocument getIncludeDocumentForFile(String filename)
            throws EditorParsingException {
        IncludeDocument incdoc;
        try {
            incdoc = IncludeDocumentFactory.getInstance().getIncludeDocument(
                    PathFactory.getInstance().createPath(filename), true);
        } catch (FileNotFoundException e) {
            incdoc = null;
        } catch (SAXException e) {
            String err = "Error on parsing include file " + filename + "!";
            Logger.getLogger(this.getClass()).error(err, e);
            throw new EditorParsingException(err, e);
        } catch (IOException e) {
            String err = "Error on parsing include file " + filename + "!";
            Logger.getLogger(this.getClass()).error(err, e);
            throw new EditorParsingException(err, e);
        } catch (TransformerException e) {
            String err = "Error on parsing include file " + filename + "!";
            Logger.getLogger(this.getClass()).error(err, e);
            throw new EditorParsingException(err, e);
        }
        return incdoc;
    }

    public IncludePartThemeVariant getIncludePartThemeVariant(Theme theme,
            IncludePart part) {
        return new IncludePartThemeVariantImpl(this.projectfactory,
                this.variantfactory, this.includefactory, this.themefactory,
                this.imagefactory, this.filesystem, this.pathresolver,
                this.configuration, this.securitymanager, theme, part);
    }

}
