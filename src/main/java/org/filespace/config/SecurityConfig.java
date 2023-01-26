package org.filespace.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.util.Properties;

@Component
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static String propertiesPath = "classpath:session.properties";

    private static int maximumSessions;
    static {

        try {
            FileInputStream fileInputStream = new FileInputStream(
                    ResourceUtils.getFile(propertiesPath));
            Properties properties = new Properties();
            properties.load(fileInputStream);

            maximumSessions = Integer.parseInt(properties.getProperty("maximum-sessions"));

            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();

            maximumSessions = 1;
        }
    }

    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    private ApplicationContext context;
    /*
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("USER");
        //auth.userDetailsService(userDetailsService);
    }

     */

    @Configuration
    @Order(1)
    public class BasicHttpSecurityConfig extends WebSecurityConfigurerAdapter{

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            SessionRegistry sessions = context.getBean(SessionRegistry.class);

            http.sessionManagement().maximumSessions(5).
                    maxSessionsPreventsLogin(false).sessionRegistry(sessions);

            http
                    .csrf()
                        .disable()
                    .antMatcher("/api/**")
                    .authorizeRequests()
                        .antMatchers(HttpMethod.POST,"/api/users")
                            .permitAll()
                    .and()
                    .authorizeRequests()
                        .antMatchers("/api/**").authenticated()
                    .and()
                    /*
                        .sessionManagement()
                            .maximumSessions(5)
                                .sessionRegistry(sessions).and()
                    .and()

                     */
                        .httpBasic();

        }
    }

    @Configuration
    @Order(2)
    public class FormLoginSecurityConfig extends WebSecurityConfigurerAdapter{

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            SessionRegistry sessions = context.getBean(SessionRegistry.class);

            http.sessionManagement().maximumSessions(5).
                    maxSessionsPreventsLogin(false).sessionRegistry(sessions);

            http
                    .csrf()
                        .disable()
                    .formLogin()
                        .loginPage("/login")
                            .defaultSuccessUrl("/test")
                    .and().logout()
                        .logoutUrl("/logout")
                            .deleteCookies("JSESSIONID")
                                .logoutSuccessUrl("/login")
                    .and().authorizeRequests()
                        .antMatchers(HttpMethod.GET,"/registration", "/login")
                            .permitAll()
                    .and().authorizeRequests()
                        .anyRequest()
                            .authenticated();
        }

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    protected DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        SessionRegistry sessionRegistry = new SessionRegistryImpl();
        return sessionRegistry;
    }

    @Bean
    public RegisterSessionAuthenticationStrategy registerSessionAuthStr() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }
}
