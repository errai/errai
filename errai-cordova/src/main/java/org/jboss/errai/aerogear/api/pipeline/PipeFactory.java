package org.jboss.errai.aerogear.api.pipeline;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.aerogear.api.pipeline.auth.Authenticator;
import org.jboss.errai.aerogear.api.pipeline.impl.AuthenticatorAdapter;
import org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter;

/**
 * @author edewit@redhat.com
 */
public class PipeFactory {

  private native JavaScriptObject setup(String name, String type, String recordId, String baseUrl, JavaScriptObject auth) /*-{
      return $wnd.AeroGear.Pipeline([{
          name: name,
          type: type,
          recordId: recordId,
          settings: {
              baseURL: baseUrl,
              authenticator: auth
          }
      }]).pipes[name];
  }-*/;

  public <T> Pipe<T> createPipe(Class<T> type, Config config) {
    JavaScriptObject object =
            setup(config.name, config.type.getName(), config.recordId, config.baseUrl, null);
    return new PipeAdapter<T>(type, object);
  }

  public <T> Pipe<T> createPipe(Class<T> type, String name) {
    return createPipe(type, new Config(name));
  }

  public <T> Pipe<T> createPipe(Class<T> type, String name, Authenticator authenticator) {
    Config config = new Config(name);
    AuthenticatorAdapter adapter = (AuthenticatorAdapter) authenticator;
    JavaScriptObject object =
            setup(config.name, config.type.getName(), config.recordId, config.baseUrl, adapter.unwrap());
    return new PipeAdapter<T>(type, object);
  }

  public static class Config {
    private String name;
    private PipeType type;
    private String recordId;
    private String baseUrl;

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

    public Config(String name, String recordId, String baseUrl) {
      this(name);
      this.recordId = recordId;
      this.baseUrl = baseUrl;
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

    public String getBaseUrl() {
      return baseUrl;
    }
  }
}
