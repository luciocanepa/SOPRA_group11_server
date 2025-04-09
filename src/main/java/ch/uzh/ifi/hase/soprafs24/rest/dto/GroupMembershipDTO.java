package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Group Memberships
 * This DTO represents a group's membership with a user without creating circular dependencies.
 */
@Getter
@Setter
 public class GroupMembershipDTO {
    private Long id;
    private Long userId;
    private String username;
    private MembershipStatus status;
    private Long invitedBy;
    private LocalDateTime invitedAt;

} 