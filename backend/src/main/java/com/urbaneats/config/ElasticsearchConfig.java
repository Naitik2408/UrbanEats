package com.urbaneats.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch configuration for UrbanEats application.
 * Configures Elasticsearch client connection.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.urbaneats.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:5s}")
    private String connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:30s}")
    private String socketTimeout;

    @Override
    public ClientConfiguration clientConfiguration() {
        var builder = ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.replace("http://", "").replace("https://", ""))
                .withConnectTimeout(parseDuration(connectionTimeout))
                .withSocketTimeout(parseDuration(socketTimeout));

        // Add basic auth if credentials are provided
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            return builder.withBasicAuth(username, password).build();
        }

        return builder.build();
    }

    /**
     * Parse duration string (e.g., "5s", "30s") to milliseconds.
     */
    private java.time.Duration parseDuration(String duration) {
        return java.time.Duration.parse("PT" + duration.toUpperCase());
    }
}
