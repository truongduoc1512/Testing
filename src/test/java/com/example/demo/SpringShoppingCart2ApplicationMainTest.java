package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

class SpringShoppingCart2ApplicationMainTest {

    private static final AtomicReference<ConfigurableApplicationContext> CAPTURED_CONTEXT =
            new AtomicReference<>();

    @Test
    void main_startsLazyNonWebContextWithoutCreatingDatabaseBeans() {
        CAPTURED_CONTEXT.set(null);
        ConfigurableApplicationContext context = null;
        try {
            SpringShoppingCart2Application.main(new String[] {
                    "--spring.main.web-application-type=none",
                    "--spring.main.lazy-initialization=true",
                    "--spring.main.register-shutdown-hook=false",
                    "--spring.main.banner-mode=off",
                    "--spring.flyway.enabled=false",
                    "--spring.jmx.enabled=false",
                    "--spring.datasource.url=jdbc:invalid:test17-bootstrap-must-not-connect",
                    "--context.initializer.classes=" + CaptureContextInitializer.class.getName()
            });

            context = CAPTURED_CONTEXT.get();
            assertNotNull(context);
            assertTrue(context.isActive());
            assertFalse(context.getBeanFactory().containsSingleton("dataSource"));
            assertFalse(context.getBeanFactory().containsSingleton("sessionFactory"));
            assertFalse(context.getBeanFactory().containsSingleton("transactionManager"));
        } finally {
            context = context == null ? CAPTURED_CONTEXT.get() : context;
            if (context != null) {
                context.close();
            }
            CAPTURED_CONTEXT.set(null);
        }
    }

    public static class CaptureContextInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            CAPTURED_CONTEXT.set(applicationContext);
        }
    }
}
