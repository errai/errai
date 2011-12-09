/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.NoSegmentAvailableException;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Brock
 */
public class TransmissionBufferTests extends TestCase {
  public void testNoop() {

  }

  public void testBufferWriteAndRead() {
    TransmissionBuffer buffer = new TransmissionBuffer();

    String s = "This is a test";

    BufferColor colorA = new BufferColor(1);


    try {
      ByteArrayInputStream bInputStream = new ByteArrayInputStream(s.getBytes());
      buffer.write(s.length(), bInputStream, colorA);

      ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
      buffer.read(bOutputStream, colorA);

      assertEquals(s, new String(bOutputStream.toByteArray()));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void testBufferCycle() throws IOException {
    TransmissionBuffer buffer = new TransmissionBuffer(10, 2);

    BufferColor color = new BufferColor(2);

    String s = "12345789";


    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      ByteArrayInputStream bInputStream = new ByteArrayInputStream(s.getBytes());
      ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();

      buffer.write(s.length(), bInputStream, color);
      buffer.read(bOutputStream, color);

      assertEquals(s, new String(bOutputStream.toByteArray()));

    }
    System.out.println(System.currentTimeMillis() - start);
  }
//
//  public void testSegmentExhaustion() {
//    TransmissionBuffer buffer = new TransmissionBuffer(10000, 2);
//    try {
//      buffer.allocateSegment();
//      buffer.allocateSegment();
//      buffer.allocateSegment();
//    }
//    catch (NoSegmentAvailableException e) {
//      e.printStackTrace();
//      return;
//    }
//
//    fail("should have thrown an exception");
//  }
//
//  public void testDeallocation() {
//
//    TransmissionBuffer buffer = new TransmissionBuffer(10000, 2);
//    try {
//      buffer.allocateSegment();
//
//      BufferColor seg = buffer.allocateSegment();
//      buffer.deallocateSegment(seg);
//
//      seg = buffer.allocateSegment();
//      buffer.deallocateSegment(seg);
//
//      seg = buffer.allocateSegment();
//      buffer.deallocateSegment(seg);
//
//      buffer.allocateSegment();
//    }
//    catch (Throwable t) {
//      fail("through an exception");
//    }
//  }
//
//  public void testBufferOverflow() {
//    TransmissionBuffer buffer = new TransmissionBuffer(9, 2);
//    try {
//      BufferColor seg = buffer.allocateSegment();
//
//      String s = "1234567890";
//      ByteArrayInputStream bInputStream = new ByteArrayInputStream(s.getBytes());
//
//      buffer.write(bInputStream, seg);
//    }
//    catch (Throwable t) {
//      return;
//    }
//    fail("should have thrown an exception");
//  }
//
//  final static int SEGMENT_COUNT = 4;
//
//  public void testMultithreadedBufferUse() {
//    final List<Thread> readingThreads = new ArrayList<Thread>();
//
//    final List<BufferColor> segs = new ArrayList<BufferColor>();
//
//    final TransmissionBuffer buffer = new TransmissionBuffer(1024 * 1024, 4);
//
//    for (int i = 0; i < SEGMENT_COUNT; i++) {
//      segs.add(buffer.allocateSegment());
//    }
//
//    final String writeString = "<WRITE>";
//
//    final int threadRunCount = 10000;
//
//    final AtomicInteger totalWrites = new AtomicInteger();
//
//    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(100);
//
//    for (int i = 0; i < threadRunCount; i++) {
//      final BufferColor toContend = segs.get((int) (Math.random() * 1000) % SEGMENT_COUNT);
//
//      exec.execute(new Runnable() {
//        @Override
//        public void run() {
//          ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(writeString.getBytes());
//          buffer.write(byteArrayOutputStream, toContend);
//          totalWrites.incrementAndGet();
//        }
//      });
//    }
//
//    final Set<String> results = new ConcurrentSkipListSet<String>();
//
//    for (int i = 0; i < SEGMENT_COUNT; i++) {
//      final int idx = i;
//      readingThreads.add(new Thread() {
//        final BufferColor toRead = segs.get(idx);
//        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//        final StringBuilder buf = new StringBuilder();
//
//        @Override
//        public void run() {
//          final long timeout = System.currentTimeMillis() + (1000 * 10);
//
//
//          while (System.currentTimeMillis() < timeout) {
//            try {
//              byteArrayOutputStream.reset();
//              buffer.readWait(TimeUnit.SECONDS, 5, byteArrayOutputStream, toRead);
//
//              String s = new String(byteArrayOutputStream.toByteArray());
//
//              buf.append(s);
//            }
//            catch (InterruptedException e) {
//              e.printStackTrace();
//              throw new RuntimeException(e);
//            }
//          }
//
//          results.add(buf.toString());
//
//          System.out.println("Thread has exited");
//        }
//      });
//    }
//
//    try {
//      for (Thread read : readingThreads) {
//        read.start();
//      }
//
//      for (Thread read : readingThreads) {
//        read.join();
//      }
//    }
//    catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//
//    int count = 0;
//    for (String res : results) {
//      for (int i = 0; i < res.length(); i += writeString.length()) {
//        String compare = res.substring(i, i + writeString.length());
//        if (writeString.equals(compare)) {
//          count++;
//        }
//        else {
//          throw new RuntimeException("bad string: " + compare);
//        }
//      }
//    }
//
//    for (BufferColor seg : segs) {
//      assertEquals(0, seg.getToRead());
//    }
//
//    assertEquals(totalWrites.intValue(), count);
//  }
}
