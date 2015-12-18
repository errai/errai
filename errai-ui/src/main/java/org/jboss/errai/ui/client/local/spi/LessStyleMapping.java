package org.jboss.errai.ui.client.local.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of this class get created by the {@code LessStyleGenerator} and will create a mapping
 * from the obfuscated selector names to the original ones.
 *
 * @see org.jboss.errai.ui.rebind.less.LessStyleGenerator
 * @author edewit@redhat.com
 */
public abstract class LessStyleMapping {

  protected Map<String, String> styleNameMapping = new HashMap<String, String>();

  public String get(String styleName) {
    return styleNameMapping.get(styleName);
  }
}
