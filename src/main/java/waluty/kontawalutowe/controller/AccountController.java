package waluty.kontawalutowe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import waluty.kontawalutowe.dto.AccountDTO;
import waluty.kontawalutowe.model.Account;
import waluty.kontawalutowe.service.AccountService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public Account createAccount(@RequestParam String firstName, @RequestParam String lastName,
                                 @RequestParam String pesel, @RequestParam BigDecimal initialBalancePLN) {
        return accountService.createAccount(firstName, lastName, pesel, initialBalancePLN);
    }

    @GetMapping("/{pesel}")
    public AccountDTO getAccount(@PathVariable String pesel) {
        return accountService.getAccount(pesel);
    }

    @PostMapping("/exchange")
    public void exchangeCurrency(@RequestParam String pesel, @RequestParam BigDecimal amount,
                                 @RequestParam String fromCurrency, @RequestParam String toCurrency) {
        accountService.exchangeCurrency(pesel, amount, fromCurrency, toCurrency);
    }
}
