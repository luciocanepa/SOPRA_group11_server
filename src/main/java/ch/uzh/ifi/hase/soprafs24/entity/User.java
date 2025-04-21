package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
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

  @Column(nullable = true)
  private LocalDateTime startTime;

  @Column(nullable = true)
  private Duration duration;

  @Column(nullable = true)
  private String name;

  @Column(nullable = true)
  private LocalDate birthday;

  @Column(nullable = true)
  private String timezone;

  @Lob
  @Column(nullable = true)
  private String profilePicture;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<GroupMembership> memberships = new ArrayList<>();

  
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
