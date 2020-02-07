package co.simplon.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;


@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/login/**", "/error/**", "/np/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().and().logout().logoutUrl("/logout").logoutSuccessUrl("/")
                .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
    }

    @Bean
    public WebClient rest(ClientRegistrationRepository clients, OAuth2AuthorizedClientRepository authz) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clients, authz);
        return WebClient.builder()
                .filter(oauth2).build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(WebClient rest) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return new MyOAuth2UserService(delegate, rest);
    }
}

class MyOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private OAuth2UserService delegate;
    private WebClient rest;

    public MyOAuth2UserService(DefaultOAuth2UserService delegate,
                               WebClient rest) {
        this.delegate = delegate;
        this.rest = rest;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = delegate.loadUser(request);
        if (!"github".equals(request.getClientRegistration().getRegistrationId())) {
            return user;
        }
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient
                (request.getClientRegistration(), user.getName(), request.getAccessToken());
        String url = user.getAttribute("organizations_url");
        List<Map<String, Object>> orgs = rest.get().uri(url)
                .attributes(oauth2AuthorizedClient(client))
                .retrieve()
                .bodyToMono(List.class)
                .block();
        System.err.println("orgs:" + orgs);
        for (Map<String, Object> org : orgs) {
            System.err.println("Checking org:" + org);
            if ("simplonco".equals(org.get("login"))) {
                System.err.println("ADMIN!!!!");
                return new DefaultOAuth2User(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER, ROLE_ADMIN"),
                        user.getAttributes(),"login");
            }
        }
        return new DefaultOAuth2User(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"),
                user.getAttributes(),"login");

        //throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Not in Simplon Team", ""));
    }
}