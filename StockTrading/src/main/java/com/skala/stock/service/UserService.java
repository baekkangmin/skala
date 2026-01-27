package com.skala.stock.service;

import com.skala.stock.dto.UserDto;
import com.skala.stock.entity.User;
import com.skala.stock.repository.PortfolioRepository;
import com.skala.stock.repository.TransactionRepository;
import com.skala.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다: " + userDto.getUsername());
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다: " + userDto.getEmail());
        }

        User user = User.builder()
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .email(userDto.getEmail())
                .balance(userDto.getBalance())
                .build();

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        User user = getUserEntity(id);
        return convertToDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ 추가: 업데이트
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = getUserEntity(id);

        String newUsername = userDto.getUsername();
        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(newUsername, id)) {
                throw new RuntimeException("이미 존재하는 사용자명입니다: " + newUsername);
            }
            user.setUsername(newUsername);
        }

        String newEmail = userDto.getEmail();
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(newEmail, id)) {
                throw new RuntimeException("이미 존재하는 이메일입니다: " + newEmail);
            }
            user.setEmail(newEmail);
        }

        if (userDto.getPassword() != null) {
            user.setPassword(userDto.getPassword());
        }
        if (userDto.getBalance() != null) {
            user.setBalance(userDto.getBalance());
        }

        User saved = userRepository.save(user);
        return convertToDto(saved);
    }

    // ✅ 추가: 삭제 (참조 있으면 삭제 불가)
    @Transactional
    public void deleteUser(Long id) {
        // 존재 확인
        getUserEntity(id);

        if (transactionRepository.existsByUserId(id) || portfolioRepository.existsByUserId(id)) {
            throw new RuntimeException("거래/포트폴리오가 존재하는 사용자는 삭제할 수 없습니다. userId=" + id);
        }

        userRepository.deleteById(id);
    }

    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .balance(user.getBalance())
                .build();
    }
}
