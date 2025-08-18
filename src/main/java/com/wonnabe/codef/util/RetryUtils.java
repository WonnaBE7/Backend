package com.wonnabe.codef.util;

import java.util.function.Supplier;

public class RetryUtils {

    /**
     * 실패 시 {@code delayMillis} 만큼 기다리며 최대 {@code maxRetries}번 시도합니다.
     *
     * @param task        실행할 작업
     * @param maxRetries  최대 시도 횟수(성공 포함)
     * @param delayMillis 재시도 간 대기(ms)
     * @param <T>         반환 타입
     * @return 작업 성공 결과
     * @throws Exception  모두 실패 시 마지막 예외 전파
     */
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
