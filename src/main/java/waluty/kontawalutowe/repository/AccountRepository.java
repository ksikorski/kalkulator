package waluty.kontawalutowe.repository;


import org.springframework.stereotype.Repository;
import waluty.kontawalutowe.model.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class AccountRepository {
    private final Map<String, Account> accountStorage = new HashMap<>();

    public Optional<Account> findById(String pesel) {
        return Optional.ofNullable(accountStorage.get(pesel));
    }

    public Account save(Account account) {
        accountStorage.put(account.getPesel(), account);
        return account;
    }

    public boolean existsById(String pesel) {
        return accountStorage.containsKey(pesel);
    }
}
