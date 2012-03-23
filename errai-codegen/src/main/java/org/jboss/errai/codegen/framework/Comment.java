package org.jboss.errai.codegen.framework;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

/**
 * @author Mike Brock
 */
public class Comment implements Statement {
  private final String comment;

  public Comment(String comment) {
    this.comment = comment.trim();
  }

  @Override
  public String generate(Context context) {
    StringBuilder sb = new StringBuilder("// ");
    for (int i = 0; i < comment.length(); i++) {
      switch (comment.charAt(i)) {
        case '\r':
          continue;
        case '\n':
          sb.append('\n').append("// ");
          continue;
        default:
          sb.append(comment.charAt(i));
      }
    }
    return sb.toString();
  }

  @Override
  public MetaClass getType() {
    return MetaClassFactory.get(void.class);
  }
}
