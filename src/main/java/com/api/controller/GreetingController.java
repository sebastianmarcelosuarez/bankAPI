package com.api.controller;

import java.net.URI;
import java.util.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.api.model.Car;
import com.api.model.Greeting;
import com.api.model.User;


import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.firebase.database.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.api.service.FirebaseService;

@RestController
@RequestMapping("/mainapi")
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private FirebaseDatabase firebase = new FirebaseService().getDb();

    @GetMapping("/getgreeting")
    public ResponseEntity<Greeting> getgreeting(@RequestBody String name) {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{name}")
                .buildAndExpand(name)
                .toUri();

        return   ResponseEntity.created(uri).body(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }


    @GetMapping("/users")
    public ResponseEntity<Map<String, User>> getUsers() {
        return   ResponseEntity.accepted().body(getDBUsers());
    }

    private  Map<String, User> getDBUsers() {
        Map<String, User> map = new HashMap<>();
        DatabaseReference ref = firebase.getReference("user");
        final Boolean[] isLoaded = {Boolean.FALSE};

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("init onDataChange");

                ApiFuture< Map<String, User> > valueFuture = ApiFutures.immediateFuture( (Map<String, User>)dataSnapshot.getValue());
                ApiFutures.addCallback(valueFuture, new ApiFutureCallback<Map<String, User>>() {
                    @Override
                    public void onSuccess(Map<String, User> result) {
                        System.out.println("Operation completed with result: " + result.toString());
                        try {
                            map.putAll(valueFuture.get());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }finally {
                            isLoaded[0] = Boolean.TRUE;
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        System.out.println("Operation failed with error: " + t);
                        isLoaded[0] = Boolean.TRUE;
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
                isLoaded[0] = Boolean.TRUE;
            }
        });


        while ( (!isLoaded[0]) ) {
            System.out.println("Loading...");
       }

        return map;
    }

    @PostMapping("/addUser")
    public ResponseEntity< Map<String,User>> addUser() {

        User user = new User();
        User user2 = new User();
        Car car = new Car();
        car.setId(UUID.randomUUID().toString());
        car.setSits(1);
        car.setWheels(4);
        car.setTrademark("Ford");
        car.setName("Fiesta");

        user.setId(UUID.randomUUID().toString());
        user.setName("John");
        user.setLastName("Rambo");

        user2.setId(UUID.randomUUID().toString());
        user2.setName("Ruben");
        user2.setLastName("Rada");
        user2.setCars(List.of(car));

        DatabaseReference ref = firebase.getReference("user");

        Map<String,User> userMap = new HashMap<String,User>();
        userMap.put(user.getId(),user);
        userMap.put(user2.getId(),user2);

        ref.setValueAsync(userMap);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{name}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(uri).body(userMap);
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
