package org.jboss.errai.marshalling.rebind.api;

import org.jboss.errai.codegen.framework.Statement;

/**
 * This class will actually figure out how to deconstruct and object and put it back together
 * by generating mappings, which represent value bind and value read statements.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface ObjectMapper {
  Statement getMarshaller();
}
