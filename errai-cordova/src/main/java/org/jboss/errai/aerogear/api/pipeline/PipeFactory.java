package org.jboss.errai.aerogear.api.pipeline;

import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.aerogear.api.pipeline.auth.Authenticator;
import org.jboss.errai.aerogear.api.pipeline.impl.AuthenticatorAdapter;
import org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter;

/**
 * @author edewit@redhat.com
 */
public class PipeFactory {

  private native JavaScriptObject setup(String name, String type, String recordId, JavaScriptObject auth) /*-{
      return $wnd.AeroGear.Pipeline([{
          name: name,
          type: type,
          recordId: recordId,
          settings: {
              authenticator: auth
          }
      }]).pipes[name];
  }-*/;

  public <T> Pipe<T> createPipe(Config config) {
    JavaScriptObject object = setup(config.getName(), config.getType().getName(), config.getRecordId(), null);
    return new PipeAdapter<T>(object);
  }

  public <T> Pipe<T> createPipe(String name) {
    return createPipe(new Config(name));
  }

  public <T> Pipe<T> createPipe(String name, Authenticator authenticator) {
    Config config = new Config(name);
    AuthenticatorAdapter adapter = (AuthenticatorAdapter) authenticator;
    JavaScriptObject object = setup(config.getName(), config.getType().getName(), config.getRecordId(), adapter.unwrap());
    return new PipeAdapter<T>(object);
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
