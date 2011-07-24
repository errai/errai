package org.reflections.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.reflections.Configuration;
import org.reflections.adapters.JavassistAdapter;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * a fluent builder for {@link org.reflections.Configuration}, to be used for constructing a {@link org.reflections.Reflections} instance
 * <p>usage:
 * <pre>
 *      new Reflections(
 *          new ConfigurationBuilder()
 *              .filterInputsBy(new FilterBuilder().include("your project's common package prefix here..."))
 *              .setUrls(ClasspathHelper.forClassLoader())
 *              .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner().filterResultsBy(myClassAnnotationsFilter)));
 * </pre>
 * <br>{@link #executorService} is used optionally used for parallel scanning. if value is null then scanning is done in a simple for loop
 * <p>defaults: accept all for {@link #inputsFilter},
 * {@link #executorService} is null,
 * {@link #serializer} is {@link org.reflections.serializers.XmlSerializer}
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public class ConfigurationBuilder implements Configuration {
    private final Set<Scanner> scanners = Sets.<Scanner>newHashSet(new TypeAnnotationsScanner(), new SubTypesScanner());
    private Set<URL> urls = Sets.newHashSet();
    private MetadataAdapter metadataAdapter = new JavassistAdapter();
    private Predicate<String> inputsFilter = Predicates.alwaysTrue();
    private Serializer serializer;
    private ExecutorService executorService;

    public ConfigurationBuilder() {
    }

    public Set<Scanner> getScanners() {
		return scanners;
	}

    /** set the scanners instances for scanning different metadata */
    public ConfigurationBuilder setScanners(final Scanner... scanners) {
        this.scanners.addAll(Arrays.asList(scanners));
        return this;
    }

    public Set<URL> getUrls() {
        return urls;
    }

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final Collection<URL> urls) {
		this.urls = Sets.newHashSet(urls);
        return this;
	}

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final URL... urls) {
		this.urls = Sets.newHashSet(urls);
        return this;
	}

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final Collection<URL>... urlss) {
        urls.clear();
        addUrls(urlss);
        return this;
    }

    /** add urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder addUrls(final Collection<URL> urls) {
        this.urls.addAll(urls);
        return this;
    }

    /** add urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder addUrls(final URL... urls) {
        this.urls.addAll(Sets.newHashSet(urls));
        return this;
    }

    /** add urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder addUrls(final Collection<URL>... urlss) {
        for (Collection<URL> urls : urlss) { addUrls(urls); }
        return this;
    }

    public MetadataAdapter getMetadataAdapter() {
        return metadataAdapter;
    }

    /** sets the metadata adapter used to fetch metadata from classes */
    public ConfigurationBuilder setMetadataAdapter(final MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
        return this;
    }

    public boolean acceptsInput(String inputFqn) {
        return inputsFilter.apply(inputFqn);
    }

    /** sets the input filter for all resources to be scanned
     * <p> supply a {@link com.google.common.base.Predicate} or use the {@link FilterBuilder}*/
    public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
        this.inputsFilter = inputsFilter;
        return this;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /** sets the executor service used for scanning. */
    public ConfigurationBuilder setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /** sets the executor service used for scanning to ThreadPoolExecutor with core size as {@link java.lang.Runtime#availableProcessors()}
     * <p>default is ThreadPoolExecutor with a single core */
    public ConfigurationBuilder useParallelExecutor() {
        return useParallelExecutor(Runtime.getRuntime().availableProcessors());
    }

    /** sets the executor service used for scanning to ThreadPoolExecutor with core size as the given availableProcessors parameter
     * <p>default is ThreadPoolExecutor with a single core */
    public ConfigurationBuilder useParallelExecutor(final int availableProcessors) {
        setExecutorService(Executors.newFixedThreadPool(availableProcessors));
        return this;
    }

    public Serializer getSerializer() {
        if (serializer == null) {
            serializer = new XmlSerializer(); //lazily defaults to XmlSerializer
        }
        return serializer;
    }

    /** sets the serializer used when issuing {@link org.reflections.Reflections#save} */
    public ConfigurationBuilder setSerializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }
}
