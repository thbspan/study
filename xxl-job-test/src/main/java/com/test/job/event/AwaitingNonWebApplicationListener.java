package com.test.job.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

@Component
public class AwaitingNonWebApplicationListener implements SmartApplicationListener {
    private static final String[] WEB_APPLICATION_CONTEXT_CLASSES = new String[]{
            "org.springframework.web.context.WebApplicationContext",
            "org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext"
    };
    private static final int UNDEFINED_ID = -1;
    private static final AtomicInteger APPLICATION_CONTEXT_ID = new AtomicInteger(UNDEFINED_ID);

    private static final AtomicBoolean AWAITED = new AtomicBoolean(false);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final Lock LOCK = new ReentrantLock();

    private static final Condition CONDITION = LOCK.newCondition();

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ApplicationReadyEvent.class.isAssignableFrom(eventType)
                || ContextClosedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            onApplicationReadyEvent((ApplicationReadyEvent) event);
        } else if (event instanceof ContextClosedEvent) {
            release();
        }
    }

    private void onApplicationReadyEvent(ApplicationReadyEvent event) {
        final ConfigurableApplicationContext applicationContext = event.getApplicationContext();

        if (isWebApplication(applicationContext)) {
            return;
        }

        if (APPLICATION_CONTEXT_ID.compareAndSet(UNDEFINED_ID, applicationContext.hashCode())) {
            await();
            releaseOnExit();
        }
    }

    private void await() {
        if (AWAITED.get()) {
            return;
        }
        if (EXECUTOR_SERVICE.isShutdown()) {
            return;
        }
        EXECUTOR_SERVICE.execute(() -> executeMutually(() -> {
            while (!AWAITED.get()) {
                try {
                    CONDITION.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
    }

    private void releaseOnExit() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::release, "XxlJobShutdownHook"));
    }

    protected void release() {
        executeMutually(() -> {
            while (AWAITED.compareAndSet(false, true)) {
                CONDITION.signalAll();
                shutdown();
            }
        });
    }

    public static boolean isWebApplication(ApplicationContext applicationContext) {
        boolean webApplication = false;
        for (String contextClass : WEB_APPLICATION_CONTEXT_CLASSES) {
            if (isAssignable(contextClass, applicationContext.getClass(), applicationContext.getClassLoader())) {
                webApplication = true;
                break;
            }
        }
        return webApplication;
    }

    private static boolean isAssignable(String target, Class<?> type, ClassLoader classLoader) {
        try {
            return ClassUtils.resolveClassName(target, classLoader).isAssignableFrom(type);
        } catch (Throwable ex) {
            return false;
        }
    }

    private void shutdown() {
        if (!EXECUTOR_SERVICE.isShutdown()) {
            // Shutdown executorService
            EXECUTOR_SERVICE.shutdown();
        }
    }

    private void executeMutually(Runnable runnable) {
        try {
            LOCK.lock();
            runnable.run();
        } finally {
            LOCK.unlock();
        }
    }
}
