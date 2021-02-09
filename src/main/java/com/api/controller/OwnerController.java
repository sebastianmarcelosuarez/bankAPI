package com.api.controller;

import java.net.URI;
import java.util.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.api.model.Owner;
import com.api.security.request.AuthRequest;
import com.api.model.Greeting;
;
import com.api.repository.UserRepository;
import com.api.security.util.JwtUtil;
import com.google.api.core.SettableApiFuture;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.api.service.FirebaseService;

@RestController
@RequestMapping("/api_v1")
public class OwnerController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private FirebaseDatabase firebase = new FirebaseService().getDb();
    private JwtUtil jwtUtil = new JwtUtil();
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/authenticate")
    public String generateToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            System.out.println("printing H2 users");
            List<String> users =  userRepository.findAll().stream().map(user -> user.toString()).collect(Collectors.toList());
            System.out.println(users);
            System.out.println("printing authRequest");
            System.out.println("username: " + authRequest.getUserName());
            System.out.println("password: " + authRequest.getPassword());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("invalid username or password");
        }

        return jwtUtil.generateToken(authRequest.getUserName());
    }

    @GetMapping("/getgreeting")
    public ResponseEntity<Greeting> getgreeting(@RequestBody String name) {
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{name}")
                .buildAndExpand(name)
                .toUri();

        return ResponseEntity.created(uri).body(new Greeting(counter.incrementAndGet(), String.format(template, name)));
    }


    @GetMapping("/users")
    public ResponseEntity<Map<String, Owner>> getUsers() {
        return ResponseEntity.accepted().body(getDBUsers());
    }

    private Map<String, Owner> getDBUsers() {
        Map<String, Owner> map = new HashMap<>();
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

        Map<String, Owner> result;
        try {
            while (!future.isDone() && !future.isCancelled()) {
                System.out.println("waiting db result");
            }
            DataSnapshot dataSnapshot = future.get();
            result = (Map<String, Owner>) dataSnapshot.getValue();
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
    public ResponseEntity<Map<String, Owner>> addUser(@RequestBody String payload) {
        GsonBuilder builder = new GsonBuilder();
        Map<String, Owner> userMap = new HashMap<String, Owner>();
        Gson gson = builder.create();
        Owner owner = gson.fromJson(payload, Owner.class);

        DatabaseReference ref = firebase.getReference("users");
        owner.setId(ref.push().getKey());
        userMap.put(owner.getId(), owner);
        ref.child(owner.getId()).setValueAsync(owner);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{name}")
                .buildAndExpand(owner.getId())
                .toUri();

        return ResponseEntity.created(uri).body(userMap);
    }

    // update user
    @PutMapping("/updateUser")
    public ResponseEntity updateUser(@RequestParam(value = "userId", defaultValue = "") String userId, @RequestBody String payload) {
        Map<String, Object> userUpdatedMap = new HashMap<>();
        System.out.println("user id: " + userId);
        //1 buscar si existe el usuario en la db
        DatabaseReference ref = firebase.getReference("users");

        final SettableApiFuture<DataSnapshot> future = SettableApiFuture.create();
        Query query = ref.orderByChild("id").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("getting datasnapshot");
                future.set(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Map<String, Object> result;
        try {
            while (!future.isDone() && !future.isCancelled()) {
                System.out.println("waiting db result");
            }
            DataSnapshot dataSnapshot = future.get();
            result = (Map<String, Object>) dataSnapshot.getValue();
            if (result == null || result.size() == 0) {
                System.out.println("user not found");
                return null;
            }
            System.out.println("USER FOUND!!");
            System.out.println(result.toString());

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(payload);

            userUpdatedMap = updateUserOnFirebase(result, json);

        } catch (InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }

        return ResponseEntity.ok().body(userUpdatedMap);
    }

    /**
     * Updates user on firebase
     *
     * @param dbUser  the user found on DB
     * @param payload new info for update the user on json format
     * @return
     */
    private Map<String, Object> updateUserOnFirebase(Map<String, Object> dbUser, JSONObject payload) {
        dbUser.forEach((k, v) -> {
            System.out.println("for each");
            if (payload.containsKey("newName")) ((Map) v).put("name", payload.get("newName"));
            if (payload.containsKey("newLastName")) ((Map) v).put("lastName", payload.get("newLastName"));

            DatabaseReference ref = firebase.getReference("users");
            Map<String, Object> mapToDB = new HashMap<>();
            mapToDB.put(k, v);
            ref.updateChildrenAsync(mapToDB);
        });
        return dbUser;
    }


    /**
     * deletes user on firebase
     *
     * @param dbUser the user found on DB
     * @return
     */
    private void deleteUserOnFirebase(Map<String, Object> dbUser) {
        dbUser.forEach((k, v) -> {
            DatabaseReference ref = firebase.getReference("users");
            Map<String, Object> mapToDB = new HashMap<>();
            mapToDB.put(k, null);
            ref.updateChildrenAsync(mapToDB);
        });

    }

    /**
     * Deletes user handler
     *
     * @param userId: Id of user to delete
     * @return
     */
    @DeleteMapping("/deleteUser")
    public ResponseEntity deleteUser(@RequestParam(value = "userId", defaultValue = "") String userId) {
        System.out.println("user id: " + userId);
        //1 buscar si existe el usuario en la db
        DatabaseReference ref = firebase.getReference("users");

        final SettableApiFuture<DataSnapshot> future = SettableApiFuture.create();
        Query query = ref.orderByChild("id").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("getting datasnapshot");
                future.set(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Map<String, Object> result;
        try {
            while (!future.isDone() && !future.isCancelled()) {
                System.out.println("waiting db result");
            }
            DataSnapshot dataSnapshot = future.get();
            result = (Map<String, Object>) dataSnapshot.getValue();
            if (result == null || result.size() == 0) {
                System.out.println("user not found");
                return ResponseEntity.notFound().build();
            }
            System.out.println("USER FOUND!!");
            System.out.println(result.toString());
            deleteUserOnFirebase(result);

        } catch (InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }

        return ResponseEntity.ok().body("User Deleted");
    }
}
