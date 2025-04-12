package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Internal Group Representation
 * This class composes the internal representation of the group and defines how
 * the group is stored in the database.
 */
@Entity
@Table(name = "GROUPS")
@Getter
@Setter
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Lob
    @Column
    private String image;

    @Column(nullable = false)
    private Long adminId;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GroupMembership> memberships = new ArrayList<>();

    @Transient
    public List<User> getActiveUsers() {
        return memberships.stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .map(GroupMembership::getUser)
            .toList();
    }
}
