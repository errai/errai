package org.jboss.errai.codegen.gwt.test;

import static org.junit.Assert.assertEquals;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.junit.Test;

/**
 * @author Mike Brock
 */
public class GWTMetaClassTests extends AbstractGWTMetaClassTest {
  private final TypeOracle mockacle;

  public GWTMetaClassTests() {
    addTestClass("foo.MyTestClass");
    mockacle = generateMockacle();
  }

  @Test
  public void confirmContractConsistency1() throws Exception {
    final String fqcn = "foo.MyTestClass";
    final JClassType myTestClass = mockacle.getType(fqcn);
    final MetaClass metaClass = GWTClass.newInstance(mockacle, myTestClass);

    final Class myTestClass1 = loadTestClass(fqcn);

    final MetaClass metaClass1 = JavaReflectionClass.newUncachedInstance(myTestClass1);

    assertEquals(metaClass.getName(), metaClass1.getName());
    assertEquals(metaClass.getFullyQualifiedName(), metaClass1.getFullyQualifiedName());
    assertEquals(metaClass.getCanonicalName(), metaClass1.getCanonicalName());
    assertEquals(metaClass.getInternalName(), metaClass1.getInternalName());
  }
}
