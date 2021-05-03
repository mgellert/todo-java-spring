package com.mgellert.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;

import static com.mgellert.todo.Utils.CSRF_COOKIE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SecurityTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void testAccessingUserDataWithoutAuthentication() throws Exception {
        mvc.perform(get("/user")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testOAuth2RedirectToGithub() throws Exception {
        MvcResult mvcResult = mvc.perform(get("/oauth2/authorization/github")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andReturn();

        String location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        assertTrue(location.startsWith("https://github.com/login/oauth/authorize"));
    }

    @Test
    void testLogout() throws Exception {
        MvcResult mvcResult = mvc.perform(get("/user")
                .with(oidcLogin())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Cookie csrfCookie = mvcResult.getResponse().getCookie(CSRF_COOKIE);
        assertNotNull(csrfCookie);

        mvc.perform(post("/logout")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue()))
                .andExpect(status().isNoContent());
    }

}
