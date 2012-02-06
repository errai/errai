package org.jboss.errai.cdi.test.stress.client.shared;

import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A log entry indicating a gap in the tick stream received from the server.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Portable
public class TickStreamDiscontinuity {

  private TickEvent lastTickBeforeProblem;
  private TickEvent firstTickAfterProblem;
  private Date detectionTime;
  
  /** Serialization constructor. Do not use. */
  @Deprecated
  public TickStreamDiscontinuity() {}

  public TickStreamDiscontinuity(TickEvent lastTickBeforeGap,
      TickEvent firstTickAfterGap, Date gapDetectionTime) {
    this.lastTickBeforeProblem = lastTickBeforeGap;
    this.firstTickAfterProblem = firstTickAfterGap;
    this.detectionTime = gapDetectionTime;
  }

  public TickEvent getLastTickBeforeGap() {
    return lastTickBeforeProblem;
  }

  public void setLastTickBeforeGap(TickEvent lastTickBeforeGap) {
    this.lastTickBeforeProblem = lastTickBeforeGap;
  }

  public TickEvent getFirstTickAfterGap() {
    return firstTickAfterProblem;
  }

  public void setFirstTickAfterGap(TickEvent firstTickAfterGap) {
    this.firstTickAfterProblem = firstTickAfterGap;
  }

  public Date getDetectionTime() {
    return detectionTime;
  }

  public void setDetectionTime(Date detectionTime) {
    this.detectionTime = detectionTime;
  }
  
  @Override
  public String toString() {
    int gapSize = firstTickAfterProblem.getId() - lastTickBeforeProblem.getId() - 1;

    final String problemDesc;
    if (gapSize < 0) {
      problemDesc = "Tick arrived late. Expected " + (lastTickBeforeProblem.getId() + 1) + " but got " + firstTickAfterProblem.getId();
    }
    else if (gapSize == 0) {
      problemDesc = "Received duplicate tick: " + firstTickAfterProblem.getId();
    }
    else {
      problemDesc = gapSize + " ticks missing from " +
          lastTickBeforeProblem.getId() + " to " + firstTickAfterProblem.getId();
    }
    
    return detectionTime + ": " + problemDesc;
  }
}
