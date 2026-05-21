package com.school.library.repository;

import com.school.library.entity.BookRent;
import com.school.library.entity.RentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRentRepository extends JpaRepository<BookRent, Long> {
    Optional<BookRent> findByRentCode(String rentCode);
    
    long countByMemberIdAndStatus(Long memberId, RentStatus status);
    
    boolean existsByMemberIdAndBookIdAndStatus(Long memberId, Long bookId, RentStatus status);
    
    boolean existsByBookIdAndStatus(Long bookId, RentStatus status);
    
    List<BookRent> findByMemberId(Long memberId);
    
    List<BookRent> findByBookId(Long bookId);
    
    Optional<BookRent> findFirstByOrderByIdDesc();
}
