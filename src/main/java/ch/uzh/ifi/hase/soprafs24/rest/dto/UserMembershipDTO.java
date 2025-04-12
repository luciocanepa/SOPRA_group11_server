package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User Memberships
 * This DTO represents a user's membership in a group without creating circular dependencies.
 */
@Getter
@Setter
public class UserMembershipDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private MembershipStatus status;
    private Long invitedBy;
    private LocalDateTime invitedAt;

} 