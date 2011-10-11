package org.jboss.errai.cdi.test.stress.client.shared;

import java.util.Date;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * A log entry indicating a gap in the tick stream received from the server.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@ExposeEntity
public class TickStreamGap {

  private TickEvent lastTickBeforeGap;
  private TickEvent firstTickAfterGap;
  private Date gapDetectionTime;
  
  /** Serialization constructor. Do not use. */
  @Deprecated
  public TickStreamGap() {}

  public TickStreamGap(TickEvent lastTickBeforeGap,
      TickEvent firstTickAfterGap, Date gapDetectionTime) {
    this.lastTickBeforeGap = lastTickBeforeGap;
    this.firstTickAfterGap = firstTickAfterGap;
    this.gapDetectionTime = gapDetectionTime;
  }

  public TickEvent getLastTickBeforeGap() {
    return lastTickBeforeGap;
  }

  public void setLastTickBeforeGap(TickEvent lastTickBeforeGap) {
    this.lastTickBeforeGap = lastTickBeforeGap;
  }

  public TickEvent getFirstTickAfterGap() {
    return firstTickAfterGap;
  }

  public void setFirstTickAfterGap(TickEvent firstTickAfterGap) {
    this.firstTickAfterGap = firstTickAfterGap;
  }

  public Date getGapDetectionTime() {
    return gapDetectionTime;
  }

  public void setGapDetectionTime(Date gapDetectionTime) {
    this.gapDetectionTime = gapDetectionTime;
  }
  
  @Override
  public String toString() {
    int lostTickCount = firstTickAfterGap.getId() - lastTickBeforeGap.getId() - 1;
    long gapStartTime = lastTickBeforeGap.getServerTime();
    long gapEndTime = firstTickAfterGap.getServerTime();
    return lostTickCount + " ticks missing from " +
        lastTickBeforeGap.getId() + " to " + firstTickAfterGap.getId() +
        " (" + (gapEndTime - gapStartTime) + "ms starting at " + new Date(gapStartTime) + ")";
  }
}
