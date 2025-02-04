package com.sos.payment_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MercadoPagoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MercadoPagoService mercadoPagoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreatePixPaymentSuccess() throws Exception {
        // Resposta simulada do MercadoPago API
        String mockResponse = "{ \"id\": \"123456\", \"point_of_interaction\": { \"transaction_data\": { \"qr_code_base64\": \"mockedBase64QRCode\" } } }";
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Configura mock para o ObjectMapper
        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode mockPointOfInteractionNode = mock(JsonNode.class);
        JsonNode mockTransactionDataNode = mock(JsonNode.class);

        // Configurando cada nível do JSON retornado
        when(objectMapper.readTree(mockResponse)).thenReturn(mockJsonNode);
        when(mockJsonNode.get("id").asText()).thenReturn("123456");

        // Mockando os objetos dentro de "point_of_interaction"
        when(mockJsonNode.get("point_of_interaction")).thenReturn(mockPointOfInteractionNode);
        when(mockPointOfInteractionNode.get("transaction_data")).thenReturn(mockTransactionDataNode);
        when(mockTransactionDataNode.get("qr_code_base64").asText()).thenReturn("mockedBase64QRCode");

        // Executa o método
        String result = mercadoPagoService.createPixPayment(100.50);

        // Verifica o resultado
        assertNotNull(result);
        assertEquals("mockedBase64QRCode", result);

        // Verifica se o RestTemplate foi chamado corretamente
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }


    @Test
    public void testCreatePixPaymentFailure() throws Exception {
        // Configura mock para o RestTemplate com erro
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Executa o método
        String result = mercadoPagoService.createPixPayment(100.50);

        // Verifica que o resultado é null quando há erro
        assertNull(result);

        // Verifica se o RestTemplate foi chamado corretamente
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }


     @Test
    public void testFetchPaymentDetailsSuccess() {
        // Configura mock para o RestTemplate
        String mockResponse = "{ \"id\": \"123456\", \"status\": \"approved\" }";
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Executa o método
        String result = mercadoPagoService.fetchPaymentDetails("123456");

        // Verifica o resultado
        assertNotNull(result);
        assertTrue(result.contains("123456"));

        // Verifica se o RestTemplate foi chamado corretamente
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class));
    }

    @Test
    public void testFetchPaymentDetailsFailure() {
        // Configura mock para o RestTemplate com erro
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Executa o método
        String result = mercadoPagoService.fetchPaymentDetails("123456");

        // Verifica que o resultado é null quando há erro
        assertNull(result);

        // Verifica se o RestTemplate foi chamado corretamente
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class));
    }
}
