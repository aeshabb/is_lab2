package org.itmo.lab1.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackages = {
    "org.itmo.lab1.service",
    "org.itmo.lab1.repository"
})
public class SpringConfig {
}

