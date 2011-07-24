package org.reflections.maven.plugin;

import com.google.common.collect.Sets;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;
import org.jfrog.jade.plugins.common.injectable.MvnInjectableMojoSupport;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoPhase;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.*;
import org.reflections.serializers.Serializer;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** maven plugin for Reflections
 * <p> use it by configuring the pom with:
 * <pre>
 * &#60;build>
 *       &#60;plugins>
 *           &#60;plugin>
 *               &#60;groupId>org.reflections&#60;/groupId>
 *               &#60;artifactId>reflections-maven&#60;/artifactId>
 *               &#60;version>0.9.5 or whatever the version might be&#60;/version>
 *               &#60;executions>
 *                   &#60;execution>
 *                       &#60;goals>
 *                           &#60;goal>reflections&#60;/goal>
 *                       &#60;/goals>
 *                       &#60;phase>process-classes&#60;/phase>
 *                   &#60;/execution>
 *               &#60;/executions>
 *               &#60;configuration>
 *                  <... optional configuration here>
 *               &#60;/configuration>
 *           &#60;/plugin>
 *       &#60;/plugins>
 *   &#60;/build>
 * </pre>
 * <ul>configurations:
 * <li>{@link org.reflections.maven.plugin.ReflectionsMojo#scanners} - a comma separated list of scanner classes,
 * defaults to "org.reflections.scanners.TypeAnnotationsScanner, org.reflections.scanners.SubTypesScanner"
 * <li>{@link org.reflections.maven.plugin.ReflectionsMojo#includeExclude} - a comma separated list of include exclude filters,
 * to be used with {@link org.reflections.util.FilterBuilder} to filter the inputs and the results of all scanners,
 * defaults to "-java., -javax., -sun., -com.sun."
 * <li>{@link org.reflections.maven.plugin.ReflectionsMojo#destinations} - a comma separated list of destinations to save metadata to,
 * defaults to ${project.build.outputDirectory}/META-INF/reflections/${project.artifactId}-reflections.xml
 * <li>{@link org.reflections.maven.plugin.ReflectionsMojo#serializer} - fully qualified name of the serializer to be used for saving,
 * defaults to {@link org.reflections.serializers.XmlSerializer}
 * <li>{@link org.reflections.maven.plugin.ReflectionsMojo#parallel} - indicates whether to use parallel scanning of classes, using j.u.c FixedThreadPool,
 * defaults to false
 * */
@MojoGoal("reflections")
@MojoPhase("process-classes")
public class ReflectionsMojo extends MvnInjectableMojoSupport {

    @MojoParameter(description = "a comma separated list of scanner classes")
    private String scanners;

    @MojoParameter(description = "a comma separated list of include exclude filters, to be used with {@link org.reflections.util.FilterBuilder}" +
            " to filter the inputs and the results of all scanners"
            , defaultValue = "-java., -javax., -sun., -com.sun.")
    private String includeExclude;

    @MojoParameter(description = "a comma separated list of destinations to save metadata to." +
            "<p>defaults to ${project.build.outputDirectory}/META-INF/reflections/${project.artifactId}-reflections.xml"
            , defaultValue = "${project.build.outputDirectory}/META-INF/reflections/${project.artifactId}-reflections.xml")
    private String destinations;

    @MojoParameter(description = "fully qualified name of the serializer to be used for saving. defaults to {@link org.reflections.serializers.XmlSerializer}")
    private String serializer;

    @MojoParameter(description = "indicates whether to use parallel scanning of classes, using j.u.c FixedThreadPool"
            , defaultValue = "false")
    private Boolean parallel;

    public void execute() throws MojoExecutionException, MojoFailureException {
        //
        if (StringUtils.isEmpty(destinations)) {
            getLog().error("Reflections plugin is skipping because it should have been configured with parse non empty destinations parameter");
            return;
        }

        String outputDirectory = getProject().getBuild().getOutputDirectory();
        if (!new File(outputDirectory).exists()) {
            getLog().warn(String.format("Reflections plugin is skipping because %s was not found", outputDirectory));
            return;
        }

        //

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .setUrls(Arrays.asList(parseOutputDirUrl()))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());

        FilterBuilder filter = FilterBuilder.parse(includeExclude);

        configurationBuilder.filterInputsBy(filter);

        Serializer serializerInstance = null;
        if (serializer != null && serializer.length() != 0) {
            try {
                serializerInstance = (Serializer) Class.forName(serializer).newInstance();
                configurationBuilder.setSerializer(serializerInstance);
            } catch (Exception ex) {
                throw new ReflectionsException("could not create serializer instance", ex);
            }
        }

        final Set<Scanner> scannerInstances;
        if (scanners != null && scanners.length() != 0) {
            scannerInstances = parseScanners(filter);
        } else {
            scannerInstances = Sets.<Scanner>newHashSet(new SubTypesScanner(), new TypeAnnotationsScanner());
        }

        if (serializerInstance != null) {
            scannerInstances.add(new TypesScanner());
            scannerInstances.add(new TypeElementsScanner());
            getLog().info("added type scanners");
        }

        configurationBuilder.setScanners(scannerInstances.toArray(new Scanner[]{}));

        if (parallel != null && parallel.equals(Boolean.TRUE)) {
            configurationBuilder.useParallelExecutor();
        }

        Reflections reflections = new Reflections(configurationBuilder);

        for (String destination : parseDestinations()) {
            reflections.save(destination.trim());
        }
    }

    private Set<Scanner> parseScanners(FilterBuilder filter) throws MojoExecutionException {
        Set<Scanner> scannersSet = new HashSet<Scanner>(0);

        if (StringUtils.isNotEmpty(scanners)) {
            String[] scannerClasses = scanners.split(",");
            for (String scannerClass : scannerClasses) {
                String trimmed = scannerClass.trim();
                try {
                    Scanner scanner = (Scanner) Class.forName(scannerClass).newInstance();
                    scanner.filterResultsBy(filter);
                    scannersSet.add(scanner);
                } catch (Exception e) {
                    throw new MojoExecutionException(String.format("could not find scanner %s [%s]",trimmed,scannerClass), e);
                }
            }
        }

        return scannersSet;
    }

    private String[] parseDestinations() {
        return destinations.split(",");
    }

    private URL parseOutputDirUrl() throws MojoExecutionException {
        try {
            File outputDirectoryFile = new File(getProject().getBuild().getOutputDirectory() + '/');
            return outputDirectoryFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
