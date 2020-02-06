package com.colorseq.abrowse;


/*import com.colorseq.cscore.dao.user.RoleFlagType;
import com.colorseq.cscore.dao.user.UserService;*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    UserDetailsService userService() {
        return new UserService();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

//        http
//                .csrf().disable()
//                .authorizeRequests()
//                .anyRequest().permitAll();

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/webjars/**", "/css/**",
                        "/images/**", "/js/**", "**/favicon.ico",
                        "/gmap/**",
                        "/register", "/about").permitAll()
                .antMatchers("/config/**", "/admin/**").hasRole(RoleFlagType.ADMIN)
                .anyRequest().authenticated() // 除上述例外之外所有请求必须登陆后访问
                .and()
                .formLogin()
                .loginPage("/login")
                .failureUrl("/login?error")
                .permitAll()//登录界面，错误界面可以直接访问
                .and()
                .logout().logoutSuccessUrl("/")
                .permitAll();//注销请求可直接访问
    }
}
