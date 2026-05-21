package com.school.library.repository;

import com.school.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findAllByIsDeletedFalse();
    Optional<Book> findByIdAndIsDeletedFalse(Long id);
    Optional<Book> findByBookCodeAndIsDeletedFalse(String bookCode);
    Optional<Book> findByBookCode(String bookCode);
    boolean existsByBookCode(String bookCode);
    Optional<Book> findFirstByOrderByIdDesc();
}
