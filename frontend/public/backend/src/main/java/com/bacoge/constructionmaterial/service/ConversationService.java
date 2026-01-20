package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.Conversation;
import com.bacoge.constructionmaterial.model.Message;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.ConversationRepository;
import com.bacoge.constructionmaterial.repository.MessageRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public Conversation createConversationFromContact(String subject, String content, String userEmailOptional) {
        Conversation conv = new Conversation();
        conv.setSubject(subject != null ? subject : "Contact");
        if (userEmailOptional != null) {
            Optional<User> u = userRepository.findByEmail(userEmailOptional);
            u.ifPresent(conv::setUser);
        }
        conv.setStatus(Conversation.Status.OPEN);
        conv.setCreatedAt(LocalDateTime.now());
        conv.setLastMessageAt(LocalDateTime.now());
        Conversation saved = conversationRepository.save(conv);

        Message first = new Message();
        first.setConversation(saved);
        first.setSenderType(Message.SenderType.USER);
        first.setContent(content);
        first.setCreatedAt(LocalDateTime.now());
        first.setReadByAdmin(false);
        first.setReadByUser(true);
        if (saved.getUser() != null) {
            first.setSenderUser(saved.getUser());
        }
        messageRepository.save(first);

        saved.setAdminUnreadCount(saved.getAdminUnreadCount() + 1);
        saved.setLastMessageAt(first.getCreatedAt());
        conversationRepository.save(saved);
        return saved;
    }

    public Page<Conversation> getAdminConversations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageAt").descending());
        return conversationRepository.findAll(pageable);
    }

    public Page<Conversation> getUserConversations(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageAt").descending());
        return conversationRepository.findByUser(user, pageable);
    }

    public List<Message> getMessages(Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        return messageRepository.findByConversationOrderByCreatedAtAsc(conv);
    }

    public Conversation adminReply(Long conversationId, String content, Long adminUserIdOptional) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSenderType(Message.SenderType.ADMIN);
        if (adminUserIdOptional != null) {
            userRepository.findById(adminUserIdOptional).ifPresent(msg::setSenderUser);
        }
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setReadByAdmin(true);
        msg.setReadByUser(false);
        messageRepository.save(msg);

        conv.setUserUnreadCount(conv.getUserUnreadCount() + 1);
        conv.setLastMessageAt(msg.getCreatedAt());
        
        // Envoyer une notification pour la réponse admin
        try {
            String senderName = msg.getSenderUser() != null ? msg.getSenderUser().getFirstName() + " " + msg.getSenderUser().getLastName() : "Administrateur";
            notificationService.sendNewConversationMessageNotification(conversationId, senderName, content, true);
        } catch (Exception e) {
            // Log l'erreur mais ne pas empêcher la création du message
            System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
        
        return conversationRepository.save(conv);
    }

    public Conversation userReply(User user, Long conversationId, String content) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        if (conv.getUser() == null || !conv.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }
        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSenderType(Message.SenderType.USER);
        msg.setSenderUser(user);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setReadByAdmin(false);
        msg.setReadByUser(true);
        messageRepository.save(msg);

        conv.setAdminUnreadCount(conv.getAdminUnreadCount() + 1);
        conv.setLastMessageAt(msg.getCreatedAt());
        
        // Envoyer une notification pour le message utilisateur
        try {
            String senderName = user.getFirstName() + " " + user.getLastName();
            notificationService.sendNewConversationMessageNotification(conversationId, senderName, content, false);
        } catch (Exception e) {
            // Log l'erreur mais ne pas empêcher la création du message
            System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
        return conversationRepository.save(conv);
    }

    public void markAdminRead(Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        if (conv.getAdminUnreadCount() > 0) {
            conv.setAdminUnreadCount(0);
            conversationRepository.save(conv);
        }
    }

    public void markUserRead(User user, Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        if (conv.getUser() == null || !conv.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Accès refusé");
        }
        if (conv.getUserUnreadCount() > 0) {
            conv.setUserUnreadCount(0);
            conversationRepository.save(conv);
        }
    }

    public long getUserUnreadCount(User user) {
        Page<Conversation> page = getUserConversations(user, 0, 100);
        return page.getContent().stream().mapToLong(Conversation::getUserUnreadCount).sum();
    }

    /**
     * Total des messages non lus côté admin (somme de adminUnreadCount)
     * Note: limite à la première page (100) comme pour getUserUnreadCount
     */
    public long getAdminUnreadCount() {
        Page<Conversation> page = getAdminConversations(0, 100);
        return page.getContent().stream().mapToLong(Conversation::getAdminUnreadCount).sum();
    }
}
