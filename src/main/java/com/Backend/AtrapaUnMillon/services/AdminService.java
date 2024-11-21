package com.Backend.AtrapaUnMillon.services;

import com.Backend.AtrapaUnMillon.exceptions.AdminBadRequestException;
import com.Backend.AtrapaUnMillon.models.Admin;
import com.Backend.AtrapaUnMillon.repositories.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /*
    @PostConstruct
    public void encryptExistingPasswords() {
        List<Admin> admins = adminRepository.findAll();

        for (Admin admin : admins) {
            String hashedPassword = passwordEncoder.encode(admin.getPassword());
            admin.setPassword(hashedPassword);
            adminRepository.save(admin);
        }
    }
    */

    public List<Admin> getAllAdmins(){
        return adminRepository.findAll();
    }

    public Admin login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username);
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
            return admin;
        }
        return null;
    }

    public Optional<Admin> getById (Long id){
        return adminRepository.findById(id);
    }

    public Admin signIn(String username, String password, String key){
        final String hiddenKey = "PROFECHULO24";
        if(!key.equals(hiddenKey)){
            throw new AdminBadRequestException("Clave inválida");
        }
        Admin admin = new Admin(username, password);
        return adminRepository.save(admin);
    }
}
