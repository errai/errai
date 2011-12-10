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
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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


  public void testColorInterleaving() throws IOException {
    TransmissionBuffer buffer = new TransmissionBuffer(10, 20);

    BufferColor colorA = new BufferColor(2);
    BufferColor colorB = new BufferColor(3);
    BufferColor colorC = new BufferColor(4);


    String stringA = "12345678";
    String stringB = "ABCDEFGH";
    String stringC = "IJKLMNOP";


    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      ByteArrayInputStream bInputStream = new ByteArrayInputStream(stringA.getBytes());
      buffer.write(stringA.length(), bInputStream, colorA);

      bInputStream = new ByteArrayInputStream(stringB.getBytes());
      buffer.write(stringB.length(), bInputStream, colorB);

      bInputStream = new ByteArrayInputStream(stringC.getBytes());
      buffer.write(stringC.length(), bInputStream, colorC);


      ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
      buffer.read(bOutputStream, colorA);
      assertEquals(stringA, new String(bOutputStream.toByteArray()));

      bOutputStream = new ByteArrayOutputStream();
      buffer.read(bOutputStream, colorB);
      assertEquals(stringB, new String(bOutputStream.toByteArray()));

      bOutputStream = new ByteArrayOutputStream();
      buffer.read(bOutputStream, colorC);
      assertEquals(stringC, new String(bOutputStream.toByteArray()));

    }
    System.out.println(System.currentTimeMillis() - start);
  }

  final static int COLOR_COUNT = 10;

  public void testAudited() throws Exception {

    final List<BufferColor> colors = new ArrayList<BufferColor>();

    final TransmissionBuffer buffer = new TransmissionBuffer();

    for (int i = 0; i < COLOR_COUNT; i++) {
      colors.add(new BufferColor(i + 1));
    }
    final Random random = new Random(2234);

    final String[] writeString = {"<JIMMY>", "<CRAB>", "<KITTY>", "<DOG>", "<JONATHAN>"};

    final Map<Integer, List<String>> writeLog = new HashMap<Integer, List<String>>();

    final int createCount = 500;

    final AtomicInteger totalWrites = new AtomicInteger();

    List<String> results = Collections.synchronizedList(new ArrayList<String>());

    for (int i = 0; i < createCount; i++) {
      final BufferColor toContend = colors.get(random.nextInt(COLOR_COUNT));
      assertNotNull(toContend);
      new Runnable() {
        @Override
        public void run() {
          try {
            String toWrite = writeString[random.nextInt(writeString.length)];
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(toWrite.getBytes());
            buffer.write(toWrite.getBytes().length, byteArrayInputStream, toContend);
            totalWrites.incrementAndGet();

            List<String> stack = writeLog.get(toContend.getColor());
            if (stack == null) {
              writeLog.put(toContend.getColor(), stack = new ArrayList<String>());
            }

            stack.add(toWrite);

            System.out.println("Wrote color " + toContend.getColor() + ": " + toWrite + ". Total writes is now " + totalWrites);
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }.run();
    }

    assertEquals(createCount, totalWrites.intValue());

    AtomicInteger resultSequenceNumber = new AtomicInteger();

    for (int i = 0; i < COLOR_COUNT; i++) {
      resultSequenceNumber.incrementAndGet();

      final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byteArrayOutputStream.reset();
      assertEquals(0, byteArrayOutputStream.size());
      buffer.read(byteArrayOutputStream, colors.get(i));
      assertTrue("Expected >0 bytes; got " + byteArrayOutputStream.size(), byteArrayOutputStream.size() > 0);

      String val = new String(byteArrayOutputStream.toByteArray());
      results.add(val);

      List<String> buildResultList = new ArrayList<String>();

      int st = 0;
      for (int c = 0; c < val.length(); c++) {
        switch (val.charAt(c)) {
          case '>':
            c++;
            buildResultList.add(val.substring(st, st = c));
        }
      }


      List<String> resultList = new ArrayList<String>(buildResultList);
      List<String> log = new ArrayList<String>(writeLog.get(colors.get(i).getColor()));

      while (!log.isEmpty() && !resultList.isEmpty()) {
        String nm = log.remove(0);
        String test = resultList.remove(0);
        if (!nm.equals(test)) {
          System.out.println("[" + resultSequenceNumber + "] expected : " + nm + " -- but found: " + test
                  + " (color: " + colors.get(i).getColor() + ")");

          System.out.println("  --> log: " + writeLog.get(colors.get(i).getColor()) + " vs result: " + buildResultList);
        }
      }


      if (!log.isEmpty())
        System.out.println("[" + resultSequenceNumber + "] results have missing items: " + log
                + " (color: " + colors.get(i).getColor() + ")");

      if (!resultList.isEmpty())
        System.out.println("[" + resultSequenceNumber + "] results contain items not logged: " + resultList
                + " (color: " + colors.get(i).getColor() + ")");
    }

    assertEquals(COLOR_COUNT, results.size());

    int count = 0;
    for (String res : results) {
      for (int i = 0; i < res.length(); i++) {
        if (res.charAt(i) == '<') count++;
      }

      System.out.println();
      System.out.print(res);
    }


    buffer.dumpSegments();

    assertEquals(createCount, count);
  }


  final static int SEGMENT_COUNT = 100;

  public void testMultithreadedBufferUse() throws Exception {
    final List<Thread> readingThreads = new ArrayList<Thread>();

    final List<BufferColor> segs = new ArrayList<BufferColor>();

    final TransmissionBuffer buffer = new TransmissionBuffer();

    for (int i = 0; i < SEGMENT_COUNT; i++) {
      segs.add(new BufferColor(i + 1));
    }

    final String[] writeString = {"<JIMMY>", "<CRAB>", "<KITTY>", "<DOG>"};

    final int threadRunCount = 10000;

    final AtomicInteger totalWrites = new AtomicInteger();
    final AtomicInteger totalReads = new AtomicInteger();
    
    final CountDownLatch latch = new CountDownLatch(threadRunCount);

    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(40);

    for (int i = 0; i < threadRunCount; i++) {
      final BufferColor toContend = segs.get((int) (Math.random() * 1000) % SEGMENT_COUNT);

      exec.execute(new Runnable() {
        @Override
        public void run() {
          try {
            String toWrite = writeString[(int) (Math.random() * 1000) % writeString.length];
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(toWrite.getBytes());
            buffer.write(toWrite.length(), byteArrayInputStream, toContend);
            totalWrites.incrementAndGet();
            latch.countDown();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }

    /**
     * Wait a maximum of 20 seconds.
     */
    latch.await(20, TimeUnit.SECONDS);

    for (int i = 0; i < SEGMENT_COUNT; i++) {
      final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byteArrayOutputStream.reset();
      buffer.readWait(TimeUnit.MILLISECONDS, 100, byteArrayOutputStream, segs.get(i));

      String val = new String(byteArrayOutputStream.toByteArray());
      List<String> buildResultList = new ArrayList<String>();

      int st = 0;
      for (int c = 0; c < val.length(); c++) {
        switch (val.charAt(c)) {
          case '>':
            c++;
            buildResultList.add(val.substring(st, st = c));
        }
      }

      boolean match;
      for (String s : buildResultList) {
        match = false;
        for (String testString : writeString) {
          if (s.equals(testString)) match = true;
        }
        assertTrue("unrecognized test string: " + s, match);
      }


      totalReads.addAndGet(buildResultList.size());
    }

    assertEquals(totalWrites.intValue(), totalReads.intValue());
  }
}
