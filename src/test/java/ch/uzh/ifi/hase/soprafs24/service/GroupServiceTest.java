package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GroupMembershipRepository;
import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
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

class GroupServiceTest {

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private UserRepository userRepository;
  
  @Mock
  private GroupMembershipRepository membershipRepository;

  @Mock
  private MembershipService membershipService;

  @InjectMocks
  private GroupService groupService;

  private Group testGroup;
  private User testUser;
  private GroupMembership testMembership;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testUsername");
    testUser.setMemberships(new ArrayList<>());

    testGroup = new Group();
    testGroup.setId(1L);
    testGroup.setName("testGroup");
    testGroup.setAdminId(1L);
    testGroup.setMemberships(new ArrayList<>());

    testMembership = new GroupMembership();
    testMembership.setUser(testUser);
    testMembership.setGroup(testGroup);
    testMembership.setStatus(MembershipStatus.ACTIVE);

    // when -> any object is being saved in the repositories -> return the dummy objects
    Mockito.when(groupRepository.save(Mockito.any())).thenReturn(testGroup);
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    Mockito.when(membershipRepository.save(Mockito.any())).thenReturn(testMembership);
    Mockito.when(membershipService.addUserToGroup(Mockito.any(), Mockito.any(), Mockito.eq(MembershipStatus.ACTIVE), Mockito.any())).thenReturn(testMembership);
  }

  @Test
  void createGroup_validInputs_success() {
    // when -> setup additional mocks
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    
    // Create a new group with admin
    Group groupToCreate = new Group();
    groupToCreate.setName("testGroup");
    groupToCreate.setAdminId(testUser.getId());
    
    Group createdGroup = groupService.createGroup(groupToCreate);

    // then
    Mockito.verify(groupRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(membershipService).addUserToGroup(Mockito.any(), Mockito.any(), Mockito.eq(MembershipStatus.ACTIVE), Mockito.eq(testUser.getId()));

    assertEquals(testGroup.getId(), createdGroup.getId());
    assertEquals(testGroup.getName(), createdGroup.getName());
    assertEquals(testGroup.getAdminId(), createdGroup.getAdminId());
  }

  @Test
  void createGroup_invalidAdminId_throwsException() {
    // when -> setup additional mocks
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    // then -> attempt to create group with invalid admin id -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> groupService.createGroup(testGroup));
  }

  @Test
  void getGroupById_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));

    // then
    Group found = groupService.getGroupById(1L);
    assertEquals(testGroup.getId(), found.getId());
    assertEquals(testGroup.getName(), found.getName());
  }

  @Test
  void getGroupById_notFound_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    // then -> attempt to get non-existent group -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> groupService.getGroupById(1L));
  }
}
