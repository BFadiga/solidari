package br.com.fiap.solidari.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParceiroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveListarParceirosSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/parceiros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void deveBloquearCriacaoDeParceiroSemAutenticacao() throws Exception {
        String corpo = """
                {"nome": "Novo Parceiro", "descricao": "desc", "categoria": "Educação",
                 "badge": "5% OFF", "destaque": false}
                """;

        mockMvc.perform(post("/api/parceiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }
}
