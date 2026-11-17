package com.tylerandrews.lcg.repository;

import com.tylerandrews.lcg.model.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    @Query("""
        select ticket from SupportTicket ticket
        where (:status is null or ticket.status = :status)
          and (:email is null or lower(ticket.email) like lower(concat('%', :email, '%')))
          and (:from is null or ticket.createdAt >= :from)
          and (:through is null or ticket.createdAt < :through)
        order by ticket.createdAt desc
        """)
    Page<SupportTicket> search(@Param("status") SupportTicket.Status status,
                               @Param("email") String email,
                               @Param("from") LocalDateTime from,
                               @Param("through") LocalDateTime through,
                               Pageable pageable);
}
