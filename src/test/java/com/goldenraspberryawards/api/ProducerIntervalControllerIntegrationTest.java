package com.goldenraspberryawards.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldenraspberryawards.api.dto.ProducerIntervalRecord;
import com.goldenraspberryawards.api.dto.ProducerIntervalResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProducerIntervalControllerIntegrationTest
{

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ProducerIntervalResponse getProducerIntervalsResponse() throws Exception
    {
        String json = mockMvc.perform(get("/api/producers/intervals").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(json, ProducerIntervalResponse.class);
    }

    @Test
    @DisplayName("GET /api/producers/intervals retorna 200 e JSON com arrays min e max")
    void getProducerIntervals_returnsOkWithMinMax() throws Exception
    {
        ResultActions result = mockMvc.perform(get("/api/producers/intervals").accept(MediaType.APPLICATION_JSON));

        result
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.min").isArray())
                .andExpect(jsonPath("$.max").isArray());
    }

    @Test
    @DisplayName("Registros de menor intervalo conferem com CSV de teste: Producer A e B com intervalo 1")
    void getProducerIntervals_minMatchesTestData() throws Exception
    {
        ProducerIntervalResponse response = getProducerIntervalsResponse();
        assertThat(response.min()).hasSize(2);

        assertThat(response.min()).anyMatch(r ->
                "Producer A".equals(r.producer()) && r.interval() == 1 && r.previousWin() == 2000 && r.followingWin() == 2001);
        assertThat(response.min()).anyMatch(r ->
                "Producer B".equals(r.producer()) && r.interval() == 1 && r.previousWin() == 2018 && r.followingWin() == 2019);
    }

    @Test
    @DisplayName("Registro de maior intervalo confere com CSV de teste: Producer C com intervalo 9")
    void getProducerIntervals_maxMatchesTestData() throws Exception
    {
        ProducerIntervalResponse response = getProducerIntervalsResponse();
        assertThat(response.max()).hasSize(1);
        ProducerIntervalRecord maxRecord = response.max().get(0);
        assertThat(maxRecord.producer()).isEqualTo("Producer C");
        assertThat(maxRecord.interval()).isEqualTo(9);
        assertThat(maxRecord.previousWin()).isEqualTo(1990);
        assertThat(maxRecord.followingWin()).isEqualTo(1999);
    }
}
