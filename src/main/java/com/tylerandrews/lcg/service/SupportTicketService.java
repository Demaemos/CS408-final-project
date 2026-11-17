package com.tylerandrews.lcg.service;

import com.tylerandrews.lcg.form.SupportTicketForm;
import com.tylerandrews.lcg.model.Game;
import com.tylerandrews.lcg.model.SupportTicket;
import com.tylerandrews.lcg.repository.SupportTicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public SupportTicket submit(SupportTicketForm form, Game game) {
        SupportTicket ticket = new SupportTicket();
        ticket.setName(form.getName().trim());
        ticket.setEmail(form.getEmail().trim().toLowerCase(Locale.ROOT));
        ticket.setTopic(form.getTopic().trim());
        ticket.setGame(game);
        ticket.setMessage(form.getMessage().trim());
        return ticketRepository.save(ticket);
    }

    public Page<SupportTicket> search(SupportTicket.Status status, String email,
                                     LocalDate from, LocalDate through, int page) {
        LocalDateTime fromTime = from == null ? null : from.atStartOfDay();
        LocalDateTime throughTime = through == null ? null : through.plusDays(1).atStartOfDay();
        return ticketRepository.search(status, email == null || email.isBlank() ? null : email.trim(),
            fromTime, throughTime, PageRequest.of(Math.max(0, page), 25));
    }

    public Optional<SupportTicket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional
    public SupportTicket update(SupportTicket ticket, SupportTicket.Status status, String adminNote) {
        ticket.setStatus(status);
        ticket.setAdminNote(adminNote == null || adminNote.isBlank() ? null : adminNote.trim());
        return ticketRepository.save(ticket);
    }

    public void delete(SupportTicket ticket) {
        ticketRepository.delete(ticket);
    }
}
