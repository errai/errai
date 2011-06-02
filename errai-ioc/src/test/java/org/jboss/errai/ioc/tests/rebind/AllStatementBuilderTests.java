package org.jboss.errai.ioc.tests.rebind;

import org.jboss.errai.ioc.tests.rebind.literals.LiteralTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AnnotationEncoderTest.class, ClassStructureBuilderTest.class, ContextBuilderTest.class,
        IfBlockBuilderTest.class, InvocationBuilderTest.class, LiteralTest.class, LoopBuilderTest.class,
        StatementBuilderTest.class })
public class AllStatementBuilderTests {

}