package ltd.newbee.mall.config.seata;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.config.properties.DruidProperties;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@ConditionalOnProperty(value = "transactional.mode", havingValue = "seata")
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Configuration
@MapperScan(basePackages = "ltd.newbee.mall.slave.dao", sqlSessionFactoryRef = "slaveSqlSessionFactory")
public class Db2DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.druid.slave")
    public DataSource slaveDataSource(DruidProperties druidProperties) {
        DruidDataSource build = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(build);
    }


    /**
     * @param datasource 数据源
     * @return SqlSessionFactory
     * @Primary 默认SqlSessionFactory
     */
    @Bean(name = "slaveSqlSessionFactory")
    public SqlSessionFactory slaveSqlSessionFactory(@Qualifier("slaveDataSource") DataSource datasource,
                                                    Interceptor interceptor) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(datasource);
        // mybatis扫描xml所在位置
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:slavemapper/*.xml"));
        bean.setTypeAliasesPackage("ltd.**.slave.entity");
        bean.setPlugins(interceptor);
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setLogicDeleteField("isDeleted");
        dbConfig.setLogicDeleteValue("1");
        dbConfig.setLogicNotDeleteValue("0");
        globalConfig.setDbConfig(dbConfig);
        bean.setGlobalConfig(globalConfig);
        log.info("slaveDataSource 配置成功");
        return bean.getObject();
    }

    @Bean(name = "slaveTransactionManager")
    public DataSourceTransactionManager slaveTransactionManager(@Qualifier("slaveDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
