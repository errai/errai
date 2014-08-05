package org.jboss.errai.ioc.client.container.async;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.errai.common.client.util.CreationalCallback;


/**
 * Returns the requested instance or retrieve it if its already available
 * 
 * have concurrency check build in
 * @author mariusgerwinn
 *
 * @param <T>
 */
public abstract class SingletonBeanProvider<T> {
    
    private T instance;
    
    private final List<CreationalCallback<T>> loadingCallbacks = new ArrayList<CreationalCallback<T>>();
    
    public SingletonBeanProvider() {}
    
   
    public void getInstance(final CreationalCallback<T> callback) {
        if (instance != null)
            callback.callback(instance);
        else {
            loadingCallbacks.add(callback);
            if (loadingCallbacks.size() <= 1)
                getNewInstance(new CreationalCallback<T>() {
                    
                    @Override
                    public void callback(T beanInstance) {
                        instance = beanInstance;
                        for (CreationalCallback<T> c : loadingCallbacks) {
                            getInstance(c);
                        }
                        loadingCallbacks.clear();
                        
                    }
                });
        }
    }
    
    protected abstract void getNewInstance(CreationalCallback<T> callback);
}
