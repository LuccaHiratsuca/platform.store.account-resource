package insper.store.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public AccountModel create(Account in) {
        return accountRepository.save(new AccountModel(in)).to();
    }
    
}
