package com.api.controller;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import com.api.model.Greeting;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/mainapi")
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/getgreeting")
    public ResponseEntity<Greeting> getgreeting(@RequestBody String name) {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{name}")
                .buildAndExpand(name)
                .toUri();

        return   ResponseEntity.created(uri).body(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }

    @PutMapping("/putgreeting")
    public Greeting putgreeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @PostMapping("/postgreeting")
    public Greeting postgreeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @DeleteMapping("/deletegreeting")
    public Greeting deletegreeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
