package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User Memberships
 * This DTO represents a user's membership in a group without creating circular dependencies.
 */
public class UserMembershipDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private MembershipStatus status;
    private Long invitedBy;
    private LocalDateTime invitedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    public Long getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(Long invitedBy) {
        this.invitedBy = invitedBy;
    }

    public LocalDateTime getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(LocalDateTime invitedAt) {
        this.invitedAt = invitedAt;
    }
} 