package co.simplon.oauth2;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

class MyOAuth2UserService extends DefaultOAuth2UserService {
    private WebClient rest;

    public MyOAuth2UserService(WebClient rest) {
        this.rest = rest;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(request);
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