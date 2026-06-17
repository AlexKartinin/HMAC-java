package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.controller.HmacController;
import ru.yandex.practicum.service.HmacService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HmacController.class)
class ServerHMACTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HmacService hmacService;

    @MockBean
    private AppConfig appConfig;

    @Test
    void signWithValidMsg() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);
        when(hmacService.sign("hello")).thenReturn("validSignatureBase64url");

        mockMvc.perform(post("/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msg\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature").value("validSignatureBase64url"));
    }

    @Test
    void signWithoutMsg() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);

        mockMvc.perform(post("/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"other\":\"value\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_msg"));
    }

    @Test
    void signWithEmptyMsg() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);

        mockMvc.perform(post("/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msg\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_msg"));
    }

    @Test
    void signWithOversizedMsg() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(5L);

        mockMvc.perform(post("/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msg\":\"toolong\"}"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").value("payload_too_large"));
    }

    @Test
    void signWithoutContentType() throws Exception {
        mockMvc.perform(post("/sign")
                        .content("{\"msg\":\"hello\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("unsupported_media_type"));
    }

    @Test
    void verifyWithValidSignature() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);
        when(hmacService.verify("hello", "validSig")).thenReturn(true);

        mockMvc.perform(post("/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msg\":\"hello\",\"signature\":\"validSig\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void verifyWithWrongSignature() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);
        when(hmacService.verify(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msg\":\"hello\",\"signature\":\"wrongSig\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void verifyWithInvalidBase64UrlSignature() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);

        mockMvc.perform(post("/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msg\":\"hello\",\"signature\":\"@@@\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_signature_format"));
    }

    @Test
    void verifyWithoutSignature() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);

        mockMvc.perform(post("/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"msg\":\"hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_signature_format"));
    }

    @Test
    void verifyWithoutMsg() throws Exception {
        when(appConfig.getMaxMsgSizeBytes()).thenReturn(1_048_576L);

        mockMvc.perform(post("/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signature\":\"validSig\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_msg"));
    }
}
