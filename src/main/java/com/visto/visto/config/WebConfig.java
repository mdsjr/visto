package com.visto.visto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Serializa Page como PagedModel estável ({ "content": [...], "page": {...} }),
 * evitando o aviso do Spring Data sobre PageImpl.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {
}
