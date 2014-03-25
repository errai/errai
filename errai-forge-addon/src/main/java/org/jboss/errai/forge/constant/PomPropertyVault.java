package org.jboss.errai.forge.constant;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class PomPropertyVault {

  /**
   * An enumeration of Maven pom file properties.
   * 
   * @author Max Barkley <mbarkley@redhat.com>
   */
  public static enum Property {
    JbossHome("errai.jboss.home"),
    ErraiVersion("errai.version"),
    DevContext("errai.dev.context");

    private String name;

    private Property(final String name) {
      this.name = name;
    }

    /**
     * @return The name of this property.
     */
    public String getName() {
      return name;
    }

    /**
     * @return An invocation of this property (i.e. for a value named
     *         {@code some.prop} this would return <code>${some.prop}</code>.
     */
    public String invoke() {
      return "${" + name + "}";
    }
  }

}
