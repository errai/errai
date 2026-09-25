/*
 * Copyright (C) 2024 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Minimal GWT supersource stub for java.time.Duration.
 * Only the methods required by Hibernate Validator's DurationMin/DurationMax
 * validators are included. They always return neutral values so no actual
 * duration-based validation occurs in the browser.
 */
package java.time;

public class Duration implements Comparable<Duration> {

  private final long nanos;

  private Duration(final long nanos) {
    this.nanos = nanos;
  }

  public static Duration ofNanos(final long nanos) {
    return new Duration(nanos);
  }

  public static Duration ofMillis(final long millis) {
    return new Duration(millis * 1_000_000L);
  }

  public static Duration ofSeconds(final long seconds) {
    return new Duration(seconds * 1_000_000_000L);
  }

  public static Duration ofMinutes(final long minutes) {
    return ofSeconds(minutes * 60L);
  }

  public static Duration ofHours(final long hours) {
    return ofMinutes(hours * 60L);
  }

  public static Duration ofDays(final long days) {
    return ofHours(days * 24L);
  }

  public Duration plusNanos(final long nanosToAdd) {
    return new Duration(this.nanos + nanosToAdd);
  }

  public Duration plusMillis(final long millisToAdd) {
    return plusNanos(millisToAdd * 1_000_000L);
  }

  public Duration plusSeconds(final long secondsToAdd) {
    return plusNanos(secondsToAdd * 1_000_000_000L);
  }

  public Duration plusMinutes(final long minutesToAdd) {
    return plusSeconds(minutesToAdd * 60L);
  }

  public Duration plusHours(final long hoursToAdd) {
    return plusMinutes(hoursToAdd * 60L);
  }

  public Duration plusDays(final long daysToAdd) {
    return plusHours(daysToAdd * 24L);
  }

  @Override
  public int compareTo(final Duration other) {
    return Long.compare(this.nanos, other.nanos);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Duration)) {
      return false;
    }
    final Duration other = (Duration) obj;
    return this.nanos == other.nanos;
  }

  @Override
  public int hashCode() {
    return (int) (nanos ^ (nanos >>> 32));
  }

  @Override
  public String toString() {
    return "PT" + (nanos / 1_000_000_000L) + "." + (Math.abs(nanos) % 1_000_000_000L) + "S";
  }
}
