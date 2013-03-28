/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.servlet.http;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/**
 * This interface should be implemented by Servlets which would like to handle
 * asynchronous IO, receiving events when data is available for reading, and
 * being able to output data without the need for being invoked by the container.
 * Note: When this interface is implemented, the service method of the Servlet will
 * never be called, and will be replaced with a begin event.
 */
public interface HttpEventServlet extends Servlet 
{

    /**
     * Process the given IO event.
     * 
     * @param event The event that will be processed
     * @throws IOException
     * @throws ServletException
     */
    public void event(HttpEvent event)
        throws IOException, ServletException;

}