package com.alonso.salesapp.service;

import com.alonso.salesapp.dto.UserDTO;

import java.util.List;

public interface IUserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Integer idUser);
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Integer idUser, UserDTO userDTO);
    void deleteUser(Integer idUser);
}
