package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;

@Entity
@Table(name = "GROUPS_USERS")
public class GroupMembership implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipStatus status;
    
    @Column
    private Long invitedBy;
    
    @Column
    private LocalDateTime invitedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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