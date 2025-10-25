package com.ns.solve.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

@Configuration
public class CustomMetricsConfig {

    @Bean
    public MeterBinder virtualThreadsBinder() {
        return registry -> {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

            // 전체 가상 스레드 수
            Gauge.builder("jvm.threads.virtual.total", () -> getVirtualThreadCount(null))
                    .description("Total number of active virtual threads")
                    .register(registry);

            // 상태별 가상 스레드 수
            Gauge.builder("jvm.threads.virtual.runnable", () -> getVirtualThreadCount(Thread.State.RUNNABLE))
                    .description("Number of virtual threads in RUNNABLE state")
                    .register(registry);

            Gauge.builder("jvm.threads.virtual.waiting", () -> getVirtualThreadCount(Thread.State.WAITING))
                    .description("Number of virtual threads in WAITING state")
                    .register(registry);

            Gauge.builder("jvm.threads.virtual.timed_waiting", () -> getVirtualThreadCount(Thread.State.TIMED_WAITING))
                    .description("Number of virtual threads in TIMED_WAITING state")
                    .register(registry);

            Gauge.builder("jvm.threads.virtual.blocked", () -> getVirtualThreadCount(Thread.State.BLOCKED))
                    .description("Number of virtual threads in BLOCKED state")
                    .register(registry);
        };
    }

    private int getVirtualThreadCount(Thread.State filter) {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        return (int) threads.keySet().stream()
                .filter(Thread::isVirtual)  // 가상 스레드만 필터링
                .filter(t -> filter == null || t.getState() == filter)  // 상태 필터
                .count();
    }

    @Bean
    public MeterBinder hikariMetrics(HikariDataSource dataSource) {
        return registry -> {
            HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
            registry.gauge("hikaricp.active", pool, HikariPoolMXBean::getActiveConnections);
            registry.gauge("hikaricp.pending", pool, HikariPoolMXBean::getThreadsAwaitingConnection);
            registry.gauge("hikaricp.idle", pool, HikariPoolMXBean::getIdleConnections);
            registry.gauge("hikaricp.total.connections", pool, HikariPoolMXBean::getTotalConnections);
        };
    }

}
