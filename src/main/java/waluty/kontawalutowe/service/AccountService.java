package waluty.kontawalutowe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import waluty.kontawalutowe.dto.AccountDTO;
import waluty.kontawalutowe.exception.AccountAlreadyExistsException;
import waluty.kontawalutowe.model.Account;
import waluty.kontawalutowe.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Account createAccount(String firstName, String lastName, String pesel, BigDecimal initialBalancePLN) {
        if (accountRepository.existsById(pesel)) {
            throw new AccountAlreadyExistsException("Account with PESEL " + pesel + " already exists");
        }
        if (!isAdult(pesel)) {
            throw new IllegalArgumentException("User must be an adult");
        }

        Account account = new Account();
        account.setFirstName(firstName);
        account.setLastName(lastName);
        account.setPesel(pesel);
        account.setBalancePLN(initialBalancePLN);
        account.setBalanceUSD(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    public AccountDTO getAccount(String pesel) {
        Account account = accountRepository.findById(pesel)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return new AccountDTO(
                account.getPesel(),
                account.getFirstName(),
                account.getLastName(),
                account.getBalancePLN(),
                account.getBalanceUSD()
        );
    }

    public void exchangeCurrency(String pesel, BigDecimal amount, String fromCurrency, String toCurrency) {
        Account account = getAccountInternal(pesel);
        BigDecimal exchangeRate = getExchangeRate(fromCurrency, toCurrency);

        if (fromCurrency.equals("PLN") && toCurrency.equals("USD")) {
            BigDecimal usdAmount = amount.multiply(exchangeRate);
            if (account.getBalancePLN().compareTo(amount) >= 0) {
                account.setBalancePLN(account.getBalancePLN().subtract(amount));
                account.setBalanceUSD(account.getBalanceUSD().add(usdAmount));
            } else {
                throw new IllegalArgumentException("Insufficient funds in PLN");
            }
        } else if (fromCurrency.equals("USD") && toCurrency.equals("PLN")) {
            BigDecimal plnAmount = amount.multiply(exchangeRate);
            if (account.getBalanceUSD().compareTo(amount) >= 0) {
                account.setBalanceUSD(account.getBalanceUSD().subtract(amount));
                account.setBalancePLN(account.getBalancePLN().add(plnAmount));
            } else {
                throw new IllegalArgumentException("Insufficient funds in USD");
            }
        } else {
            throw new IllegalArgumentException("Invalid currency pair");
        }
        accountRepository.save(account);
    }

    private Account getAccountInternal(String pesel) {
        return accountRepository.findById(pesel)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String url = "https://api.nbp.pl/api/exchangerates/rates/a/" + (fromCurrency.equals("PLN") ? toCurrency : fromCurrency) + "/?format=json";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rates = (List<Map<String, Object>>) response.get("rates");
        BigDecimal rateValue = new BigDecimal(rates.get(0).get("mid").toString());

        return fromCurrency.equals("PLN") ? BigDecimal.ONE.divide(rateValue, 4, BigDecimal.ROUND_HALF_UP) : rateValue;
    }

    private boolean isAdult(String pesel) {
        int year = Integer.parseInt(pesel.substring(0, 2));
        int month = Integer.parseInt(pesel.substring(2, 4));
        int day = Integer.parseInt(pesel.substring(4, 6));

        // Determine century
        int century;
        if (month >= 1 && month <= 12) {
            century = 1900;
        } else if (month >= 21 && month <= 32) {
            century = 2000;
            month -= 20;
        } else if (month >= 41 && month <= 52) {
            century = 2100;
            month -= 40;
        } else if (month >= 61 && month <= 72) {
            century = 2200;
            month -= 60;
        } else if (month >= 81 && month <= 92) {
            century = 1800;
            month -= 80;
        } else {
            throw new IllegalArgumentException("Invalid PESEL number");
        }

        year += century;

        LocalDate birthDate = LocalDate.of(year, month, day);
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}
