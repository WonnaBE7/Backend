package com.wonnabe.codef.util;

import java.util.function.Supplier;

public class RetryUtils {

    public static <T> T retryWithBackoff(Supplier<T> task, int maxRetries, long delayMillis) {
        int attempt = 0;
        while (true) {
            try {
                return task.get();
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }
}
