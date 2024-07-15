package waluty.kontawalutowe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import waluty.kontawalutowe.model.Account;
import waluty.kontawalutowe.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testCreateAccount() throws Exception {
        String pesel = "85123014358";
        mockMvc.perform(post("/api/accounts")
                        .param("firstName", "Kamil")
                        .param("lastName", "Sikorski")
                        .param("pesel", pesel)
                        .param("initialBalancePLN", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesel").value(pesel))
                .andExpect(jsonPath("$.firstName").value("Kamil"))
                .andExpect(jsonPath("$.lastName").value("Sikorski"))
                .andExpect(jsonPath("$.balancePLN").value(1000))
                .andExpect(jsonPath("$.balanceUSD").value(0));
    }

    @Test
    void testGetAccount() throws Exception {
        String pesel = "85123014358";
        Account account = new Account();
        account.setPesel(pesel);
        account.setFirstName("Kamil");
        account.setLastName("Sikorski");
        account.setBalancePLN(BigDecimal.valueOf(1000));
        account.setBalanceUSD(BigDecimal.ZERO);

        accountRepository.save(account);

        mockMvc.perform(get("/api/accounts/{pesel}", pesel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesel").value(pesel))
                .andExpect(jsonPath("$.firstName").value("Kamil"))
                .andExpect(jsonPath("$.lastName").value("Sikorski"))
                .andExpect(jsonPath("$.balancePLN").value(1000))
                .andExpect(jsonPath("$.balanceUSD").value(0));
    }

    @Test
    void testExchangeCurrency() throws Exception {
        String pesel = "85123014358";
        Account account = new Account();
        account.setPesel(pesel);
        account.setFirstName("Kamil");
        account.setLastName("Sikorski");
        account.setBalancePLN(BigDecimal.valueOf(1000));
        account.setBalanceUSD(BigDecimal.ZERO);

        accountRepository.save(account);

        Map<String, Object> response = new HashMap<>();
        response.put("rates", List.of(Map.of("mid", 4)));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts/exchange")
                        .param("pesel", pesel)
                        .param("amount", "100")
                        .param("fromCurrency", "PLN")
                        .param("toCurrency", "USD"))
                .andExpect(status().isOk());

        Account updatedAccount = accountRepository.findById(pesel).orElseThrow();

        assertEquals(BigDecimal.valueOf(900).stripTrailingZeros(), updatedAccount.getBalancePLN().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(25).stripTrailingZeros(), updatedAccount.getBalanceUSD().stripTrailingZeros());
    }
}

