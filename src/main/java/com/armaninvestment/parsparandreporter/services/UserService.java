package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.UserDto;
import com.armaninvestment.parsparandreporter.entities.User;
import com.armaninvestment.parsparandreporter.mappers.UserMapper;
import com.armaninvestment.parsparandreporter.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserDto save(UserDto userDto) {
        User user = userMapper.toEntity(userDto);

        // You can perform any additional operations before saving the user, such as encoding the password.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userMapper.toDto(userRepository.save(user));
    }

    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userDto.getId()));

        userMapper.partialUpdate(userDto, user);

        return userMapper.toDto(userRepository.save(user));
    }

    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto).orElseThrow(() -> new EntityNotFoundException("کاربر با شناسه " + id + " یافت نشد."));
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}


