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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author edewit@redhat.com
 */
public class PipeAdapter<T> extends AbstractAdapter<T> implements Pipe<T> {
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
      $wnd.pipe.read(
          {
              success: function (data, textStatus, jqXHR) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(data, callback);
              },
              error: function (jqXHR, textStatus, errorThrown) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(textStatus, callback);
              }
          });
  }-*/;

  @Override
  public void save(T item, AsyncCallback<T> callback) {
    save0(item, callback);
  }

  private native void save0(T item, AsyncCallback<T> callback) /*-{
      var that = this;
      $wnd.pipe.save(item,
          {
              success: function (data, textStatus, jqXHR) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(data, callback);
              },
              error: function (jqXHR, textStatus, errorThrown) {
                  that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(textStatus, callback);
              }
          });
  }-*/;

  @Override
  public void remove(String id, AsyncCallback<Void> callback) {
    remove0(id, callback);
  }

  private native void remove0(String id, AsyncCallback<Void> callback) /*-{
      var that = this;
      $wnd.pipe.remove(id, {
          success: function (data, textStatus, jqXHR) {
              callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(null);
          },
          error: function (jqXHR, textStatus, errorThrown) {
              that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(textStatus, callback);
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
      $wnd.pipe.read({
          offsetValue: offset,
          limitValue: limit,
          success: function (data, textStatus, jqXHR) {
              $wnd.pagedResultSet = data;
              that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callbackFilter(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(data, callback);
          },
          error: function (jqXHR, textStatus, errorThrown) {
              that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(textStatus, callback);
          }
      })
  }-*/;


  private void callback(JavaScriptObject object, AsyncCallback<T> callback) {
    callback.onSuccess(convertToType(object));
  }

  private void callback(JsArray array, AsyncCallback<List<T>> callback) {
    callback.onSuccess(convertToType(array));
  }

  private void callbackFilter(JsArray array, AsyncCallback<PagedList<T>> callback) {
    callback.onSuccess(new PagedListAdapter(convertToType(array)));
  }

  private void callback(String errorText, AsyncCallback<T> callback) {
    callback.onFailure(new RequestException(errorText));
  }

  /**
   * @author edewit@redhat.com
   */
  public static class PagedListAdapter<T> extends ArrayList<T> implements PagedList<T> {

    @SuppressWarnings("UnusedDeclaration")
    public PagedListAdapter() {
    }

    public PagedListAdapter(List<T> list) {
      super(list);
    }

    @Override
    public void next(AsyncCallback<List<T>> callback) {
      next0(callback);
    }

    private native void next0(AsyncCallback<List<T>> callback) /*-{
        var that = this;
        $wnd.pagedResultSet.next({
            success: function (morePagedResults) {
                $wnd.pagedResultSet = morePagedResults
                that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callbackFilter(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(morePagedResults, callback);
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
        $wnd.pagedResultSet.previous({
            success: function (morePagedResults) {
                $wnd.pagedResultSet = morePagedResults
                that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callbackFilter(Lcom/google/gwt/core/client/JsArray;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(morePagedResults, callback);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                that.@org.jboss.errai.aerogear.api.pipeline.impl.PipeAdapter::callback(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(textStatus, callback);
            }
        });
    }-*/;

  }
}
