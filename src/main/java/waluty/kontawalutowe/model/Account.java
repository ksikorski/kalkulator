package waluty.kontawalutowe.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Account {
    private String pesel;
    private String firstName;
    private String lastName;
    private BigDecimal balancePLN;
    private BigDecimal balanceUSD;
}
