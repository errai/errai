package org.jboss.errai.reflections.scanners.reg;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.reflections.scanners.Scanner;

public class ScannerRegistry {

  private final Map<Class<? extends Scanner>, String> nameMap = new HashMap<Class<? extends Scanner>, String>();
  
  public String getName(Class<? extends Scanner> clazz) {
    return nameMap.get(clazz);
  }
  
  public void setName(Class<? extends Scanner> clazz, String name) {
    if (!nameMap.containsKey(clazz))
      nameMap.put(clazz, name);
  }
  
  private ScannerRegistry() {}
  
  private static ScannerRegistry registry = null;
  
  public static ScannerRegistry getRegistry() {
    if (registry == null)
      registry = new ScannerRegistry();
    return registry;
  }
  
}
