package org.jboss.errai.persistence.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.hibernate.ejb.Ejb3Configuration;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.server.ErraiModule;
import org.jboss.errai.bus.server.annotations.ExtensionConfigurator;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.ConfigVisitor;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.util.List;


/**
 * This is a prototype implementation of the persistence service.
 */
@ExtensionConfigurator
public class ErraiPersistenceConfigurator implements ErraiConfigExtension {
    private ErraiBus bus;
    private ErraiModule module;
    private ErraiServiceConfigurator config;

    @Inject
    public ErraiPersistenceConfigurator(ErraiBus bus, ErraiModule module, ErraiServiceConfigurator config) {
        this.bus = bus;
        this.module = module;
        this.config = config;
    }

    public void configure() {
        final Ejb3Configuration ejbconf = new Ejb3Configuration();
        ejbconf.setProperty("hibernate.connection.datasource", "java:/" + config.getProperty("errai.prototyping.persistence.datasource"));
        ejbconf.setProperty("hibernate.dialect", config.getProperty("errai.prototyping.persistence.dialect"));
        ejbconf.setProperty("hibernate.show_sql", "false");
        ejbconf.setProperty("hibernate.hbm2ddl.auto", "update");

        List<File> roots = config.getConfigurationRoots();

        ConfigUtil.visitAllTargets(roots, new ConfigVisitor() {
            public void visit(Class<?> clazz) {
                if (clazz.isAnnotationPresent(Entity.class)) {
                    ejbconf.addAnnotatedClass(clazz);
                }
            }
        });

        final EntityManagerFactory emf = ejbconf.buildEntityManagerFactory();
        module.bind(EntityManager.class).toProvider(new Provider<EntityManager>() {
            public EntityManager get() {
                return emf.createEntityManager();
            }
        });

        
    }
}
