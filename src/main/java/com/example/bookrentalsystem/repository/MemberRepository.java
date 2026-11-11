package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUserAccountId(Long userAccountId);
}