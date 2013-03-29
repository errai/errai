package org.jboss.errai.aerogear.api.pipeline;

import org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
public class PipeFactory {

  private native void setup(String name, String type, String recordId, List<String> settings) /*-{
      $wnd.pipe = $wnd.AeroGear.Pipeline([{
          name: name,
          type: type,
          recordId: recordId,
          settings: settings
      }]).pipes[name];
  }-*/;


  public <T> Pipe<T> createPipe(Config config) {
    setup(config.getName(), config.getType().getName(), config.getRecordId(), config.getSettings());
    return new PipeAdapter<T>();
  }

  public <T> Pipe<T> createPipe(String name) {
    return createPipe(new Config(name));
  }

  public static class Config {
    private String name;
    private PipeType type;
    private String recordId;
    private List<String> settings = new ArrayList<String>();

    public Config() {
      this("pipes");
    }

    public Config(String name) {
      this(name, PipeType.REST);
    }

    public Config(String name, PipeType type) {
      this(name, type, "id");
    }

    public Config(String name, PipeType type, String recordId) {
      this.name = name;
      this.type = type;
      this.recordId = recordId;
    }

    public String getName() {
      return name;
    }

    public PipeType getType() {
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
