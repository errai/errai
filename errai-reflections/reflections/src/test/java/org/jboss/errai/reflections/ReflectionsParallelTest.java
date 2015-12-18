package org.jboss.errai.reflections;

import com.google.common.base.Predicate;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.jboss.errai.reflections.scanners.*;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.util.ClasspathHelper;
import org.jboss.errai.reflections.util.FilterBuilder;

import static java.util.Arrays.asList;

/** */
@Ignore
public class ReflectionsParallelTest extends ReflectionsTest {

    @BeforeClass
    public static void init() {
        Predicate<String> filter = new FilterBuilder().include("org.jboss.errai.reflections.TestModel\\$.*");

        reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(asList(ClasspathHelper.forClass(TestModel.class)))
                .filterInputsBy(filter)
                .setScanners(
                        new SubTypesScanner().filterResultsBy(filter),
                        new TypeAnnotationsScanner().filterResultsBy(filter),
                        new FieldAnnotationsScanner().filterResultsBy(filter),
                        new MethodAnnotationsScanner().filterResultsBy(filter),
                        new ConvertersScanner().filterResultsBy(filter))
                .useParallelExecutor());
    }

    @Test
    public void testAll() {
        super.testAll();
    }
}
