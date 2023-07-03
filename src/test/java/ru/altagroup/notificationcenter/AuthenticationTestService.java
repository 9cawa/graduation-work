package ru.altagroup.notificationcenter;

import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class AuthenticationTestService {

    public AccessTokenResponse auth(String login, String password) {
        RestTemplate testRestTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", "altacloud-client-dev");
        form.add("grant_type", "password");
        form.add("username", login);
        form.add("password", password);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        return testRestTemplate
                .exchange(
                        "https://cloud.alta-group.ru/iam/auth/realms/altacloud-dev/protocol/openid-connect/token",
                        HttpMethod.POST,
                        entity,
                        AccessTokenResponse.class)
                .getBody();
    }

    @Data
    public static class AccessTokenResponse {
        private String access_token;
        private Long expires_in;
        private String refresh_token;
        private Long refresh_expires_in;
        private String token_type;
        private String not_before_policy;
        private String session_state;
        private String scope;
    }
}
