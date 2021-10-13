package com.jisoooh.bulletinboard.config;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionOption;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableR2dbcRepositories
public class H2R2dbcConfig extends AbstractR2dbcConfiguration {
	private static final String H2_DB_URL = "~/test";
	private static final String H2_DDL_PATH = "scheme/DDL.sql";
	private static final String H2_DML_PATH = "scheme/DML.sql";
	private static final String H2_CONSOLE_PORT = "8081";
	private static final String H2_DB_NAME = "testdb";

	@Value("${database.h2.userName}")
	private String dbUserName;
	@Value("${database.h2.password}")
	private String dbPassword;

	@Override
	@Bean
	public ConnectionFactory connectionFactory() {
		H2ConnectionConfiguration connectionConfiguration = H2ConnectionConfiguration.builder()
				.property(H2ConnectionOption.DB_CLOSE_DELAY, "-1") // DB연결이 닫혀도 유지되도록 설정
				.property("DATABASE_TO_UPPER", "FALSE")
				.inMemory(H2_DB_NAME) // 데이터베이스 이름
				.url(H2_DB_URL)
				.username(dbUserName)
				.password(dbPassword)
				.build();
		return new H2ConnectionFactory(connectionConfiguration);
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

	@Component
	public class H2Console {
		private Server webServer;

		@EventListener(ContextRefreshedEvent.class)
		public void start() throws java.sql.SQLException {
			this.webServer = Server.createWebServer("-webPort", H2_CONSOLE_PORT, "-tcpAllowOthers").start();
		}

		@EventListener(ContextClosedEvent.class)
		public void stop() {
			this.webServer.stop();
		}
	}
}
