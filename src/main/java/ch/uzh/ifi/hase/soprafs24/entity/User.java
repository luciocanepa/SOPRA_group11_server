package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USERS")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private UserStatus status;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<GroupMembership> memberships = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
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
    membership.setUser(this);
  }
  
  public void removeMembership(GroupMembership membership) {
    memberships.remove(membership);
    membership.setUser(null);
  }

  @Transient
  public List<Group> getActiveGroups() {
    return memberships.stream()
        .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
        .map(GroupMembership::getGroup)
        .toList();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    User user = (User) object;
    return id.equals(user.id) &&
           password.equals(user.password) &&
           username.equals(user.username) &&
           token.equals(user.token) &&
           status == user.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, password, username, token, status);
  }
}
