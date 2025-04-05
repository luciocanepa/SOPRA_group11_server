package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class GroupRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private GroupRepository groupRepository;

  @Test
  public void findById_success() {
    // given
    User admin = new User();
    admin.setUsername("testAdmin");
    admin.setPassword("password");
    admin.setToken("testToken");
    admin.setStatus(UserStatus.ONLINE);
    entityManager.persist(admin);

    Group group = new Group();
    group.setName("testGroup");
    group.setAdminId(admin.getId());

    entityManager.persist(group);
    entityManager.flush();

    // when
    Group found = groupRepository.findById(group.getId()).get();

    // then
    assertNotNull(found.getId());
    assertEquals(found.getName(), group.getName());
    assertEquals(found.getAdminId(), admin.getId());
  }
}
