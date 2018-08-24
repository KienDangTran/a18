package com.a18.common.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple dummy stupid class to provide {@link Clock} instance for other date time APIs to work.
 *
 * This is required to make code testable. If we call {@code LocalDate.now()}, then that code is coupled with system
 * time, and we can't test its function at other time and timezone.
 *
 * <b>All code that use Java 8 DateTime API</b> should refer to {@link #getClock()} to take the
 * clock instance.
 */
public class ClockProvider {
  private static final ReentrantLock clockAccessLock = new ReentrantLock();

  private static Clock clock = Clock.systemDefaultZone();

  private ClockProvider() {
    // Util class
  }

  public static synchronized Clock getClock() {
    clockAccessLock.lock();
    try {
      return clock;
    } finally {
      clockAccessLock.unlock();
    }
  }

  public static synchronized void setClock(Clock newClock) {
    clockAccessLock.lock();
    ClockProvider.clock = newClock;
    clockAccessLock.unlock();
  }

  public static synchronized LocalDateTime now() {
    return LocalDateTime.now(ClockProvider.getClock()).truncatedTo(ChronoUnit.SECONDS);
  }

  public static LocalDate today() {
    return LocalDate.now(ClockProvider.getClock());
  }
}

