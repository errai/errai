package org.jboss.errai.aerogear.api.pipeline.impl;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.errai.aerogear.api.pipeline.*;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

import java.util.*;

/**
 * @author edewit@redhat.com
 */
public class PipeTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.aerogear.api.AerogearTests";
  }

  public void testReadTasks() {
    //given
    Pipe<Task> pipe = new PipeFactory().createPipe("tasks");

    //when
    pipe.read(new FailingAsyncCallback<List<Task>>() {
      @Override
      public void onSuccess(List<Task> tasks) {
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(new Task(12345)));
      }
    });

    pipe.save(new Task(123, "new", "2012-01-01"), new FailingAsyncCallback<Task>() {
      @Override
      public void onSuccess(Task result) {
        assertEquals("Updated Task", result.title);
      }
    });

    pipe.remove("123", new FailingAsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        finishTest();
      }
    });

    delayTestFinish(3000);
  }

  public void testPaging() {
    //given
    Pipe<Task> pipe = new PipeFactory().createPipe(new PipeFactory.Config("pageTestWebLink"));

    ReadFilter filter = new ReadFilter();
    filter.setOffset(1);
    filter.setLimit(2);

    pipe.readWithFilter(filter, new FailingAsyncCallback<List<Task>>() {
      @Override
      public void onSuccess(List<Task> result) {
        assertNotNull(result);
        assertTrue(result instanceof PagedList);
        finishTest();
      }
    });

    delayTestFinish(3000);
  }

  @Portable
  public static class Task {
    private int id;
    private String title;
    private String date;

    public Task(@MapsTo("id") int id, @MapsTo("title") String title, @MapsTo("date") String date) {
      this.id = id;
      this.title = title;
      this.date = date;
    }

    public Task(int id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "Task{" +
              "id=" + id +
              ", title='" + title + '\'' +
              ", date=" + date +
              '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Task)) return false;
      return id == ((Task) o).id;
    }

    @Override
    public int hashCode() {
      return id;
    }
  }

  private abstract class FailingAsyncCallback<E> implements AsyncCallback<E> {
    @Override
    public void onFailure(Throwable caught) {
      fail("exception thrown " + caught.getMessage());
      finishTest();
    }
  }
}
