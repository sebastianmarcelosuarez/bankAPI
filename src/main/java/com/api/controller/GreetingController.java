package com.api.controller;

import java.net.URI;
import java.util.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import com.api.model.Greeting;
import com.api.model.User;


import com.api.service.OnGetDataListener;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.core.SettableApiFuture;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
        DatabaseReference ref = firebase.getReference("users");

        final SettableApiFuture<DataSnapshot> future = SettableApiFuture.create();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              future.set(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });

        Map<String, User> result;
        try {
            while (!future.isDone() && !future.isCancelled()){
                System.out.println("waiting db result");
            }
            DataSnapshot dataSnapshot =  future.get();
            result = (Map<String, User>) dataSnapshot.getValue();
            System.out.println("info added to result:" + result.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = new HashMap<>();
        } catch (ExecutionException e) {
            e.printStackTrace();
            result = new HashMap<>();
        }

        return result;
    }

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
    @PutMapping("/updateUser")
    public ResponseEntity updateUser(@RequestParam(value = "userId", defaultValue = "") String name, @RequestBody String payload) {

        //1 buscar si existe el usuario en la db
        DatabaseReference ref = firebase.getReference("users");
        //ref
        // 2 actualizar
        //3 informar resultado
        return null;
    }

    @DeleteMapping("/deletegreeting")
    public Greeting deletegreeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
