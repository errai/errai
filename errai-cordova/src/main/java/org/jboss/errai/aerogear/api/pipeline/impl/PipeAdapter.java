package org.jboss.errai.aerogear.api.pipeline.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.errai.aerogear.api.impl.AbstractAdapter;
import org.jboss.errai.aerogear.api.pipeline.PagedList;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.aerogear.api.pipeline.PipeType;
import org.jboss.errai.aerogear.api.pipeline.ReadFilter;
import org.jboss.errai.marshalling.client.Marshalling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;

/**
 * @author edewit@redhat.com
 */
@SuppressWarnings("ALL")
public class PipeAdapter<T> extends AbstractAdapter<T> implements Pipe<T> {

  static {
      enableJacksonMarchalling();
  }  

  private native static void enableJacksonMarchalling() /*-{
    $wnd.erraiJaxRsJacksonMarshallingActive = true;
  }-*/;
  
  private final Class<T> type;

  public PipeAdapter(Class<T> type, JavaScriptObject pipe) {
    this.object = pipe;
    this.type = type;
  }

  @Override
  protected T fromJSON(String json) {
    return (T) MarshallingWrapper.fromJSON(json, type);
  }

  @Override
  public PipeType getType() {
    return PipeType.REST;
  }

  @Override
  public void read(AsyncCallback<List<T>> callback) {
    read0(callback);
  }

  private native void read0(AsyncCallback<List<T>> callback) /*-{
      var that = this;
      this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.read(
          {
              success: function (data, textStatus, jqXHR) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(data, callback);
              },
              error: function (jqXHR, textStatus, errorThrown) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(errorThrown, callback);
              }
          });
  }-*/;

  @Override
  public void save(T item, AsyncCallback<T> callback) {
    String json;
    if (!(item instanceof Map)) {
      json = MarshallingWrapper.toJSON(item);
    } else {
      Map map = new HashMap();
      map.putAll((Map) item);
      json = Marshalling.toJSON(map);
    }
    save0(json, callback);
  }

  private native void save0(String item, AsyncCallback<T> callback) /*-{
      var that = this;
      this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.save(eval('[' + item + '][0]'),
          {
              success: function (data, textStatus, jqXHR) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(data, callback);
              },
              error: function (jqXHR, textStatus, errorThrown) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(errorThrown, callback);
              }
          });
  }-*/;

  @Override
  public void remove(String id, AsyncCallback<Void> callback) {
    remove0(id, callback);
  }

  private native void remove0(String id, AsyncCallback<Void> callback) /*-{
      var that = this;
      this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.remove(id, {
          success: function (data, textStatus, jqXHR) {
              callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(null);
          },
          error: function (jqXHR, textStatus, errorThrown) {
              that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(errorThrown, callback);
          }
      })
  }-*/;

  @Override
  public void readWithFilter(ReadFilter filter, AsyncCallback<List<T>> callback) {
    JSONObject where = new JSONObject();
    for (Map.Entry<String, String> entry : filter.getWhere().entrySet()) {
      where.put(entry.getKey(), new JSONString(entry.getValue()));
    }

    readWithFilter(filter.getLimit(), filter.getOffset(), where, callback);
  }

  private native void readWithFilter(Integer limit, Integer offset, JSONObject where, AsyncCallback<List<T>> callback) /*-{
      var that = this;
      this.@org.jboss.errai.aerogear.api.impl.AbstractAdapter::object.read({
          offsetValue: offset,
          limitValue: limit,
          success: function (data, textStatus, jqXHR) {
              that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callbackFilter(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(data, callback);
          },
          error: function (jqXHR, textStatus, errorThrown) {
              that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(errorThrown, callback);
          }
      })
  }-*/;


  private void callback(JavaScriptObject object, AsyncCallback<T> callback) {
    callback.onSuccess(convertToType(object));
  }

  private void callback(JsArray array, AsyncCallback<List<T>> callback) {
    callback.onSuccess(convertToType(array));
  }

  private void callbackFilter(JavaScriptObject array, AsyncCallback<PagedList<T>> callback) {
    callback.onSuccess(new PagedListAdapter(array, convertToType((JsArray) array)));
  }

  private void callback(String errorText, AsyncCallback<T> callback) {
    callback.onFailure(new RequestException(errorText));
  }

  public static class PagedListAdapter<T> extends ArrayList<T> implements PagedList<T> {

    private final JavaScriptObject pagedResultSet;

    public PagedListAdapter(JavaScriptObject pagedResultSet, List<T> list) {
      super(list);
      this.pagedResultSet = pagedResultSet;
    }

    @Override
    public void next(AsyncCallback<List<T>> callback) {
      next0(callback);
    }

    private native void next0(AsyncCallback<List<T>> callback) /*-{
        var that = this;
        this.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter.PagedListAdapter::pagedResultSet.next({
            success: function (morePagedResults) {
                that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callbackFilter(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(morePagedResults, callback);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(textStatus, callback);
            }
        });
    }-*/;

    @Override
    public void previous(AsyncCallback<List<T>> callback) {
      previous0(callback);
    }

    private native void previous0(AsyncCallback<List<T>> callback) /*-{
        var that = this;
        this.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter.PagedListAdapter::pagedResultSet.previous({
            success: function (morePagedResults) {
                that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callbackFilter(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(morePagedResults, callback);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(textStatus, callback);
            }
        });
    }-*/;

  }
}
