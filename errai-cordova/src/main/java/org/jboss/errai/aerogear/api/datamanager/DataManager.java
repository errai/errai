package org.jboss.errai.aerogear.api.datamanager;

import org.jboss.errai.aerogear.api.datamanager.impl.StoreWrapper;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class DataManager {
  private final Map<String, Store> stores = new HashMap<String, Store>();

  private native void setup(String name) /*-{
      $wnd.store = $wnd.AeroGear.DataManager( name ).stores[name];
  }-*/;

  public Store store(Config config) {
    setup(config.getName());
    return new StoreWrapper();
  }

  public <T> Store<T> store() {
    return store(new Config().getName());
  }

  public <T> Store<T> store(String name) {
    setup(name);
    return new StoreWrapper<T>();
  }

  public static class Config {
    private String name;
    private String type;
    private String recordId;
    private List<String> settings;

    public Config() {
      this("store");
    }

    public Config(String name) {
      this(name, "memory");
    }

    public Config(String name, String type) {
      this(name, type, "id");
    }

    public Config(String name, String type, String recordId) {
      this.name = name;
      this.type = type;
      this.recordId = recordId;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public String getRecordId() {
      return recordId;
    }

    public List<String> getSettings() {
      return settings;
    }
  }
}
