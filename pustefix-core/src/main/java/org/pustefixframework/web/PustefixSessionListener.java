/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pustefixframework.web;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Initializes newly created sessions with unique visit id
 * and mutex/lock objects for session access synchronization.
 */
public class PustefixSessionListener implements HttpSessionListener {

    private static VisitIdGenerator visitIdGenerator = new VisitIdGenerator();

    @Override
    public void sessionCreated(HttpSessionEvent event) {

        HttpSession session = event.getSession();
        session.setAttribute(ServletUtils.SESSION_ATTR_SESSION_MUTEX, new SessionMutex());
        session.setAttribute(ServletUtils.SESSION_ATTR_LOCK, new ReentrantReadWriteLock());
        session.setAttribute(ServletUtils.SESSION_ATTR_VISIT_ID,
                visitIdGenerator.generateId(session.getId()));
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
    }


    @SuppressWarnings("serial")
    static class SessionMutex implements Serializable {}


    static class VisitIdGenerator {

        private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        private NumberFormat numberFormat;

        private int currentIncrement = 0;
        private String currentTimeStamp = "";

        VisitIdGenerator() {
            numberFormat = NumberFormat.getInstance();
            numberFormat.setMinimumIntegerDigits(3);
        }

        synchronized String generateId(String sessionId) {
            String timestamp = timeStampFormat.format(new Date());
            if (timestamp.equals(currentTimeStamp)) {
                currentIncrement++;
            } else {
                currentTimeStamp = timestamp;
                currentIncrement = 0;
            }
            String jvmRoute = "";
            int ind = sessionId.lastIndexOf(".");
            if (ind > 0) {
                jvmRoute = sessionId.substring(ind);
            }
            return currentTimeStamp + "-" + numberFormat.format(currentIncrement) + jvmRoute;
        }
    }

}
