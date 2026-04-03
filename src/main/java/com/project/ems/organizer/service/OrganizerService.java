package com.project.ems.organizer.service;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.ems.auth.repository.UserRepository;
import com.project.ems.common.entity.OrganizerProfile;
import com.project.ems.common.entity.User;
import com.project.ems.common.exception.OrganizerAlreadyExistsException;
import com.project.ems.common.exception.OrganizerNotFoundException;
import com.project.ems.common.exception.UserNotFoundException;
import com.project.ems.organizer.dto.OrganizerOnboardingRequest;
import com.project.ems.organizer.dto.OrganizerProfileDTO;
import com.project.ems.organizer.dto.OrganizerStatusRequest;
import com.project.ems.organizer.dto.OrganizerUpdateRequest;
import com.project.ems.organizer.repository.OrganizerProfileRepository;

@Service
public class OrganizerService {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final UserRepository userRepository;

    public OrganizerService(OrganizerProfileRepository organizerProfileRepository,
                            UserRepository userRepository) {
        this.organizerProfileRepository = organizerProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrganizerProfileDTO onboard(Long userId, OrganizerOnboardingRequest request) {

        if (organizerProfileRepository.findByUserId(userId).isPresent()) {
            throw new OrganizerAlreadyExistsException("You have already submitted your organizer profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);
        profile.setOrganizationName(request.getOrganizationName().trim());

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            profile.setDescription(request.getDescription().trim());
        }

        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            profile.setWebsite(request.getWebsite().trim());
        }

        if (request.getInstagram() != null && !request.getInstagram().isBlank()) {
            profile.setInstagram(request.getInstagram().trim());
        }

        if (request.getLinkedin() != null && !request.getLinkedin().isBlank()) {
            profile.setLinkedin(request.getLinkedin().trim());
        }

        profile.setVerified(false);
        profile.setCreatedAt(LocalDateTime.now());

        return toDTO(organizerProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public OrganizerProfileDTO getProfileByUserId(Long userId) {
        OrganizerProfile profile = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OrganizerNotFoundException("Organizer profile not found"));
        return toDTO(profile);
    }

    @Transactional(readOnly = true)
    public OrganizerProfileDTO getMyProfile(Long userId) {
        return getProfileByUserId(userId);
    }

    @Transactional
    public OrganizerProfileDTO updateMyProfile(Long userId, OrganizerUpdateRequest request) {
        OrganizerProfile profile = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OrganizerNotFoundException("Organizer profile not found"));

        if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
            profile.setOrganizationName(request.getOrganizationName().trim());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            profile.setDescription(request.getDescription().trim());
        }

        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            profile.setWebsite(request.getWebsite().trim());
        }

        if (request.getInstagram() != null && !request.getInstagram().isBlank()) {
            profile.setInstagram(request.getInstagram().trim());
        }

        if (request.getLinkedin() != null && !request.getLinkedin().isBlank()) {
            profile.setLinkedin(request.getLinkedin().trim());
        }

        return toDTO(organizerProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public Page<OrganizerProfileDTO> getAllOrganizers(String status, int page, int size, String sort) {
    	String[] parts = sort.split(",");

    	String property = parts[0];
    	String direction = parts.length > 1 ? parts[1] : "asc";

    	Sort.Direction dir = direction.equalsIgnoreCase("desc")
    	        ? Sort.Direction.DESC
    	        : Sort.Direction.ASC;

    	Pageable pageable = PageRequest.of(page, size, Sort.by(dir, property));

        if (status != null && status.equalsIgnoreCase("PENDING")) {
            return organizerProfileRepository.findByVerified(false, pageable).map(this::toDTO);
        }

        return organizerProfileRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public OrganizerProfileDTO getOrganizerById(Long profileId) {
        OrganizerProfile profile = organizerProfileRepository.findById(profileId)
                .orElseThrow(() -> new OrganizerNotFoundException("Organizer profile not found"));
        return toDTO(profile);
    }

    @Transactional
    public OrganizerProfileDTO updateOrganizerStatus(Long profileId, OrganizerStatusRequest request) {
        OrganizerProfile profile = organizerProfileRepository.findById(profileId)
                .orElseThrow(() -> new OrganizerNotFoundException("Organizer profile not found"));

        String status = request.getStatus().toUpperCase();
        if (status.equals("APPROVED")) {
            profile.setVerified(true);
        } else if (status.equals("REJECTED") || status.equals("SUSPENDED")) {
            profile.setVerified(false);
        }

        return toDTO(organizerProfileRepository.save(profile));
    }

    @Transactional
    public OrganizerProfileDTO updateVerificationStatus(Long profileId, Boolean verified) {
        OrganizerProfile profile = organizerProfileRepository.findById(profileId)
                .orElseThrow(() -> new OrganizerNotFoundException("Organizer profile not found"));
        profile.setVerified(verified);
        return toDTO(organizerProfileRepository.save(profile));
    }

    private OrganizerProfileDTO toDTO(OrganizerProfile profile) {
        OrganizerProfileDTO dto = new OrganizerProfileDTO();
        dto.setId(profile.getId());
        dto.setOrganizationName(profile.getOrganizationName());
        dto.setDescription(profile.getDescription());
        dto.setWebsite(profile.getWebsite());
        dto.setInstagram(profile.getInstagram());
        dto.setLinkedin(profile.getLinkedin());
        dto.setVerified(profile.getVerified());
        dto.setCreatedAt(profile.getCreatedAt());

        if (profile.getUser() != null) {
            dto.setUserId(profile.getUser().getId());
            dto.setFullName(profile.getUser().getFullName());
            dto.setEmail(profile.getUser().getEmail());
        }

        return dto;
    }
}
