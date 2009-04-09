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

package de.schlund.pfixcore.generator;

import de.schlund.pfixcore.workflow.Context;

/**
 * IHandler are classes which represent application logic and work
 * on supplied wrappers to get their data or set default values
 * which should appear on the html form that is used to supply the
 * data needed by the handler.</br>
 * From the application programmers point of view these classes which
 * handle all the application logic have to be written on their own.
 */

public interface IHandler {
    void    handleSubmittedData(Context context, IWrapper wrapper) throws Exception;
    void    retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception;
    boolean prerequisitesMet(Context context) throws Exception;
    boolean isActive(Context context) throws Exception;
    boolean needsData(Context context) throws Exception;
}
