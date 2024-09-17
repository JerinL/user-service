package com.user.service.service;

import com.user.service.model.Users;
import com.user.service.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Users createUser(Users usersReq) {
        return userRepository.save(usersReq);
    }

    public List<Users> getAllUser() {
        return userRepository.findAll();
    }

    public Users getUserById(Integer id) throws Exception {
        return userRepository.findById(id).orElseThrow(()-> new Exception("user Not Found"));
    }

    public Users updateUser(Integer userId,Users userReq) throws Exception {
        Users user = userRepository.findById(userId).orElseThrow(() -> new Exception("User not Found"));
        user.builder()
                .name(userReq.getName())
                .age(userReq.getAge())
                .email(userReq.getEmail());
        return userRepository.save(user);
    }
}
