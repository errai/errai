/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server.gwt.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.google.gwt.thirdparty.guava.common.collect.HashMultimap;
import com.google.gwt.thirdparty.guava.common.collect.Multimap;

/**
 * Copies XML file to another location. Can filter out tags with provided names
 * and attributes. Can insert tags into a parent with a given tag name.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class SimpleTranslator {

  /**
   * An attribute key-value pair.
   * 
   * @author Max Barkley
   */
  public static class AttributeEntry {
    private final String key;
    private final String value;

    public AttributeEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * A simple xml tag with a local name and attribute key-value pairs.
   * 
   * @author Max Barkley
   */
  public static class Tag extends ArrayList<AttributeEntry> {
    private static final long serialVersionUID = 1L;
    private final String name;

    public Tag(String name) {
      this.name = name;
    }

    public Tag(String name, Collection<AttributeEntry> entries) {
      super(entries);
      this.name = name;
    }

    public Tag(String name, AttributeEntry... entries) {
      this(name, Arrays.asList(entries));
    }

    public String getName() {
      return name;
    }
  }

  private static class XMLMatcher {
    private Tag tag;

    public XMLMatcher(Tag tag) {
      this.tag = tag;
    }

    public boolean matches(StartElement element) {
      if (!element.getName().getLocalPart().equals(tag.getName())) {
        return false;
      }

      boolean matches = true;
      for (final AttributeEntry entry : tag) {
        Attribute attr = element.getAttributeByName(QName.valueOf(entry.getKey()));
        if (attr == null || !attr.getValue().equals(entry.getValue())) {
          matches = false;
          break;
        }
      }

      return matches;
    }

  }

  private Multimap<String, XMLMatcher> toRemove = HashMultimap.create();
  private Multimap<String, Tag> toAdd = HashMultimap.create();

  /**
   * Copy an xml file, removing any xml tags matching those provided with
   * {@link SimpleTranslator#addFilter(Tag)} and adding any tags to provided
   * with {@link SimpleTranslator#addNewTag(String, Tag)}.
   * 
   * @param in
   *          A stream to an xml file.
   * @param out
   *          A stream to an empty file.
   */
  public void translate(InputStream in, OutputStream out) throws XMLStreamException {
    final XMLInputFactory inFactory = XMLInputFactory.newInstance();
    final XMLOutputFactory outFactory = XMLOutputFactory.newInstance();

    final XMLEventReader reader = inFactory.createXMLEventReader(in);
    final XMLEventWriter writer = outFactory.createXMLEventWriter(out);

    try {
      EventLoop: while (reader.hasNext()) {
        XMLEvent event = reader.nextEvent();

        if (event.isStartElement()) {
          StartElement start = event.asStartElement();
          if (isRemovable(start)) {
            while (reader.hasNext()) {
              XMLEvent next = reader.nextEvent();
              if (next.isEndElement() && next.asEndElement().getName().equals(start.getName()))
                continue EventLoop;
            }
            // If this is reached, we finished the document before the tag ended
            throw new RuntimeException(String.format("End of file was reached before %s closing tag was found.",
                    start.getName()));
          }
          else if (toAdd.containsKey(start.getName().getLocalPart())
                  && !toAdd.get(start.getName().getLocalPart()).isEmpty()) {
            writer.add(event);
            final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            for (final Tag tag : toAdd.get(start.getName().getLocalPart())) {
              List<Attribute> attrs = new ArrayList<Attribute>();
              for (final AttributeEntry entry : tag) {
                attrs.add(eventFactory.createAttribute(entry.getKey(), entry.getValue()));
              }
              StartElement newStart = eventFactory.createStartElement(QName.valueOf(tag.getName()), attrs.iterator(),
                      null);
              EndElement newEnd = eventFactory.createEndElement(newStart.getName(), null);

              writer.add(newStart);
              writer.add(newEnd);
            }
          }
          else {
            writer.add(event);
          }
        }
        else {
          writer.add(event);
        }
      }
    } finally {
      writer.close();
      reader.close();
    }

  }

  private boolean isRemovable(StartElement start) {
    Collection<XMLMatcher> matchers = toRemove.get(start.getName().getLocalPart());
    if (matchers == null)
      return false;

    for (final XMLMatcher matcher : matchers) {
      if (matcher.matches(start))
        return true;
    }

    return false;
  }

  /**
   * Add a description of a tag to be filtered on a subsequent call to
   * {@link SimpleTranslator#translate(InputStream, OutputStream)}. {@code tagA}
   * filters out {@code tagB} if both tags have the same name (ignoring
   * namespaces) and the set of attribute key-value pairs in {@code tagB} is a
   * superset of those in {@code tagA}.
   * 
   * @param tag
   *          The tag description to be filtered.
   */
  public void addFilter(Tag tag) {
    toRemove.put(tag.getName(), new XMLMatcher(tag));
  }

  /**
   * Add a description for a new tag to be added on a subsequent call to
   * {@link SimpleTranslator#translate(InputStream, OutputStream)}. This tag
   * will be added a child of any tag with given parent name (ignoring
   * namespaces).
   * 
   * @param parentName
   *          The name (ignoring namespaces) of a parent element, under which to
   *          insert a new tag.
   * @param tag
   *          The new tag to be inserted.
   */
  public void addNewTag(String parentName, Tag tag) {
    toAdd.put(parentName, tag);
  }

}
