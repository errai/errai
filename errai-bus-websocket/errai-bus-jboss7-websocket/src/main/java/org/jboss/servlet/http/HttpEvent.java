/*
 * Copyright (C) 2008 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.servlet.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The HttpEvent interface, which indicates the type of the event that is
 * being processed, as well as provides useful callbacks and utility objects.
 */
public interface HttpEvent {

    /**
     * Enumeration describing the major events that the container can invoke 
     * the EventHttpServlet event() method with:
     * <ul>
     * <li>BEGIN - will be called at the beginning 
     *  of the processing of the connection. It can be used to initialize any relevant 
     *  fields using the request and response objects. Between the end of the processing 
     *  of this event, and the beginning of the processing of the end or error events,
     *  it is possible to use the response object to write data on the open connection.
     *  Note that the response object and dependent OutputStream and Writer are  
     *  not synchronized, so when they are accessed by multiple threads adequate
     *  synchronization is needed. After processing the initial event, the request 
     *  is considered to be committed.</li>
     * <li>EOF - The end of file of the input has been reached, and no further data is
     *  available. This event is sent because it can be difficult to detect otherwise.
     *  Following the processing of this event and the processing of any subsequent
     *  event, the event will be suspended.</li>
     * <li>END - End may be called to end the processing of the request. Fields that have
     *  been initialized in the begin method should be reset. After this event has
     *  been processed, the request and response objects, as well as all their dependent
     *  objects will be recycled and used to process other requests. In particular,
     *  this event will be called if the HTTP session associated with the connection
     *  times out, if the web application is reloaded, if the server is shutdown, or
     *  if the connection was closed asynchronously.</li>
     * <li>ERROR - Error will be called by the container in the case where an IO exception
     *  or a similar unrecoverable error occurs on the connection. Fields that have
     *  been initialized in the begin method should be reset. After this event has
     *  been processed, the request and response objects, as well as all their dependent
     *  objects will be recycled and used to process other requests.</li>
     * <li>EVENT - Event will be called by the container after the resume() method is called,
     *  during which any operations can be performed, including closing the connection
     *  using the close() method.</li>
     * <li>READ - This indicates that input data is available, and that at least one 
     *  read can be made without blocking. The available and ready methods of the InputStream or
     *  Reader may be used to determine if there is a risk of blocking: the Servlet
     *  must continue reading while data is reported available. When encountering a read error, 
     *  the Servlet should report it by propagating the exception properly. Throwing 
     *  an exception will cause the error event to be invoked, and the connection 
     *  will be closed. 
     *  Alternately, it is also possible to catch any exception, perform clean up
     *  on any data structure the Servlet may be using, and using the close method
     *  of the event. It is not allowed to attempt reading data from the request 
     *  object outside of the processing of this event, unless the suspend() method
     *  has been used.</li>
     * <li>TIMEOUT - the connection timed out, but the connection will not be closed unless 
     *  the servlet uses the close method of the event</li>
     * <li>WRITE - Write is sent if the Servlet is using the ready method. This means that 
     *  the connection is ready to receive data to be written out. This event will never
     *  be received if the Servlet is not using the ready() method, or if the ready() 
     *  method always returns true.</li>
     * </ul>
     */
    public enum EventType { BEGIN, END, ERROR, EVENT, READ, EOF, TIMEOUT, WRITE }
    
    
    /**
     * Returns the HttpServletRequest.
     * 
     * @return HttpServletRequest
     */
    public HttpServletRequest getHttpServletRequest();
    
    /**
     * Returns the HttpServletResponse.
     * 
     * @return HttpServletResponse
     */
    public HttpServletResponse getHttpServletResponse();
    
    /**
     * Returns the event type.
     * 
     * @return EventType
     * @see #EventType
     */
    public EventType getType();
    
    /**
     * Ends the request, which marks the end of the event stream. This will send 
     * back to the client a notice that the server has no more data to send 
     * as part of this request. An END event will be sent to the Servlet.
     * 
     * @throws IOException if an IO exception occurs
     */
    public void close() throws IOException;

    /**
     * This method sets the timeout in milliseconds of idle time on the connection.
     * The timeout is reset every time data is received from the connection. If a timeout occurs, the 
     * Servlet will receive an TIMEOUT event which will not result in automatically closing
     * the event (the event may be closed using the close() method).
     * 
     * @param timeout The timeout in milliseconds for this connection, must be a positive value, larger than 0
     */
    public void setTimeout(int timeout);

    /**
     * Returns true when data may be read from the connection (the flag becomes false if no data
     * is available to read). When the flag becomes false, the Servlet can attempt to read additional
     * data, but it will block until data is available. This method is equivalent to 
     * Reader.ready() and (InputStream.available() > 0).
     * 
     * @return boolean true if data can be read without blocking
     */
    public boolean isReadReady();

    /**
     * Returns true when data may be written to the connection (the flag becomes false 
     * when the client is unable to accept data fast enough). When the flag becomes false, 
     * the Servlet must stop writing data. If there's an attempt to flush additional data 
     * to the client and data still cannot be written immediately, an IOException will be 
     * thrown. If calling this method returns false, it will also 
     * request notification when the connection becomes available for writing again, and the  
     * Servlet will receive a write event.
     * <br>
     * Note: If the Servlet is not using isWriteReady, and is writing its output inside the
     * container threads (inside the event() method processing, for example), using this method
     * is not mandatory, and writes will block until all bytes are written.
     * 
     * @return boolean true if data can be written without blocking
     */
    public boolean isWriteReady();

    /**
     * Suspend processing of the connection until the configured timeout occurs, 
     * or resume() is called. In practice, this means the servlet will no longer 
     * receive read events. Reading should always be performed synchronously in 
     * the Tomcat threads unless the connection has been suspended.
     */
    public void suspend();

    /**
     * Resume will cause the Servlet container to send a generic event 
     * to the Servlet, where the request can be processed synchronously 
     * (for example, it is possible to use this to complete the request after 
     * some asynchronous processing is done). This also resumes read events 
     * if they have been disabled using suspend. It is then possible to call suspend 
     * again later. It is also possible to call resume without calling suspend before.
     * This method must be called asynchronously.
     */
    public void resume();

}
