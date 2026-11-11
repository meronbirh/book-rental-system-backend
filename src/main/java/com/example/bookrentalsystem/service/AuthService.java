
package com.example.bookrentalsystem.service;

import com.example.bookrentalsystem.entity.Member;
import com.example.bookrentalsystem.entity.UserAccount;
import com.example.bookrentalsystem.entity.Wallet;
import com.example.bookrentalsystem.entity.Notification;
import com.example.bookrentalsystem.repository.MemberRepository;
import com.example.bookrentalsystem.repository.UserAccountRepository;
import com.example.bookrentalsystem.repository.WalletRepository;
import com.example.bookrentalsystem.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final NotificationRepository notificationRepository;

    public AuthService(
            UserAccountRepository userAccountRepository,
            MemberRepository memberRepository,
            WalletRepository walletRepository,
            NotificationRepository notificationRepository) {

        this.userAccountRepository = userAccountRepository;
        this.memberRepository = memberRepository;
        this.walletRepository = walletRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public UserAccount register(
            String firstName,
            String lastName,
            String username,
            String password,
            String email,
            String phone,
            String address) {

        if (userAccountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists.");
        }

        if (userAccountRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists.");
        }

        UserAccount userAccount = new UserAccount();

        userAccount.setFirstName(firstName);
        userAccount.setLastName(lastName);
        userAccount.setUsername(username);
        userAccount.setPassword(password);
        userAccount.setEmail(email);
        userAccount.setRole("MEMBER");
        userAccount.setStatus("PENDING");
        userAccount.setActive(true);

        UserAccount savedUser =
                userAccountRepository.save(userAccount);

        Member member = new Member();

        member.setPhone(phone);
        member.setAddress(address);
        member.setMembershipDate(LocalDate.now());
        member.setUserAccount(savedUser);

        Member savedMember =
                memberRepository.save(member);

        Wallet wallet = new Wallet();

        wallet.setBalance(0.0);
        wallet.setMember(savedMember);

        walletRepository.save(wallet);

        // Notify the owner about the new registration.
        Notification notification = new Notification();

        notification.setMessage(
                firstName + " " + lastName
                        + " has submitted a registration request."
        );

        notification.setCreatedDate(LocalDateTime.now());
        notification.setStatus("UNREAD");

        // Preserve the existing owner notification setup.
        UserAccount owner = new UserAccount();
        owner.setId(2L);

        notification.setUserAccount(owner);

        notificationRepository.save(notification);

        return savedUser;
    }

    public UserAccount login(
            String username,
            String password) {

        UserAccount user =
                userAccountRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Invalid username or password."
                                )
                        );

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException(
                    "Invalid username or password."
            );
        }

        if ("PENDING".equals(user.getStatus())) {
            throw new RuntimeException(
                    "Your account is waiting for owner approval."
            );
        }

        if ("REJECTED".equals(user.getStatus())) {
            throw new RuntimeException(
                    "Your account has been rejected."
            );
        }

        if (!user.isActive()) {
            throw new RuntimeException(
                    "Your account has been deactivated. "
                            + "Please contact the administrator."
            );
        }

        return user;
    }

    public void resetPassword(
            String username,
            String email,
            String newPassword,
            String confirmPassword) {

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new RuntimeException(
                    "Passwords do not match."
            );
        }

        if (newPassword.trim().isEmpty()) {
            throw new RuntimeException(
                    "Password cannot be empty."
            );
        }

        UserAccount user =
                userAccountRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Username not found."
                                )
                        );

        if (user.getEmail() == null
                || email == null
                || !user.getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException(
                    "The email does not match this username."
            );
        }

        user.setPassword(newPassword);

        userAccountRepository.save(user);
    }
}
