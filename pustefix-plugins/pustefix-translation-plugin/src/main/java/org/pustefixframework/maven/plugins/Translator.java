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

import java.util.Locale;
import java.util.Properties;

/**
 * Interface implemented by Translation API clients.
 */
public interface Translator {

    /**
     * Called after creation for initializing by properties.
     * @param properties - initialization properties.
     */
    public void init(Properties properties);

    /**
     * Translates text from source locale to target locale.
     *
     * @param sourceLocale - source locale
     * @param targetLocale - target locale
     * @param text - text to be translated
     * @return translated text
     */
    public String[] translate(Locale sourceLocale, Locale targetLocale, String[] text);

}
