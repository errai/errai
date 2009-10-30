package org.jboss.errai.bus.server.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.Module;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.annotations.LoadModule;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;
import org.jboss.errai.bus.server.security.auth.rules.RolesRequiredRule;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.ConfigVisitor;

import java.io.File;
import java.util.*;

public class ErraiServiceConfiguratorImpl implements ErraiServiceConfigurator {
    private ServerMessageBus bus;
    private List<File> configRootTargets;
    private Map<String, String> properties;

    private ErraiServiceConfigurator configInst = this;

    @Inject
    public ErraiServiceConfiguratorImpl(ServerMessageBus bus) {
        this.bus = bus;
    }

    public void configure() {
        properties = new HashMap<String, String>();
        configRootTargets = ConfigUtil.findAllConfigTargets();

        try {
            ResourceBundle erraiServiceConfig = ResourceBundle.getBundle("ErraiService");
            Enumeration<String> keys = erraiServiceConfig.getKeys();
            String key;
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                properties.put(key, erraiServiceConfig.getString(key));
            }
        }
        catch (Exception e) {
            throw new RuntimeException("error reading from configuration", e);
        }

        final Map<Class, Provider> bindings = new HashMap<Class, Provider>();


        ConfigUtil.visitAllTargets(configRootTargets, new ConfigVisitor() {
            public void visit(Class<?> loadClass) {
                if (ErraiConfigExtension.class.isAssignableFrom(loadClass)) {
                            if (loadClass.isAnnotationPresent(ExtensionComponent.class)) {
                                final Class<? extends ErraiConfigExtension> clazz =
                                        loadClass.asSubclass(ErraiConfigExtension.class);

                                try {
                                    Guice.createInjector(new AbstractModule() {
                                        @Override
                                        protected void configure() {
                                            bind(ErraiConfigExtension.class).to(clazz);
                                            bind(ErraiServiceConfigurator.class).toInstance(configInst);
                                        }
                                    }).getInstance(ErraiConfigExtension.class).configure(bindings);
                                }
                                catch (Throwable e) {
                                    e.printStackTrace();
                                    throw new RuntimeException("could not initialize extension: " + loadClass.getName(), e);
                                }
                            }
                        }
            }
        });



        ConfigUtil.visitAllTargets(configRootTargets,
                new ConfigVisitor() {
                    public void visit(final Class<?> loadClass) {
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

                                        for (Map.Entry<Class, Provider> entry : bindings.entrySet()) {
                                          bind(entry.getKey()).toProvider(entry.getValue());  
                                        }
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
                }
        );


        String requireAuthenticationForAll = "errai.require_authentication_for_all";

        if (hasProperty(requireAuthenticationForAll) && "true".equals(getProperty(requireAuthenticationForAll))) {
            bus.addRule("ClientNegotiationService", new RolesRequiredRule(new HashSet<Object>(), bus));
        }
    }



    public List<File> getConfigurationRoots() {
        return this.configRootTargets;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }
}
