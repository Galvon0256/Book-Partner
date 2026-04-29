package com.cg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.cg.entity.Author;
import com.cg.entity.TitleAuthor;
import com.cg.entity.Publisher;
@Configuration
public class RepositoryConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {

        // -------------------------------------------------------
        // 1. Expose entity IDs in JSON responses
        //    Without this, the JSON won't include "auId" or "titleId"
        // -------------------------------------------------------
        config.exposeIdsFor(Author.class, TitleAuthor.class, Publisher.class);

        // -------------------------------------------------------
        // 2. Disable DELETE for both Author and TitleAuthor
        //    This blocks HTTP DELETE method on both endpoints
        // -------------------------------------------------------
        config.getExposureConfiguration()
                .forDomainType(Author.class)
                .withItemExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE))
                .withCollectionExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE));

        config.getExposureConfiguration()
                .forDomainType(TitleAuthor.class)
                .withItemExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE))
                .withCollectionExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE));

        // -------------------------------------------------------
        // 3. Allow frontend (Thymeleaf app) to call this backend
        //    "/**" means allow all paths
        //    In production, replace "*" with your actual frontend URL
        // -------------------------------------------------------
        cors.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS");
    }
}
