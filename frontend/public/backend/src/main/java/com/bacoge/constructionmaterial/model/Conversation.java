package com.bacoge.constructionmaterial.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
public class Conversation {

    public enum Status { OPEN, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String subject;

    // Client participant (one-to-one messaging MVP)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // nullable for guest contact

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.OPEN;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastMessageAt = LocalDateTime.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    // Unread flags (admin side vs user side)
    @Column(nullable = false)
    private int adminUnreadCount = 0;

    @Column(nullable = false)
    private int userUnreadCount = 0;

    public void addMessage(Message msg) {
        msg.setConversation(this);
        messages.add(msg);
        lastMessageAt = msg.getCreatedAt();
        if (msg.getSenderType() == Message.SenderType.USER) {
            adminUnreadCount++;
        } else if (msg.getSenderType() == Message.SenderType.ADMIN || msg.getSenderType() == Message.SenderType.SYSTEM) {
            userUnreadCount++;
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public List<Message> getMessages() { return messages; }
    public int getAdminUnreadCount() { return adminUnreadCount; }
    public void setAdminUnreadCount(int adminUnreadCount) { this.adminUnreadCount = adminUnreadCount; }
    public int getUserUnreadCount() { return userUnreadCount; }
    public void setUserUnreadCount(int userUnreadCount) { this.userUnreadCount = userUnreadCount; }
}
