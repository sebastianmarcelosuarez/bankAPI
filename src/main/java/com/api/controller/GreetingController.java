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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.google.common.util.concurrent.MoreExecutors.*;

import com.api.service.FirebaseService;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

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
                }, directExecutor());
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

    //TODO agregar nuevo usuario con Json, ver si usar post o put! uno crea, otro updatea.. El user puede incluir cars!
    @PostMapping("/addUser")
    public ResponseEntity< Map<String,User>> addUser(@RequestBody String payload) {
        GsonBuilder builder = new GsonBuilder();
        Map<String,User> userMap = new HashMap<String,User>();
        Gson gson = builder.create();

        User user = gson.fromJson(payload, User.class);

        DatabaseReference ref = firebase.getReference("users");

        user.setId(ref.push().getKey());
        userMap.put(user.getId(),user);



        ref.child(user.getId()).setValueAsync(user);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{name}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(uri).body(userMap);
    }

    // update user
    @PutMapping("/postgreeting")
    public Greeting postgreeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @DeleteMapping("/deletegreeting")
    public Greeting deletegreeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
