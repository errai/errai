package org.jboss.errai.codegen.framework.literal;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.util.Stmt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class MapValue extends LiteralValue<Map<Object, Object>> {
  public MapValue(Map<Object, Object> value) {
    super(value);
  }

  @Override
  public String getCanonicalString(Context context) {
    BlockBuilder<AnonymousClassStructureBuilder> initBlock
            = ObjectBuilder.newInstanceOf(HashMap.class, context).extend().initialize();

    for (Map.Entry<Object, Object> v : getValue().entrySet()) {
      initBlock.append(Stmt.loadVariable("this").invoke("put", LiteralFactory.getLiteral(v.getKey()),
              LiteralFactory.getLiteral(v.getValue())));
    }

    return initBlock.finish().finish().toJavaString();
  }
}
