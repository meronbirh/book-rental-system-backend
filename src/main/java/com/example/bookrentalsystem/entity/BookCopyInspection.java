
package com.example.bookrentalsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class BookCopyInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(nullable = false)
    private String previousCondition;

    @Column(nullable = false)
    private String observedCondition;

    @Column(length = 2000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspected_by_user_id", nullable = false)
    private UserAccount inspectedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime inspectedAt;

    public BookCopyInspection() {
    }

    @PrePersist
    protected void onCreate() {
        if (inspectedAt == null) {
            inspectedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public String getPreviousCondition() {
        return previousCondition;
    }

    public void setPreviousCondition(String previousCondition) {
        this.previousCondition = previousCondition;
    }

    public String getObservedCondition() {
        return observedCondition;
    }

    public void setObservedCondition(String observedCondition) {
        this.observedCondition = observedCondition;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UserAccount getInspectedBy() {
        return inspectedBy;
    }

    public void setInspectedBy(UserAccount inspectedBy) {
        this.inspectedBy = inspectedBy;
    }

    public LocalDateTime getInspectedAt() {
        return inspectedAt;
    }

    public void setInspectedAt(LocalDateTime inspectedAt) {
        this.inspectedAt = inspectedAt;
    }
}
