package com.chatspot.chatapp.repository;
import com.chatspot.chatapp.entity.chat.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.user.id = :userId AND p.leftAt IS NULL")
    Page<Chat> findChatsByUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE c.id = :chatId AND p.user.id = :userId AND p.leftAt IS NULL")
    Optional<Chat> findByIdAndUserId(@Param("chatId") Long chatId, @Param("userId") String userId);
    
    @Query("SELECT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE c.type = 'INDIVIDUAL' AND p1.user.id = :userId1 AND p2.user.id = :userId2 " +
           "AND p1.leftAt IS NULL AND p2.leftAt IS NULL")
    Optional<Chat> findIndividualChatBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);
}