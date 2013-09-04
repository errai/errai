package org.jboss.errai.ui.shared.chain;

import org.w3c.dom.Element;

/**
 * @author edewit@redhat.com
 */
public interface Command {

  void execute(Element element);
}
