package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.Conversation;
import com.bacoge.constructionmaterial.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c WHERE c.user = :user")
    Page<Conversation> findByUser(@Param("user") User user, Pageable pageable);
    @Query("SELECT c FROM Conversation c WHERE c.status = :status")
    Page<Conversation> findByStatus(@Param("status") Conversation.Status status, Pageable pageable);
}
