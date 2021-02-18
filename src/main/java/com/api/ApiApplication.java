package com.api;

import com.api.model.Account;
import com.api.model.Transaction;
import com.api.repository.AccountRepository;
import com.api.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;

@SpringBootApplication
public class ApiApplication {
	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@PostConstruct
	public void initUsers () {
		Account account = new Account(1100,"myUser",1000);
		accountRepository.save(account);
		Transaction transaction = new Transaction(1,"myUser",1000, Date.from(Instant.now()));
		transactionRepository.save(transaction);
	}
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
