package org.jboss.errai.bus.server.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.Module;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.annotations.LoadModule;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.security.auth.rules.RolesRequiredRule;
import org.jboss.errai.bus.server.util.ConfigUtil;

import java.io.File;
import java.util.*;

public class ErraiServiceConfiguratorImpl implements ErraiServiceConfigurator {
    private ServerMessageBus bus;
    private List<File> configRootTargets;
    private Set<String> loadedTargets;

    @Inject
    public ErraiServiceConfiguratorImpl(ServerMessageBus bus) {
        this.bus = bus;
    }

    public void configure() {
        loadedTargets = new HashSet<String>();
        configRootTargets = ConfigUtil.findAllConfigTargets();

        findLoadableModules();

        try {
            ResourceBundle erraiServiceConfig = ResourceBundle.getBundle("ErraiService");

            Enumeration<String> keys = erraiServiceConfig.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();

                if ("errai.require_authentication_for_all".equals(key)) {
                    if ("true".equals(erraiServiceConfig.getString(key))) {
                        bus.addRule("ClientNegotiationService", new RolesRequiredRule(new HashSet<Object>(), bus));
                    }
                }

            }
        }
        catch (Exception e) {
            throw new RuntimeException("error reading from configuration", e);
        }
    }

    private void findLoadableModules() {
        for (File root : configRootTargets) {
            _findLoadableModules(root, root);
        }
    }

    private void _findLoadableModules(File root, File start) {
        for (File file : start.listFiles()) {
            if (file.isDirectory()) _findLoadableModules(root, file);
            if (file.getName().endsWith(".class")) {
                try {
                    String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());

                    if (loadedTargets.contains(FQCN)) {
                        return;
                    } else {
                        loadedTargets.add(FQCN);
                    }

                    Class<?> loadClass = Class.forName(FQCN);

                    if (Module.class.isAssignableFrom(loadClass)) {
                        final Class<? extends Module> clazz = loadClass.asSubclass(Module.class);

                        if (clazz.isAnnotationPresent(LoadModule.class)) {
                            Guice.createInjector(new AbstractModule() {
                                @Override
                                protected void configure() {
                                    bind(Module.class).to(clazz);
                                    bind(MessageBus.class).toInstance(bus);
                                }
                            }).getInstance(Module.class).init();
                        }

                    } else if (MessageCallback.class.isAssignableFrom(loadClass)) {
                        final Class<? extends MessageCallback> clazz = loadClass.asSubclass(MessageCallback.class);
                        if (clazz.isAnnotationPresent(Service.class)) {
                            MessageCallback svc = Guice.createInjector(new AbstractModule() {
                                @Override
                                protected void configure() {
                                    bind(MessageCallback.class).to(clazz);
                                    bind(MessageBus.class).toInstance(bus);
                                }
                            }).getInstance(MessageCallback.class);

                            String svcName = clazz.getAnnotation(Service.class).value();

                            if ("".equals(svcName)) {
                                svcName = clazz.getSimpleName();
                            }

                            bus.subscribe(svcName, svc);

                            RolesRequiredRule rule = null;
                            if (clazz.isAnnotationPresent(RequireRoles.class)) {
                                rule = new RolesRequiredRule(clazz.getAnnotation(RequireRoles.class).value(), bus);
                            } else if (clazz.isAnnotationPresent(RequireAuthentication.class)) {
                                rule = new RolesRequiredRule(new HashSet<Object>(), bus);
                            }
                            if (rule != null) {
                                bus.addRule(svcName, rule);
                            }
                        }
                    }
                }
                catch (NoClassDefFoundError e) {
                    // do nothing.
                }
                catch (ExceptionInInitializerError e) {
                    // do nothing.
                }
                catch (UnsupportedOperationException e) {
                    // do nothing.
                }
                catch (ClassNotFoundException e) {
                    // do nothing.
                }
                catch (UnsatisfiedLinkError e) {
                    // do nothing.
                }
            }
        }
    }

    private String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }

}
