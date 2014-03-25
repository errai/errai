package org.jboss.errai.forge.constant;

public class DefaultVault {
  
  public static enum DefaultValue {
    SourceDirectory("sourceDirectory", "src/main/java"),
    ResourceDirectory("directory", "src/main/resources"),
    WarSourceDirectory("warSourceDirectory", "src/main/webapp");
    
    private final String valueName;
    private final String defaultValue;
    private DefaultValue(final String valueName, final String defaultValue) {
      this.valueName = valueName;
      this.defaultValue = defaultValue;
    }
    public String getDefaultValue() {
      return defaultValue;
    }
    public String getValueName() {
      return valueName;
    }
  }
  
  public static String getValue(final String value, final DefaultValue valueType) {
    return (value != null && !value.equals("")) ? value : valueType.getDefaultValue();
  }

}
