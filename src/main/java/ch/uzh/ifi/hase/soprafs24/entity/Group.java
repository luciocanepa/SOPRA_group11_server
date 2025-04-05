package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;

/**
 * Internal Group Representation
 * This class composes the internal representation of the group and defines how
 * the group is stored in the database.
 */
@Entity
@Table(name = "GROUPS")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private String image;

    @Column(nullable = false)
    private Long adminId;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GroupMembership> memberships = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public List<GroupMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<GroupMembership> memberships) {
        this.memberships = memberships;
    }
    
    // These methods are kept for backward compatibility but should be replaced
    // with calls to MembershipService in the future
    public void addMembership(GroupMembership membership) {
        memberships.add(membership);
        membership.setGroup(this);
    }
    
    public void removeMembership(GroupMembership membership) {
        memberships.remove(membership);
        membership.setGroup(null);
    }

    @Transient
    public List<User> getActiveUsers() {
        return memberships.stream()
            .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
            .map(GroupMembership::getUser)
            .toList();
    }
}
