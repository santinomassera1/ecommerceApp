package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByFromUserAndToUserOrderByTimestampAsc(String fromUser, String toUser);

    List<Message> findByToUserAndFromUserOrderByTimestampAsc(String toUser, String fromUser);
}
