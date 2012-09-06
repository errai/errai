/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;

/**
 * Visit the DOM
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class Visit
{
   public static <T> VisitContext<T> breadthFirst(Element root, Visitor<T> visitor)
   {
      return breadthFirst(new VisitContextImpl<T>(), root, visitor);
   }

   private static <T> VisitContext<T> breadthFirst(VisitContextImpl<T> context, Element root, Visitor<T> visitor)
   {
      if (root == null)
         throw new IllegalArgumentException("Root Element to visit must not be null.");

      if (context == null)
         context = new VisitContextImpl<T>();

      Element current = root;
      visitor.visit(context, current);
      current = current.getFirstChildElement();

      while (current != null && !context.isVisitComplete()) {
         breadthFirst(context, current, visitor);
         current = current.getNextSiblingElement();
      }

      return context;
   }

}
