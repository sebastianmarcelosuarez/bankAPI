package com.api.controller;

import com.api.model.Account;

import com.api.model.Transaction;
import com.api.repository.AccountRepository;

import com.api.repository.TransactionRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;


@RestController
public class BankController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Gson gson = new Gson();

    @GetMapping("/getgreeting")
    public ResponseEntity<String> getgreeting(@RequestBody String name) {
        return ResponseEntity.accepted().body("Welcome to BankAPI");
    }

    @GetMapping("/accounts")
    public ResponseEntity<Account> getAccounts() {
        return ResponseEntity.accepted().body(accountRepository.findById(1100).get());
    }

    @GetMapping("/transactions")
    public String getTransactions() {
        return gson.toJson(transactionRepository.findAll());
    }

    @PostMapping("/modify")
    public ResponseEntity<String> modify(@RequestBody Integer myValue) {


        Integer integerValue = Integer.valueOf(myValue);
        System.out.println("modify data");
        System.out.println(integerValue) ;



        Account account = accountRepository.findById(1100).get();
        Integer result = integerValue + account.getValue() ;

            if (result < 0 ) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST);
            } else {
                account.setValue(result);
                accountRepository.save(account);

                Random random = new Random();
                int number = random.nextInt(10000000);

                Transaction transaction = new Transaction(number,"myUser",integerValue, Date.from(Instant.now()));
                transactionRepository.save(transaction);
                ResponseEntity.ok();
            }

        return ResponseEntity.accepted().body("successS");
    }

}
