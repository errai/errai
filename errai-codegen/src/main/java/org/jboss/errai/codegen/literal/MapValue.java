package org.jboss.errai.codegen.literal;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Stmt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class MapValue extends LiteralValue<Map<Object, Object>> {
  public MapValue(final Map<Object, Object> value) {
    super(value);
  }

  @Override
  public String getCanonicalString(final Context context) {
    final BlockBuilder<AnonymousClassStructureBuilder> initBlock
            = ObjectBuilder.newInstanceOf(HashMap.class, context).extend().initialize();

    for (final Map.Entry<Object, Object> v : getValue().entrySet()) {
      initBlock.append(Stmt.loadVariable("this").invoke("put", LiteralFactory.getLiteral(v.getKey()),
              LiteralFactory.getLiteral(v.getValue())));
    }

    return initBlock.finish().finish().toJavaString();
  }
}
