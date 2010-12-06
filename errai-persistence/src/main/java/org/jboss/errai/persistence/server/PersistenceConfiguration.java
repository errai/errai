/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.persistence.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.framework.ModelAdapter;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.api.ErraiConfig;
import org.jboss.errai.bus.server.api.ErraiConfigExtension;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Configures {@link HibernateAdapter} and make it available
 * as an injection point in guice ( see {@link ResourceProvider} )
 *
 * @author Heiko Braun
 * @author Marcin Misiewicz 
 */
@ExtensionComponent
public class PersistenceConfiguration implements ErraiConfigExtension {
	
  private ErraiServiceConfigurator configurator;
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Inject
  public PersistenceConfiguration(ErraiServiceConfigurator configurator) {
    this.configurator = configurator;

    logger.info("Configuring persistence extension.");
  }

  public void configure(ErraiConfig config) {
	logger.info("Configuring persistence extension.");
	if (!configurator.hasProperty("errai.persistence.factory.jndi.name")) {
		logger.info("Stopped configuring persistence extension, can't find errai.factory.jndi.name.");
		return;
	} 
	String jndiName = configurator.getProperty("errai.persistence.factory.jndi.name");
	boolean useJbossUtil = Boolean.valueOf(configurator.getProperty("errai.persistence.use_jboss_util"));
	boolean usingJpa = Boolean.valueOf(configurator.getProperty("errai.persistence.using_jpa"));
	logger.info("Factory JNDI name : "+jndiName);
	logger.info("Use jboss specific utility :  "+useJbossUtil);
	logger.info("Using JPA  : "+usingJpa);

	final ModelAdapter modelAdapter = new HibernateAdapter(jndiName, useJbossUtil, usingJpa);
	ResourceProvider<ModelAdapter> modelAdapterProvider = new ResourceProvider<ModelAdapter>() {

		public ModelAdapter get() {
			return modelAdapter;
		}
	};
	
	logger.info("Adding binding for: "+modelAdapter.getClass());
	config.addBinding(ModelAdapter.class, modelAdapterProvider);
  }
}
