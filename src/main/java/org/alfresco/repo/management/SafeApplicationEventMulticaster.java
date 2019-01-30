/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.management;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;

/**
 *
 * This application event multicaster queues events until the application is started
 *
 * @see org.springframework.context.event.SimpleApplicationEventMulticaster
 */
public class SafeApplicationEventMulticaster extends SimpleApplicationEventMulticaster implements ApplicationContextAware
{
    private ApplicationContext appContext;

    /** Has the application started? */
    private boolean isApplicationStarted;

    /** The queued events that can't be broadcast until the application is started. */
    private List<ApplicationEvent> queuedEvents = new LinkedList<>();

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType)
    {
        if (event instanceof ContextRefreshedEvent && event.getSource() == this.appContext)
        {
            this.isApplicationStarted = true;
            for (ApplicationEvent queuedEvent : this.queuedEvents)
            {
                super.multicastEvent(queuedEvent, eventType);
            }
            this.queuedEvents.clear();
            super.multicastEvent(event, eventType);
        }
        else if (event instanceof ContextClosedEvent && event.getSource() == this.appContext)
        {
            this.isApplicationStarted = false;
            super.multicastEvent(event, eventType);
        }
        else if (this.isApplicationStarted)
        {
            super.multicastEvent(event, eventType);
        }
        else
        {
            this.queuedEvents.add(event);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.appContext = applicationContext;
    }
}