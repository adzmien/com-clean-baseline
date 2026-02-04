package com.clean.backoffice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.clean.backoffice.entity.CleanConfigEntity;
import com.clean.common.base.component.DynamicFilterComponent;

@Configuration
public class DefaultBeanConfig {

    @Bean
    public DynamicFilterComponent<CleanConfigEntity> cleanConfigFilterComponent() {
        return new DynamicFilterComponent<>();
    }
}
