package org.jboss.errai.codegen;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;

/**
 * @author Mike Brock
 */
public class Comment implements Statement {
  private final String comment;

  public Comment(final String comment) {
    this.comment = comment.trim();
  }

  @Override
  public String generate(final Context context) {
    final StringBuilder sb = new StringBuilder("// ");
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
