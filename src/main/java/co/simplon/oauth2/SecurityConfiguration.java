package co.simplon.oauth2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter{

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http/*.csrf().disable()*/
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/login/**", "/error/**","/np/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login();
        http.authorizeRequests().and().logout().logoutUrl("/logout").logoutSuccessUrl("/");
    }

}

