package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserAccountId(Long userId);
}