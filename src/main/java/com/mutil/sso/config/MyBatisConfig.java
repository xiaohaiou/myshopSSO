package com.mutil.sso.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {"com.mutil.sso.dao"})
public class MyBatisConfig {
	
	private Logger logger  = LogManager.getLogger(MyBatisConfig.class);
	@Resource(name = "dataSource")
	private DataSource dataSource;
	@Value("${mybatis.type-aliases-package}")
	private String typeAliasesPackage;
	@Value("${mybatis.mapper-locations}")
	private String mapperLocations;

	@Bean
	@ConditionalOnMissingBean
	public SqlSessionFactory sqlSessionFactory() {
		try {
			SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
			// dataSource = SpringBeanUtil.getBean(DataSource.class);
			sessionFactory.setDataSource(dataSource);
			sessionFactory.setTypeAliasesPackage(typeAliasesPackage);
			sessionFactory
					.setMapperLocations(new PathMatchingResourcePatternResolver()
							.getResources(mapperLocations));
			return sessionFactory.getObject();
		} catch (Exception e) {
			logger.error("Could not confiure mybatis session factory"+e.getMessage(),e);
			return null;
		}
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(
			SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}


	@Bean
	@ConditionalOnMissingBean
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource);
	}


	@Bean
	public ConfigurationCustomizer configurationCustomizer(){
		return new MybatisPlusCustomizers();
	}

	class MybatisPlusCustomizers implements ConfigurationCustomizer {

		@Override
		public void customize(org.apache.ibatis.session.Configuration configuration) {
			configuration.setJdbcTypeForNull(JdbcType.NULL);
		}
	}

}
