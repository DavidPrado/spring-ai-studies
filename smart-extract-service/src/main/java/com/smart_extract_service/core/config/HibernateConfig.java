package com.smart_extract_service.core.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {
    @Bean
    public HibernatePropertiesCustomizer hibernateCustomizer() {
        return (hibernateProperties) ->
                hibernateProperties.put("hibernate.session_factory.statement_inspector", new TenantStatementInspector());
    }
}
