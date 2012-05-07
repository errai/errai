package org.jboss.errai.common.client.webworkers;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class AbstractWebWorker extends JavaScriptObject {
  private final List<WorkerMessageListener<Object>> messageListeners;
  private final JSONObject workerReference;
  private static int workerCount = 0;

  protected AbstractWebWorker() {
    this.messageListeners = new ArrayList<WorkerMessageListener<Object>>();
    this.workerReference = createWebWorker();
  }

  public void addListener(WorkerMessageListener<Object> workerMessageListener) {
    messageListeners.add(workerMessageListener);
  }

  private void fireMessageListeners(WorkerMessage<Object> workerMessage) {
    for (WorkerMessageListener<Object> listener : messageListeners) {
      listener.onMessage(workerMessage);
    }
  }

  private native JSONObject createWebWorker() /*-{
    var blobBuilder = new BlobBuilder();
    var count = this.@org.jboss.errai.common.client.webworkers.AbstractWebWorker::getNextWorker()();
    var callbackMethName = "Errai_fireMessageListeners_WW" + count;

    $wnd[callbackMethName] =
            function (val) {
              this.@org.jboss.errai.common.client.webworkers.AbstractWebWorker::fireMessageListeners(Lorg/jboss/errai/common/client/webworkers/WorkerMessage;)(val);
            };

    blobBuilder.append("onmessage = function(val) { " + callbackMethName + "(val); };");

    var webWorker = new Worker(window.URL.createObjectURL(blobBuilder.getBlob()));
    return webWorker;
  }-*/;

  public native void start() /*-{
    this.@org.jboss.errai.common.client.webworkers.AbstractWebWorker::workerReference.postMessage();
  }-*/;

  public native void postMessage(WorkerMessage<?> message) /*-{
    this.@org.jboss.errai.common.client.webworkers.AbstractWebWorker::workerReference.postMessage(message);
  }-*/;

  int getNextWorker() {
    return workerCount++;
  }
}
