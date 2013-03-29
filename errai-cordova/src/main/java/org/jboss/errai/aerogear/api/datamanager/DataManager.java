package org.jboss.errai.aerogear.api.datamanager;

import org.jboss.errai.aerogear.api.datamanager.impl.StoreAdapter;

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

  private native void setup(String name, String type, String recordId, List<String> settings) /*-{
      $wnd.store = $wnd.AeroGear.DataManager([{
          name: name,
          type: type,
          recordId: recordId,
          settings: settings
      }]).stores[name];
  }-*/;

  public Store store(Config config) {
    setup(config.getName(), config.getType().getName(), config.getRecordId(), config.getSettings());
    return new StoreAdapter();
  }

  public <T> Store<T> store() {
    return store(new Config());
  }

  public <T> Store<T> store(String name) {
    return store(new Config(name));
  }

  public static class Config {
    private String name;
    private StoreType type;
    private String recordId;
    private List<String> settings;

    public Config() {
      this("store");
    }

    public Config(String name) {
      this(name, StoreType.MEMORY);
    }

    public Config(String name, StoreType type) {
      this(name, type, "id");
    }

    public Config(String name, StoreType type, String recordId) {
      this.name = name;
      this.type = type;
      this.recordId = recordId;
    }

    public String getName() {
      return name;
    }

    public StoreType getType() {
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
