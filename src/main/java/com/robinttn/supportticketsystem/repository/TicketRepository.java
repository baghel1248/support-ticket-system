package com.robinttn.supportticketsystem.repository;

import com.robinttn.supportticketsystem.domain.Ticket;
import com.robinttn.supportticketsystem.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
            select t from Ticket t
            where lower(t.title) like lower(concat('%', :keyword, '%'))
               or lower(t.description) like lower(concat('%', :keyword, '%'))
            """)
    List<Ticket> searchByKeyword(@Param("keyword") String keyword);

    List<Ticket> findByStatus(TicketStatus status);
}
