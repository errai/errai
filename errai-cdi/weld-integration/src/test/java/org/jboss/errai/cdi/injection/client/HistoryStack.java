package org.jboss.errai.cdi.injection.client;

import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HistoryStack<T> {
  ArrayList<T> historyList;
  int historyIndex = -1;
  
  @Inject
  public HistoryStack(ArrayList<T> historyList) {
    this.historyList = historyList;
  }
  
  public ArrayList<T> getHistoryList() {
    return historyList;
  }

}