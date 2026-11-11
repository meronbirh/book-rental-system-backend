
package com.example.bookrentalsystem.repository;

import com.example.bookrentalsystem.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    // Get featured books, with pagination.
    Page<Book> findByFeaturedTrue(Pageable pageable);

    // Get newest catalogue additions first, with pagination.
    Page<Book> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
