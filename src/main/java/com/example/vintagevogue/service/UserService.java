package com.example.vintagevogue.service;

import com.example.vintagevogue.custom.CustomException;
import com.example.vintagevogue.model.PasswordResetToken;
import com.example.vintagevogue.model.Role;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.PasswordResetTokenRepository;
import com.example.vintagevogue.repository.RoleRepository;
import com.example.vintagevogue.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Primary
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Register a new user with default role USER.
     */
    public boolean registerUser(User user) {
        try {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new CustomException.UsernameAlreadyExistsException("Username already exists.");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new CustomException.EmailAlreadyExistsException("Address already exists.");
            }
            if (user.getPassword() == null) {
                throw new IllegalArgumentException("Password cannot be null");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                System.out.println("Error: ROLE_USER not found in database");
                return false;
            }
            
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
            
            try {
                sendVerificationEmail(user);
            } catch (Exception e) {
                System.out.println("Error sending verification email: " + e.getMessage());
                e.printStackTrace();
                // Continue with registration even if email fails
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error in registerUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void sendVerificationEmail(User user) {
        String subject = "Verificación de Correo Electrónico - Vintage Vogue";
        String confirmationUrl = "http://localhost:8080/auth/verify?token=" + user.getVerificationToken();
        String message = "Hola " + user.getUsername() + ",\n\n" +
                         "Gracias por registrarte en Vintage Vogue. Para verificar tu correo electrónico, haz clic en el siguiente enlace:\n\n" +
                         confirmationUrl + "\n\n" +
                         "Si no te has registrado en Vintage Vogue, por favor ignora este mensaje.\n\n" +
                         "Saludos,\n" +
                         "El equipo de Vintage Vogue";
        emailService.sendSimpleEmail(user.getEmail(), subject, message);
    }

    /**
     * Verifica el correo electrónico del usuario usando el token de verificación.
     */
    public boolean verifyEmail(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setVerified(true);
            user.setVerificationToken(null); // Invalidar el token después de usarlo
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Reenvía el email de verificación al usuario.
     */
    public boolean resendVerificationEmail(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        
        // Si ya está verificado, no es necesario reenviar
        if (user.isVerified()) {
            return true;
        }
        
        // Generar un nuevo token de verificación
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);
        
        // Enviar el email de verificación
        try {
            sendVerificationEmail(user);
            return true;
        } catch (Exception e) {
            System.out.println("Error sending verification email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public Optional<User> findByUsername(String username) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        userOptional.ifPresent(user -> Hibernate.initialize(user.getProducts()));
        return userOptional;
    }

    /**
     * Generate a password reset token and send it via email.
     */
    public boolean sendResetPasswordEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // Eliminar token previo antes de crear uno nuevo
        tokenRepository.deleteByUser(user);

        // Crear y guardar nuevo token
        PasswordResetToken resetToken = new PasswordResetToken(user);
        tokenRepository.save(resetToken);

        // Enviar email con el nuevo token
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + resetToken.getToken();
        String subject = "Restablecimiento de Contraseña - Vintage Vogue";
        String message = "Hola " + user.getUsername() + ",\n\n" +
                         "Has solicitado restablecer tu contraseña. Haz clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
                         resetUrl + "\n\n" +
                         "Si no has solicitado este cambio, por favor ignora este mensaje o contacta a nuestro equipo de soporte.\n\n" +
                         "Este enlace expirará en 24 horas.\n\n" +
                         "Saludos,\n" +
                         "El equipo de Vintage Vogue";
        emailService.sendSimpleEmail(user.getEmail(), subject, message);

        return true;
    }

    /**
     * Validate and reset user password.
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token after use
        tokenRepository.delete(resetToken);
        return true;
    }

    /**
     * Assign a role to a user.
     */
    public boolean assignRoleToUser(String username, String roleName) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        if (userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            return false;
        }
        user.getRoles().add(role);
        userRepository.save(user);
        return true;
    }

    /**
     * Remove a role from a user.
     */
    @Transactional
    public boolean removeRoleFromUser(String username, String roleName) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username));
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        Role role = roleRepository.findByName(roleName);
        
        if (role == null) {
            return false;
        }
        
        // Verificar si el usuario tiene el rol antes de intentar eliminarlo
        boolean hasRole = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(roleName));
                
        if (!hasRole) {
            return false;
        }
        
        // Remover el rol del usuario
        user.getRoles().removeIf(r -> r.getName().equals(roleName));
        userRepository.save(user);
        
        return true;
    }

    /**
     * Spring Security method for loading user details.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.getAuthorities());
    }

    /**
     * Update user profile.
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Find users by criteria.
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }


    public List<User> findUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public boolean deleteUser(String username) {
        userRepository.deleteByUsername(username);
        return true;
    }

    @Transactional
    public boolean blockUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setAccountNonLocked(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Save profile image.
     */
    public String saveProfileImage(MultipartFile image, User user) {
        try {
            String folder = "uploads/";
            byte[] bytes = image.getBytes();
            Path path = Paths.get(folder + user.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes);
            return "/uploads/" + user.getId() + "_" + image.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check and update password.
     */
    public boolean checkPassword(User user, String currentPassword) {
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Search for users by name.
     */
    @Transactional
    public List<User> searchUsersByName(String name) {
        return userRepository.findByUsernameContainingIgnoreCase(name);
    }

    @Transactional
    public List<User> searchUsersForMessaging(String username) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);
        users.forEach(user -> Hibernate.initialize(user.getProducts()));
        return users;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public User getUserWithProducts(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            Hibernate.initialize(user.getProducts()); // Asegurar que la colección está cargada
        }
        return user;
    }

    /**
     * Find the most recently registered users.
     * @param limit The maximum number of users to return
     * @return List of the most recently registered users
     */
    public List<User> findRecentUsers(int limit) {
        return userRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
}
