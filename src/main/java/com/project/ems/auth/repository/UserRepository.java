package com.project.ems.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.ems.common.entity.RoleName;
import com.project.ems.common.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String emailId);

    Optional<User> findByPhone(String phoneNumber);

    @Query("SELECT DISTINCT r.user FROM Registration r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    List<User> findConfirmedAttendeesForEvent(Long eventId);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:role IS NULL OR u.role.name = :role)")
    Page<User> findByFilters(
            @Param("search") String search,
            @Param("status") User.UserStatus status,
            @Param("role") RoleName role,
            Pageable pageable);
}
