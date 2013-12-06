package org.jboss.errai.common.client.logging.handlers;

import java.util.logging.Level;


public interface ErraiLogHandler {
  
  public boolean isEnabled();
  
  public void setLevel(Level level);
  
  public Level getLevel();

}
