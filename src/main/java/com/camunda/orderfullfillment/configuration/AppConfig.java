package com.camunda.orderfullfillment.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.camunda.orderfullfillment.model.ProductDetailss;

@Configuration
public class AppConfig {

	@Bean
    public ProductDetailss productDetailss() {
        return new ProductDetailss();
    }
}
