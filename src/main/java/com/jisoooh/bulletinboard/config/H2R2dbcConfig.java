package com.jisoooh.bulletinboard.config;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableR2dbcRepositories
public class H2R2dbcConfig extends AbstractR2dbcConfiguration {
	private static final String H2_DB_URL = "r2dbc:h2:mem:///testdb";
	private static final String H2_DDL_PATH = "scheme/DDL.sql";
	private static final String H2_DML_PATH = "scheme/DML.sql";

	@Value("${spring.r2dbc.username}")
	private String dbUserName;
	@Value("${spring.r2dbc.password}")
	private String dbPassword;
	@Value("${spring.r2dbc.pool.max-size}")
	private int maxPoolSize;
	@Value("${spring.r2dbc.pool.max-create-connection-time}")
	private long connectionTimeout;
	@Value("${spring.r2dbc.pool.max-life-time}")
	private long maxLifeTime;

	@Override
	@Bean
	public ConnectionFactory connectionFactory() {
		ConnectionFactory connectionFactory = ConnectionFactories.get(
				builder().from(parse(H2_DB_URL))
						.option(USER, dbUserName)
						.option(PASSWORD, dbPassword)
						.build());

		ConnectionPoolConfiguration connectionPoolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
				.maxSize(maxPoolSize)
				.maxCreateConnectionTime(Duration.ofSeconds(connectionTimeout))
				.maxLifeTime(Duration.ofSeconds(maxLifeTime))
				.build();

		return new ConnectionPool(connectionPoolConfiguration);
	}

	@Bean
	public ConnectionFactoryInitializer h2DbInitializer() {
		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		Resource initSchema = new ClassPathResource(H2_DDL_PATH);
		Resource initData = new ClassPathResource(H2_DML_PATH);

		initializer.setConnectionFactory(connectionFactory());
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(initSchema, initData));

		return initializer;
	}
}
