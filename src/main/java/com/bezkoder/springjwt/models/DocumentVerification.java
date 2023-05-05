package com.bezkoder.springjwt.models;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by USER on 5/5/2023.
 */
@Entity
@Table(name = "document_verifications")
public class DocumentVerification implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentId; // National ID or passport number

    private String documentImagePath;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentImagePath() {
        return documentImagePath;
    }

    public void setDocumentImagePath(String documentImagePath) {
        this.documentImagePath = documentImagePath;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Getters and setters
}
