/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package javax.interceptor;

import java.lang.reflect.Method;

/**
 * The InvocationContext object provides the metadata that is required for
 * AroundInvoke interceptor methods.
 * 
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 85923 $
 */
public interface InvocationContext
{
   public Object getTarget();

   public Method getMethod();

   public Object[] getParameters();

   public void setParameters(Object[] params);

   /**
    * Returns the context data associated with this invocation or lifecycle callback. If there is no context data, an empty Map object will be returned.
    */
   public java.util.Map<String, Object> getContextData();

   /**
    * Returns the timer associated with an @AroundTimeout method.
    * 
    * @since 3.1
    */
   Object getTimer();
   
   public Object proceed() throws Exception;
}