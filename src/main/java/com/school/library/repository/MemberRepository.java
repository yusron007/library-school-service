package com.school.library.repository;

import com.school.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findAllByIsDeletedFalse();
    Optional<Member> findByIdAndIsDeletedFalse(Long id);
    Optional<Member> findByMemberCodeAndIsDeletedFalse(String memberCode);
    boolean existsByEmailAndIsDeletedFalse(String email);
    Optional<Member> findByMemberCode(String memberCode);
    boolean existsByMemberCode(String memberCode);
    boolean existsByEmail(String email);
    Optional<Member> findFirstByOrderByIdDesc();
}
