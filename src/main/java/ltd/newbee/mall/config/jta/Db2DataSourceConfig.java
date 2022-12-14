// package ltd.newbee.mall.config.jta;
//
// import com.alibaba.druid.pool.DruidDataSource;
// import com.alibaba.druid.pool.xa.DruidXADataSource;
// import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
// import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
// import com.baomidou.mybatisplus.core.config.GlobalConfig;
// import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
// import com.zaxxer.hikari.HikariDataSource;
// import lombok.extern.slf4j.Slf4j;
// import ltd.newbee.mall.config.properties.DruidProperties;
// import org.apache.ibatis.plugin.Interceptor;
// import org.apache.ibatis.session.SqlSessionFactory;
// import org.mybatis.spring.annotation.MapperScan;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.EnableAspectJAutoProxy;
// import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
// import org.springframework.jdbc.datasource.DataSourceTransactionManager;
// import org.springframework.transaction.annotation.EnableTransactionManagement;
// import org.springframework.util.StringUtils;
//
// import javax.sql.DataSource;
//
// @Slf4j
// @ConditionalOnProperty(value = "transactional.mode", havingValue = "jta")
// @EnableConfigurationProperties(MybatisPlusProperties.class)
// @EnableTransactionManagement
// @EnableAspectJAutoProxy
// @Configuration
// @MapperScan(basePackages = "ltd.newbee.mall.slave.dao", sqlSessionFactoryRef = "slaveSqlSessionFactory")
// public class Db2DataSourceConfig {
//
//     @Bean
//     @ConfigurationProperties("spring.datasource.druid.slave")
//     public DataSource slaveDataSource(DruidProperties druidProperties) {
//         DruidXADataSource dataSource = druidProperties.dataSource(new DruidXADataSource());
//         dataSource.setUrl("jdbc:mysql://sh-cynosdbmysql-grp-6159f0n2.sql.tencentcdb.com:22114/newbee_mall_db2?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8");
//         dataSource.setUsername("wayn");
//         dataSource.setPassword("admin@@123456");
//         dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//         AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
//         atomikosDataSourceBean.setXaDataSourceClassName("com.alibaba.druid.pool.xa.DruidXADataSource");
//         atomikosDataSourceBean.setUniqueResourceName("slave-xa");
//         atomikosDataSourceBean.setXaDataSource(dataSource);
//         atomikosDataSourceBean.setPoolSize(5);
//         atomikosDataSourceBean.setMaxPoolSize(20);
//         atomikosDataSourceBean.setTestQuery("select 1");
//         return atomikosDataSourceBean;
//     }
//
//
//     /**
//      * @param datasource 数据源
//      * @return SqlSessionFactory
//      * @Primary 默认SqlSessionFactory
//      */
//     @Bean(name = "slaveSqlSessionFactory")
//     public SqlSessionFactory slaveSqlSessionFactory(@Qualifier("slaveDataSource") DataSource datasource,
//                                                      Interceptor interceptor,
//                                                      MybatisPlusProperties properties) throws Exception {
//         MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
//         bean.setDataSource(datasource);
//         // mybatis扫描xml所在位置
//         bean.setMapperLocations(new PathMatchingResourcePatternResolver()
//                 .getResources("classpath*:slavemapper/*.xml"));
//         if (properties.getConfigurationProperties() != null) {
//             bean.setConfigurationProperties(properties.getConfigurationProperties());
//         }
//         if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
//             bean.setTypeAliasesPackage(properties.getTypeAliasesPackage());
//         }
//         bean.setPlugins(interceptor);
//         GlobalConfig globalConfig = properties.getGlobalConfig();
//         bean.setGlobalConfig(globalConfig);
//         log.info("slaveDataSource 配置成功");
//         return bean.getObject();
//     }
//
//     @Bean(name = "slaveTransactionManager")
//     public DataSourceTransactionManager slaveTransactionManager(@Qualifier("slaveDataSource") DataSource dataSource) {
//         return new DataSourceTransactionManager(dataSource);
//     }
//
// }
