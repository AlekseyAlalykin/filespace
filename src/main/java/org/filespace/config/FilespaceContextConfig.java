package org.filespace.config;


import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.filespace")
public class FilespaceContextConfig {


    @Bean
    public Hibernate5Module datatypeHibernateModule() {
        return new Hibernate5Module();
    }

    /*
    @Autowired
    private Environment env;

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());

        return em.getObject();
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("driver_class_name"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("connection.username"));
        dataSource.setPassword(env.getProperty("connection.password"));
        dataSource.setSchema(env.getProperty("default_schema"));
        return dataSource;
    }


    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public FilespaceDAO filespaceDAO(){
        return new FilespaceDAO();
    }
    */

}
