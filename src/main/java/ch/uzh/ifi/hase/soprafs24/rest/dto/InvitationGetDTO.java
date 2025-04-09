package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvitationGetDTO {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long inviterId;
    private Long inviteeId;
    private MembershipStatus status;
    private LocalDateTime invitedAt;

} 