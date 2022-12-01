package org.filespace.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("userDetailsServiceImpl")
    UserDetailsService userDetailsService;

    /*
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("{noop}password").roles("USER");
        //auth.userDetailsService(userDetailsService);
    }

     */

    @Configuration
    @Order(1)
    public static class BasicHttpSecurityConfig extends WebSecurityConfigurerAdapter{

        @Override
        protected void configure(HttpSecurity http) throws Exception {
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
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()

                     */
                        .httpBasic();

        }
    }

    @Configuration
    @Order(2)
    public static class FormLoginSecurityConfig extends WebSecurityConfigurerAdapter{

        @Override
        protected void configure(HttpSecurity http) throws Exception {
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

    /*
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

     */

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

}
