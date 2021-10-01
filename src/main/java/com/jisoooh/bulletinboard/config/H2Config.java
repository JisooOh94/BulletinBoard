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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Configuration
public class H2Config {
	private static final String H2_DRIVER_CLASS = "org.h2.Driver";
	private static final String H2_DB_URL = "jdbc:h2:~/test";
	private static final String H2_DDL_PATH = "scheme/DDL.sql";
	private static final String H2_DML_PATH = "scheme/DML.sql";
	private static final String H2_CONSOLE_PORT = "8081";

	@Value("${database.h2.userName}")
	private String dbUserName;
	@Value("${database.h2.password}")
	private String dbPassword;

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
		return new NamedParameterJdbcTemplate(dataSource());
	}

	@Bean
	public DriverManagerDataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(H2_DRIVER_CLASS);
		dataSource.setUrl(H2_DB_URL);
		dataSource.setUsername(dbUserName);
		dataSource.setPassword(dbPassword);

		// schema init
		Resource initSchema = new ClassPathResource(H2_DDL_PATH);
		Resource initData = new ClassPathResource(H2_DML_PATH);
		DatabasePopulator databasePopulator = new ResourceDatabasePopulator(initSchema, initData);
		DatabasePopulatorUtils.execute(databasePopulator, dataSource);

		return dataSource;
	}

	@Component
	public class H2Console {
		private Server webServer;

		@EventListener(ContextRefreshedEvent.class)
		public void start() throws java.sql.SQLException {
			this.webServer = org.h2.tools.Server.createWebServer("-webPort", H2_CONSOLE_PORT, "-tcpAllowOthers").start();
		}

		@EventListener(ContextClosedEvent.class)
		public void stop() {
			this.webServer.stop();
		}
	}
}
