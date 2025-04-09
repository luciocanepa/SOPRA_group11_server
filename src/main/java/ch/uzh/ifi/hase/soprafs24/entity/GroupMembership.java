package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "GROUPS_USERS")
@Getter
@Setter
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

} 