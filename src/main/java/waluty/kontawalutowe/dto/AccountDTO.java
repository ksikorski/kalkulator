package waluty.kontawalutowe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountDTO {
    private String pesel;
    private String firstName;
    private String lastName;
    private BigDecimal balancePLN;
    private BigDecimal balanceUSD;
}