package org.jboss.errai.jbossas.modules.rebind;

import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.api.ErraiConfig;
import org.jboss.errai.bus.server.api.ErraiConfigExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ExtensionComponent
public class JBossModulesDescripterBuilder implements ErraiConfigExtension {
  private Logger log = LoggerFactory.getLogger(JBossModulesDescripterBuilder.class);

  @Override
  public void configure(ErraiConfig config) {

  }

  private void writeOutDescriptor(String descriptor) {
    File metaINF = new File("META-INF");
    metaINF.mkdirs();

    File services = new File(metaINF.getAbsolutePath() + "/jboss-deployment-structure.xml");
    try {
      services.createNewFile();

      FileOutputStream out = new FileOutputStream(services);

      for (int i = 0; i < descriptor.length(); i++) {
        out.write(descriptor.charAt(i));
      }

      out.flush();
      out.close();
    }
    catch (IOException e) {
      throw new RuntimeException("cannot write to file: " + services.getAbsolutePath());
    }
  }

}
