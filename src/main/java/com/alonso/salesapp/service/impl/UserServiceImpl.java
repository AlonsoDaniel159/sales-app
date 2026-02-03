package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.dto.UserDTO;
import com.alonso.salesapp.exception.ModelNotFoundException;
import com.alonso.salesapp.mapper.UserMapper;
import com.alonso.salesapp.model.Role;
import com.alonso.salesapp.model.User;
import com.alonso.salesapp.repository.RoleRepo;
import com.alonso.salesapp.repository.UserRepo;
import com.alonso.salesapp.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepo repo;
    private final RoleRepo roleRepo;
    private final UserMapper mapper;

    @Override
    public List<UserDTO> getAllUsers() {
        return repo.findAll().stream().map(mapper::toDTO).toList();
    }

    @Override
    public UserDTO getUserById(Integer idUser) {
        return mapper.toDTO(findUserByIdOrThrow(idUser));
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        Role role = roleRepo.findById(userDTO.idRole())
                .orElseThrow(() -> new ModelNotFoundException("Role not found ID: " + userDTO.idRole()));

        User user = mapper.toEntity(userDTO);
        user.setRole(role);
        return mapper.toDTO(repo.save(user));
    }

    @Override
    public UserDTO updateUser(Integer idUser, UserDTO userDTO) {
        findUserByIdOrThrow(idUser);
        Role role = roleRepo.findById(userDTO.idRole())
                .orElseThrow(() -> new ModelNotFoundException("Role not found ID: " + userDTO.idRole()));

        User user = mapper.toEntity(userDTO);
        user.setIdUser(idUser);
        user.setRole(role);
        return mapper.toDTO(repo.save(user));
    }

    @Override
    public void deleteUser(Integer idUser) {
        User user = findUserByIdOrThrow(idUser);
        user.setEnabled(false);
        repo.save(user);
    }

    private User findUserByIdOrThrow(Integer idUser) {
        return repo.findById(idUser)
                .orElseThrow(() -> new ModelNotFoundException("User not found ID: " + idUser));
    }
}
