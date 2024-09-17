package com.user.service.controller;

import com.user.service.model.Users;
import com.user.service.service.UserService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping()
    public Users createUser(@RequestBody Users usersReq){
        return  userService.createUser(usersReq);
    }

    @GetMapping()
    public List<Users> getAllUser(){
        return userService.getAllUser();
    }

    @GetMapping("/{id}")
    public  Users getUserById(@PathVariable("id") Integer id) throws Exception {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public Users updateUser(@PathVariable("id") Integer id, @RequestBody Users userReq) throws Exception {
        return userService.updateUser(id,userReq);
    }

}
