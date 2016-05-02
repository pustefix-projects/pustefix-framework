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

package de.schlund.pfixcore.lucefix;

import org.springframework.beans.factory.DisposableBean;

import de.schlund.pfixxml.XMLException;

/**
 * @author schuppi
 * @date Jun 24, 2005
 */
public class PfixEngine implements DisposableBean {

    private int queueIdle;
    private PfixQueueManager queueManager;

    public void setQueueIdle(int queueIdle) {
        this.queueIdle = queueIdle;
    }

    public void init() throws XMLException {
        queueManager = PfixQueueManager.getInstance(queueIdle);
        queueManager.setPriority(Thread.MIN_PRIORITY);
        queueManager.start();
    }
    
    public void destroy() throws Exception {
        queueManager.interrupt();
    }
    
}
