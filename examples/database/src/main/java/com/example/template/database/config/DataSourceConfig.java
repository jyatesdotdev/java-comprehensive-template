package com.example.template.database.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Explicit HikariCP configuration — demonstrates programmatic pool tuning.
 *
 * <p>In most Spring Boot apps, auto-configuration via {@code application.yml} is sufficient.
 * Use this approach when you need multiple data sources or custom pool logic.
 *
 * <h3>Key HikariCP settings</h3>
 * <ul>
 *   <li><b>maximumPoolSize</b> — max connections (default 10). Size to your DB and workload.</li>
 *   <li><b>minimumIdle</b> — min idle connections. Set equal to maximumPoolSize for stable throughput.</li>
 *   <li><b>connectionTimeout</b> — max wait for a connection from pool (ms).</li>
 *   <li><b>idleTimeout</b> — max time a connection can sit idle before eviction.</li>
 *   <li><b>maxLifetime</b> — max lifetime of a connection (set below DB wait_timeout).</li>
 *   <li><b>leakDetectionThreshold</b> — log warning if connection not returned within this time.</li>
 * </ul>
 */
@Configuration
public class DataSourceConfig {

    /**
     * Binds {@code spring.datasource.*} properties to a {@link DataSourceProperties} instance.
     *
     * @return the bound data-source properties
     */
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Creates a HikariCP {@link DataSource} configured via {@code spring.datasource.hikari.*}.
     *
     * @param properties the data-source properties to initialize from
     * @return the configured HikariCP data source
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
