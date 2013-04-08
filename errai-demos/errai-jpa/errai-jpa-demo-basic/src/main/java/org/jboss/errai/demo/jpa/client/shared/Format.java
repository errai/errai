package org.jboss.errai.demo.jpa.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Enum whose values are used for the "format" attribute of the testing entity
 * type "Album."
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public enum Format {
  SINGLE, SP, EP, LP, DOUBLE_EP, DOUBLE_LP
}
