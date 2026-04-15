package com.project.ems.report.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.ems.report.dto.EventReportDTO;
import com.project.ems.report.dto.EventRevenueDTO;
import com.project.ems.report.dto.OrganizerReportDTO;
import com.project.ems.report.dto.RevenueDataPoint;
import com.project.ems.report.dto.SummaryResponse;
import com.project.ems.report.dto.TicketSalesDTO;
import com.project.ems.report.service.ReportService;

@RestController
@RequestMapping("/reports")
@PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    private String resolveRole(Authentication auth) {
        return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = resolveRole(auth);
        return ResponseEntity.ok(reportService.getSummary(userId, role));
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueDataPoint>> getRevenue(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "month") String groupBy,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = resolveRole(auth);
        return ResponseEntity.ok(reportService.getRevenue(userId, role, from, to, groupBy));
    }

    @GetMapping("/events")
    public ResponseEntity<Page<EventReportDTO>> getEventReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = resolveRole(auth);
        return ResponseEntity.ok(reportService.getEventReports(userId, role, page, size));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventReportDTO> getEventDetailReport(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = resolveRole(auth);
        return ResponseEntity.ok(reportService.getEventDetailReport(eventId, userId, role));
    }

    @GetMapping("/events/{eventId}/tickets")
    public ResponseEntity<List<TicketSalesDTO>> getTicketSalesReport(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = resolveRole(auth);
        return ResponseEntity.ok(reportService.getTicketSalesReport(eventId, userId, role));
    }

    @GetMapping("/events/{eventId}/revenue")
    public ResponseEntity<EventRevenueDTO> getEventRevenueReport(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Long userId,
            Authentication auth
    ) {
        String role = resolveRole(auth);
        return ResponseEntity.ok(reportService.getEventRevenueReport(eventId, userId, role));
    }

    @GetMapping("/organizers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrganizerReportDTO>> getOrganizerReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reportService.getOrganizerReports(page, size));
    }
}
