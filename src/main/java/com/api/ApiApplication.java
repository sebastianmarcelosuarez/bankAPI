package com.api;

import com.api.entity.User;
import com.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class ApiApplication {
	@Autowired
	private UserRepository userRepository;

	@PostConstruct
	public void initUsers () {
		List<User> users = Stream.of(
				new User(101, "peplo", "password", "email@email1"),
				new User(102, "baco", "passwordbaco", "email@email22")
		).collect(Collectors.toList());
		userRepository.saveAll(users);
	}
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
