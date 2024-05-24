package insper.store.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import lombok.NonNull;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCreate")
    @CachePut(value = "accountCache", key = "#result.id")
    public Account create(Account in) {
        in.hash(calculateHash(in.password()));
        in.password(null);
        return accountRepository.save(new AccountModel(in)).to();
    }

    public Account fallbackCreate(Account in, Throwable t) {
        return new Account();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackUpdate")
    @Cacheable(value = "accountCache", key = "#id")
    public Account read(@NonNull String id) {
        return accountRepository.findById(id).map(AccountModel::to).orElse(null);
    }

    public Account fallbackUpdate(String id, Throwable t) {
        return new Account();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackUpdate")
    @Cacheable(value = "accountLoginCache", key = "#email")
    public Account login(String email, String password) {
        String hash = calculateHash(password);
        return accountRepository.findByEmailAndHash(email, hash).map(AccountModel::to).orElse(null);
    }

    public Account fallbackLogin(String email, String password, Throwable t) {
        return new Account();
    }

    private String calculateHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            byte[] encoded = Base64.getEncoder().encode(hash);
            return new String(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
}
