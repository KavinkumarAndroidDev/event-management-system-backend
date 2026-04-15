package com.project.ems.report.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.booking.repository.RegistrationRepository;
import com.project.ems.common.entity.Event;
import com.project.ems.common.entity.Registration;
import com.project.ems.common.exception.EventNotFoundException;
import com.project.ems.common.exception.UnauthorizedException;
import com.project.ems.event.repository.EventRepository;
import com.project.ems.feedback.repository.FeedbackRepository;
import com.project.ems.organizer.repository.OrganizerProfileRepository;
import com.project.ems.participant.repository.ParticipantRepository;
import com.project.ems.refund.repository.RefundRepository;
import com.project.ems.report.dto.EventReportDTO;
import com.project.ems.report.dto.EventRevenueDTO;
import com.project.ems.report.dto.OrganizerReportDTO;
import com.project.ems.report.dto.RevenueDataPoint;
import com.project.ems.report.dto.SummaryResponse;
import com.project.ems.report.dto.TicketSalesDTO;

@Service
public class ReportService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final ParticipantRepository participantRepository;
    private final FeedbackRepository feedbackRepository;
    private final RefundRepository refundRepository;
    private final OrganizerProfileRepository organizerProfileRepository;

    public ReportService(EventRepository eventRepository,
                         RegistrationRepository registrationRepository,
                         ParticipantRepository participantRepository,
                         FeedbackRepository feedbackRepository,
                         RefundRepository refundRepository,
                         OrganizerProfileRepository organizerProfileRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.participantRepository = participantRepository;
        this.feedbackRepository = feedbackRepository;
        this.refundRepository = refundRepository;
        this.organizerProfileRepository = organizerProfileRepository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary(Long userId, String role) {
        SummaryResponse summary = new SummaryResponse();

        if ("ADMIN".equals(role)) {
            summary.setTotalEvents(eventRepository.count());
            summary.setPublishedEvents(eventRepository.countByStatus(Event.EventStatus.PUBLISHED));
            summary.setTotalRegistrations(registrationRepository.count());
            summary.setConfirmedRegistrations(registrationRepository.countAllConfirmed());
            summary.setTotalRevenue(registrationRepository.sumAllRevenue());
            summary.setTotalParticipants(participantRepository.countAllActive());
            summary.setCheckedInParticipants(participantRepository.countAllCheckedIn());
            summary.setAverageRating(feedbackRepository.findOverallAverageRating());
        } else {
            summary.setTotalEvents(eventRepository.countByOrganizerId(userId));
            summary.setPublishedEvents(eventRepository.countByOrganizerIdAndStatus(userId, Event.EventStatus.PUBLISHED));
            summary.setTotalRegistrations(registrationRepository.countAllByOrganizerId(userId));
            summary.setConfirmedRegistrations(registrationRepository.countConfirmedByOrganizerId(userId));
            summary.setTotalRevenue(registrationRepository.sumRevenueByOrganizerId(userId));
            summary.setTotalParticipants(participantRepository.countActiveByOrganizerId(userId));
            summary.setCheckedInParticipants(participantRepository.countCheckedInByOrganizerId(userId));
            summary.setAverageRating(feedbackRepository.findAverageRatingByOrganizerId(userId));
        }

        return summary;
    }

    @Transactional(readOnly = true)
    public List<RevenueDataPoint> getRevenue(Long userId, String role, String from, String to, String groupBy) {
        LocalDateTime fromDate = from != null && !from.isBlank()
                ? LocalDate.parse(from).atStartOfDay()
                : LocalDateTime.now().minusMonths(12);

        LocalDateTime toDate = to != null && !to.isBlank()
                ? LocalDate.parse(to).atTime(23, 59, 59)
                : LocalDateTime.now();

        Long organizerId = "ADMIN".equals(role) ? null : userId;

        List<Registration> registrations = registrationRepository.findConfirmedBetween(fromDate, toDate, organizerId);

        boolean groupByDay = "day".equalsIgnoreCase(groupBy);
        DateTimeFormatter formatter = groupByDay
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd")
                : DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        Map<String, Long> countMap = new LinkedHashMap<>();

        for (Registration reg : registrations) {
            String key = reg.getCreatedAt().format(formatter);
            revenueMap.merge(key, reg.getTotalAmount(), BigDecimal::add);
            countMap.merge(key, 1L, Long::sum);
        }

        List<RevenueDataPoint> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : revenueMap.entrySet()) {
            result.add(new RevenueDataPoint(entry.getKey(), entry.getValue(), countMap.get(entry.getKey())));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Page<EventReportDTO> getEventReports(Long userId, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime"));

        List<Event> events;
        if ("ADMIN".equals(role)) {
            events = eventRepository.findAll(pageable).getContent();
        } else {
            events = eventRepository.findByOrganizerId(userId);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), events.size());
            if (start > events.size()) {
                events = List.of();
            } else {
                events = events.subList(start, end);
            }
        }

        List<EventReportDTO> dtos = events.stream().map(this::toEventReportDTO).toList();

        long total = "ADMIN".equals(role)
                ? eventRepository.count()
                : eventRepository.countByOrganizerId(userId);

        return new PageImpl<>(dtos, pageable, total);
    }

    @Transactional(readOnly = true)
    public EventReportDTO getEventDetailReport(Long eventId, Long userId, String role) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if ("ORGANIZER".equals(role) && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view reports for your own events");
        }

        return toEventReportDTO(event);
    }

    @Transactional(readOnly = true)
    public List<TicketSalesDTO> getTicketSalesReport(Long eventId, Long userId, String role) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if ("ORGANIZER".equals(role) && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view reports for your own events");
        }

        return event.getTickets().stream().map(ticket -> {
            TicketSalesDTO dto = new TicketSalesDTO();
            dto.setTicketId(ticket.getId());
            dto.setTicketName(ticket.getName());
            dto.setPrice(ticket.getPrice());
            dto.setTotalQuantity(ticket.getTotalQuantity());
            dto.setAvailableQuantity(ticket.getAvailableQuantity());
            int sold = ticket.getTotalQuantity() - ticket.getAvailableQuantity();
            dto.setSoldQuantity(sold);
            dto.setRevenue(ticket.getPrice().multiply(BigDecimal.valueOf(sold)));
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public EventRevenueDTO getEventRevenueReport(Long eventId, Long userId, String role) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if ("ORGANIZER".equals(role) && !event.getOrganizer().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view reports for your own events");
        }

        BigDecimal gross = registrationRepository.sumRevenueByEventId(eventId);
        BigDecimal refunds = refundRepository.sumSuccessfulRefundsByEventId(eventId);
        BigDecimal net = gross.subtract(refunds);

        EventRevenueDTO dto = new EventRevenueDTO();
        dto.setEventId(eventId);
        dto.setEventTitle(event.getTitle());
        dto.setGrossRevenue(gross);
        dto.setRefundAmount(refunds);
        dto.setNetRevenue(net);
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<OrganizerReportDTO> getOrganizerReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return organizerProfileRepository.findAll(pageable).map(profile -> {
            Long organizerId = profile.getUser().getId();

            OrganizerReportDTO dto = new OrganizerReportDTO();
            dto.setOrganizerId(organizerId);
            dto.setOrganizerName(profile.getUser().getFullName());
            dto.setOrganizationName(profile.getOrganizationName());
            dto.setTotalEvents(eventRepository.countByOrganizerId(organizerId));
            dto.setPublishedEvents(eventRepository.countByOrganizerIdAndStatus(organizerId, Event.EventStatus.PUBLISHED));
            dto.setTotalRegistrations(registrationRepository.countConfirmedByOrganizerId(organizerId));
            dto.setTotalRevenue(registrationRepository.sumRevenueByOrganizerId(organizerId));
            dto.setAverageRating(feedbackRepository.findAverageRatingByOrganizerId(organizerId));
            return dto;
        });
    }

    private EventReportDTO toEventReportDTO(Event event) {
        Long eventId = event.getId();

        Long totalParticipants = participantRepository.countActiveByEventId(eventId);
        Long checkedIn = participantRepository.countCheckedInByEventId(eventId);
        Long confirmedReg = registrationRepository.countConfirmedByEventId(eventId);
        Long totalReg = registrationRepository.countAllByEventId(eventId);
        BigDecimal gross = registrationRepository.sumRevenueByEventId(eventId);
        BigDecimal refunds = refundRepository.sumSuccessfulRefundsByEventId(eventId);
        Double avgRating = feedbackRepository.findAverageRatingByEventId(eventId);

        double attendanceRate = totalParticipants > 0
                ? (checkedIn.doubleValue() / totalParticipants.doubleValue()) * 100
                : 0.0;

        EventReportDTO dto = new EventReportDTO();
        dto.setEventId(eventId);
        dto.setEventTitle(event.getTitle());
        dto.setStatus(event.getStatus().name());
        dto.setStartTime(event.getStartTime());
        dto.setTotalRegistrations(totalReg);
        dto.setConfirmedRegistrations(confirmedReg);
        dto.setGrossRevenue(gross);
        dto.setRefundAmount(refunds);
        dto.setNetRevenue(gross.subtract(refunds));
        dto.setTotalParticipants(totalParticipants);
        dto.setCheckedInParticipants(checkedIn);
        dto.setAttendanceRate(Math.round(attendanceRate * 100.0) / 100.0);
        dto.setAverageRating(avgRating);
        return dto;
    }
}
