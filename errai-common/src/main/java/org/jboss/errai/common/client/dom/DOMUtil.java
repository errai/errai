/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.client.dom;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jboss.errai.common.client.function.Optional;

import com.google.gwt.regexp.shared.RegExp;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class DOMUtil {
  private DOMUtil() {}

  public static Optional<Element> getFirstChildElement(final Element element) {
    for (final Node child : nodeIterable(element.getChildNodes())) {
      if (isElement(child)) {
        return Optional.ofNullable((Element) child);
      }
    }

    return Optional.empty();
  }

  public static Optional<Element> getLastChildElement(final Element element) {
    final NodeList children = element.getChildNodes();
    for (int i = children.getLength()-1; i > -1; i--) {
      if (isElement(children.item(i))) {
        return Optional.ofNullable((Element) children.item(i));
      }
    }

    return Optional.empty();
  }

  public static boolean isElement(final Node node) {
    return node.getNodeType() == Node.ELEMENT_NODE;
  }

  public static Iterable<Node> nodeIterable(final NodeList nodeList) {
    return () -> DOMUtil.nodeIterator(nodeList);
  }

  public static Iterator<Node> nodeIterator(final NodeList nodeList) {
    return new Iterator<Node>() {
      int index = 0;

      @Override
      public boolean hasNext() {
        return index < nodeList.getLength();
      }

      @Override
      public Node next() {
        if (hasNext()) {
          return nodeList.item(index++);
        }
        else {
          throw new NoSuchElementException();
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static Iterable<Element> elementIterable(final NodeList nodeList) {
    return () -> elementIterator(nodeList);
  }

  public static Iterator<Element> elementIterator(final NodeList nodeList) {
    return new Iterator<Element>() {

      int i = 0;

      @Override
      public boolean hasNext() {
        while (i < nodeList.getLength() && !isElement(nodeList.item(i))) {
          i++;
        }
        return i < nodeList.getLength();
      }

      @Override
      public Element next() {
        if (hasNext()) {
          return (Element) nodeList.item(i++);
        }
        else {
          throw new NoSuchElementException();
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static boolean removeFromParent(final Node element) {
    if (element.getParentElement() != null) {
      element.getParentElement().removeChild(element);

      return true;
    }
    else {
      return false;
    }
  }

  public static boolean removeAllChildren(final Node node) {
    final boolean hadChildren = node.getLastChild() != null;
    while (node.getLastChild() != null) {
      node.removeChild(node.getLastChild());
    }

    return hadChildren;
  }

  public static boolean removeAllElementChildren(final Node node) {
    boolean elementRemoved = false;
    for (final Element child : elementIterable(node.getChildNodes())) {
      node.removeChild(child);
      elementRemoved = true;
    }

    return elementRemoved;
  }

  public static boolean removeCSSClass(final HTMLElement element, final String className) {
    if (hasCSSClass(element, className)) {
      element.setClassName(element.getClassName().replaceAll("\\b" + className + "\\b", "").trim());

      return true;
    }
    else {
      return false;
    }
  }

  public static boolean addCSSClass(final HTMLElement element, final String className) {
    if (hasCSSClass(element, className)) {
      return false;
    }
    else {
      element.setClassName(element.getClassName().trim() + " " + className);

      return true;
    }
  }

  public static boolean hasCSSClass(final HTMLElement element, final String className) {
    final RegExp pattern = RegExp.compile("\\b" + className + "\\b");

    return pattern.test(element.getClassName());
  }
}
