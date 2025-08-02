package com.ns.solve.repository.admin;

import com.ns.solve.domain.entity.admin.EmailReceiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmailReceiverRepository extends JpaRepository<EmailReceiver, Long> {
    @Query("SELECT e.email FROM EmailReceiver e")
    List<String> findAllEmails();

}
