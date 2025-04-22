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
    testUser.setToken("valid-token");

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
    Mockito.when(userRepository.existsByToken("valid-token")).thenReturn(true);
    Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
  }

  @Test
  void getGroupById_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));

    // then
    Group found = groupService.getGroupById(1L, "valid-token");
    assertEquals(testGroup.getId(), found.getId());
    assertEquals(testGroup.getName(), found.getName());
  }

  @Test
  void getGroupById_notFound_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    // then -> attempt to get non-existent group -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> groupService.getGroupById(1L, "valid-token"));
  }

  @Test
  void deleteGroup_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    
    // Add a membership to the test group
    testGroup.getMemberships().add(testMembership);
    
    // then
    groupService.deleteGroup(1L, "valid-token");
    
    // Verify that the group was deleted
    Mockito.verify(groupRepository, Mockito.times(1)).delete(testGroup);
    
    // Verify that the membership service was called to remove the user from the group
    Mockito.verify(membershipService, Mockito.times(1)).removeUserFromGroup(testUser, testGroup);
  }
  
  @Test
  void deleteGroup_notFound_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    
    // then -> attempt to delete non-existent group -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> groupService.deleteGroup(1L, "valid-token"));
    
    // Verify that the group was not deleted
    Mockito.verify(groupRepository, Mockito.never()).delete(Mockito.any());
    
    // Verify that the membership service was not called
    Mockito.verify(membershipService, Mockito.never()).removeUserFromGroup(Mockito.any(), Mockito.any());
  }
  
  @Test
  void deleteGroup_noMemberships_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    
    // Ensure the group has no memberships
    testGroup.setMemberships(new ArrayList<>());
    
    // then
    groupService.deleteGroup(1L, "valid-token");
    
    // Verify that the group was deleted
    Mockito.verify(groupRepository, Mockito.times(1)).delete(testGroup);
    
    // Verify that the membership service was not called since there are no memberships
    Mockito.verify(membershipService, Mockito.never()).removeUserFromGroup(Mockito.any(), Mockito.any());
  }

  @Test
  void updateGroup_validInputs_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    Mockito.when(groupRepository.save(Mockito.any())).thenReturn(testGroup);
    
    // Create a group with updated information
    Group updatedGroup = new Group();
    updatedGroup.setName("Updated Group Name");
    updatedGroup.setDescription("Updated Description");
    updatedGroup.setImage("Updated Image URL");
    updatedGroup.setAdminId(testUser.getId());
    
    // Update the test group with the new information
    testGroup.setName("Updated Group Name");
    testGroup.setDescription("Updated Description");
    testGroup.setImage("Updated Image URL");
    
    // then
    Group result = groupService.updateGroup(1L, updatedGroup, "valid-token");
    
    // Verify that the group was saved
    Mockito.verify(groupRepository, Mockito.times(1)).save(Mockito.any());
    
    // Verify that the group was updated correctly
    assertEquals("Updated Group Name", result.getName());
    assertEquals("Updated Description", result.getDescription());
    assertEquals("Updated Image URL", result.getImage());
  }
  
  @Test
  void updateGroup_notFound_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    
    // Create a group with updated information
    Group updatedGroup = new Group();
    updatedGroup.setName("Updated Group Name");
    
    // then -> attempt to update non-existent group -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> groupService.updateGroup(1L, updatedGroup, "valid-token"));
    
    // Verify that the group was not saved
    Mockito.verify(groupRepository, Mockito.never()).save(Mockito.any());
  }
  
  @Test
  void updateGroup_partialUpdate_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    Mockito.when(groupRepository.save(Mockito.any())).thenReturn(testGroup);
    
    // Create a group with only name updated
    Group updatedGroup = new Group();
    updatedGroup.setName("Updated Group Name");
    updatedGroup.setAdminId(testUser.getId());
    
    // Update the test group with the new name
    testGroup.setName("Updated Group Name");
    
    // then
    Group result = groupService.updateGroup(1L, updatedGroup, "valid-token");
    
    // Verify that the group was saved
    Mockito.verify(groupRepository, Mockito.times(1)).save(Mockito.any());
    
    // Verify that only the name was updated
    assertEquals("Updated Group Name", result.getName());
    // Other fields should remain unchanged
    assertEquals(testGroup.getDescription(), result.getDescription());
    assertEquals(testGroup.getImage(), result.getImage());
  }

  @Test
  void removeUserFromGroup_validInputs_success() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    
    // Create a user to remove
    User userToRemove = new User();
    userToRemove.setId(2L);
    userToRemove.setUsername("userToRemove");
    userToRemove.setMemberships(new ArrayList<>());
    
    // Create a membership for the user to remove
    GroupMembership membershipToRemove = new GroupMembership();
    membershipToRemove.setUser(userToRemove);
    membershipToRemove.setGroup(testGroup);
    membershipToRemove.setStatus(MembershipStatus.ACTIVE);
    
    // Add the membership to the group and user
    testGroup.getMemberships().add(membershipToRemove);
    userToRemove.getMemberships().add(membershipToRemove);
    
    // Mock the user repository to return the user to remove
    Mockito.when(userRepository.findById(userToRemove.getId())).thenReturn(Optional.of(userToRemove));
    
    // Mock the membership service to return the membership
    Mockito.when(membershipService.findByUserAndGroup(userToRemove, testGroup)).thenReturn(membershipToRemove);
    
    // then
    groupService.removeUserFromGroup(testGroup.getId(), userToRemove.getId(), "valid-token");
    
    // Verify that the membership service was called to remove the user from the group
    Mockito.verify(membershipService, Mockito.times(1)).removeUserFromGroup(userToRemove, testGroup);
  }
  
  @Test
  void removeUserFromGroup_notAdmin_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    
    // Create a non-admin user
    User nonAdminUser = new User();
    nonAdminUser.setId(999L);
    nonAdminUser.setUsername("nonAdmin");
    nonAdminUser.setToken("non-admin-token");
    
    // Mock the user repository to return the non-admin user
    Mockito.when(userRepository.findByToken("non-admin-token")).thenReturn(nonAdminUser);
    
    // Create a user to remove
    User userToRemove = new User();
    userToRemove.setId(2L);
    userToRemove.setUsername("userToRemove");
    
    // Mock the user repository to return the user to remove
    Mockito.when(userRepository.findById(userToRemove.getId())).thenReturn(Optional.of(userToRemove));
    
    // then -> attempt to remove user as non-admin -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> 
        groupService.removeUserFromGroup(testGroup.getId(), userToRemove.getId(), "non-admin-token"));
    
    // Verify that the membership service was not called
    Mockito.verify(membershipService, Mockito.never()).removeUserFromGroup(Mockito.any(), Mockito.any());
  }
  
  @Test
  void removeUserFromGroup_userNotMember_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    
    // Create a user that is not a member of the group
    User nonMemberUser = new User();
    nonMemberUser.setId(2L);
    nonMemberUser.setUsername("nonMember");
    
    // Mock the user repository to return the non-member user
    Mockito.when(userRepository.findById(nonMemberUser.getId())).thenReturn(Optional.of(nonMemberUser));
    
    // Mock the membership service to return null (user is not a member)
    Mockito.when(membershipService.findByUserAndGroup(nonMemberUser, testGroup)).thenReturn(null);
    
    // then -> attempt to remove non-member -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> 
        groupService.removeUserFromGroup(testGroup.getId(), nonMemberUser.getId(), "valid-token"));
    
    // Verify that the membership service was not called
    Mockito.verify(membershipService, Mockito.never()).removeUserFromGroup(Mockito.any(), Mockito.any());
  }
  
  @Test
  void removeUserFromGroup_groupNotFound_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    
    // then -> attempt to remove user from non-existent group -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> 
        groupService.removeUserFromGroup(999L, 2L, "valid-token"));
    
    // Verify that the membership service was not called
    Mockito.verify(membershipService, Mockito.never()).removeUserFromGroup(Mockito.any(), Mockito.any());
  }
  
  @Test
  void removeUserFromGroup_userNotFound_throwsException() {
    // when -> setup additional mocks
    Mockito.when(groupRepository.findById(Mockito.any())).thenReturn(Optional.of(testGroup));
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    
    // then -> attempt to remove non-existent user -> check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> 
        groupService.removeUserFromGroup(testGroup.getId(), 999L, "valid-token"));
    
    // Verify that the membership service was not called
    Mockito.verify(membershipService, Mockito.never()).removeUserFromGroup(Mockito.any(), Mockito.any());
  }
}
