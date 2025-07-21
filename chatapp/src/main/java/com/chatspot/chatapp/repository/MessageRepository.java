package com.chatspot.chatapp.repository;
import com.chatspot.chatapp.entity.message.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @EntityGraph(attributePaths = {"deliveryStatus", "reactions"})
    Optional<Message> findByIdWithDeliveryStatus(Long id);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdDate DESC")
    Page<Message> findMessagesByChatId(@Param("chatId") Long chatId, Pageable pageable);
    
    @Query("SELECT m FROM Message m JOIN m.chat c JOIN c.participants p " +
           "WHERE m.id = :messageId AND p.user.id = :userId AND p.leftAt IS NULL")
    Optional<Message> findByIdAndUserId(@Param("messageId") Long messageId, @Param("userId") String userId);
    
    @Query("SELECT COUNT(m) FROM Message m JOIN m.chat c JOIN c.participants p " +
           "WHERE c.id = :chatId AND p.user.id = :userId AND p.leftAt IS NULL " +
           "AND m.createdDate > p.lastReadAt")
    Integer countUnreadMessages(@Param("chatId") Long chatId, @Param("userId") String userId);
}