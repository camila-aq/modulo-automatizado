
package com.camila.moduloautomatizado.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

    @Bean(name = "controlOcupacionExecutor")
    public Executor controlOcupacionExecutor() {

        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        /*
         * Se utiliza un único hilo para evitar que
         * múltiples eventos procesen simultáneamente
         * la misma reserva.
         */
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);

        executor.setQueueCapacity(512);

        executor.setThreadNamePrefix(
                "control-ocupacion-"
        );

        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.initialize();

        return executor;
    }
}
