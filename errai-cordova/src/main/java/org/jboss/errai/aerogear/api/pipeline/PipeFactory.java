package org.jboss.errai.aerogear.api.pipeline;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author edewit@redhat.com
 */
public class PipeFactory {

  private native JavaScriptObject setup(String name, String type, String recordId) /*-{
      return $wnd.AeroGear.Pipeline([{
          name: name,
          type: type,
          recordId: recordId
      }]).pipes[name];
  }-*/;


  public <T> Pipe<T> createPipe(Config config) {
    JavaScriptObject object = setup(config.getName(), config.getType().getName(), config.getRecordId());
    return new PipeAdapter<T>(object);
  }

  public <T> Pipe<T> createPipe(String name) {
    return createPipe(new Config(name));
  }

  public static class Config {
    private String name;
    private PipeType type;
    private String recordId;

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
  }
}
