package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GroupServiceTest {

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GroupService groupService;

  private Group testGroup;
  private User testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testUsername");
    testUser.setGroups(new ArrayList<>());

    testGroup = new Group();
    testGroup.setId(1L);
    testGroup.setName("testGroup");
    testGroup.setAdminId(1L);
    testGroup.setUsers(new ArrayList<>());

    // when -> any object is being saved in the repositories -> return the dummy objects
    Mockito.when(groupRepository.save(Mockito.any())).thenReturn(testGroup);
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
  }

  @Test
  public void createGroup_validInputs_success() {
    // when -> setup additional mocks
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));

    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    Group createdGroup = groupService.createGroup(testGroup);

    // then
    Mockito.verify(groupRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testGroup.getId(), createdGroup.getId());
    assertEquals(testGroup.getName(), createdGroup.getName());
    assertEquals(testGroup.getAdminId(), createdGroup.getAdminId());
    assertEquals(1, createdGroup.getUsers().size());
  }

  @Test
  public void createGroup_invalidAdminId_throwsException() {
    // when -> setup additional mocks
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    // then -> attempt to create group with invalid admin id -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> groupService.createGroup(testGroup));
  }

  @Test
  public void getGroupById_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));

    // then
    Group found = groupService.getGroupById(1L);
    assertEquals(testGroup.getId(), found.getId());
    assertEquals(testGroup.getName(), found.getName());
  }

  @Test
  public void getGroupById_notFound_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    // then -> attempt to get non-existent group -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> groupService.getGroupById(1L));
  }
}
