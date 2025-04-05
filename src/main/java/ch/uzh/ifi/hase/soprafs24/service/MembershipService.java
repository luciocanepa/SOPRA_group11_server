package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.util.List;

/**
 * Membership Service Interface
 * This interface defines the contract for membership operations between users and groups.
 * It helps break circular dependencies by centralizing membership management.
 */
public interface MembershipService {
    
    /**
     * Adds a user to a group with the specified status
     * @param user the user to add
     * @param group the group to add the user to
     * @param status the membership status
     * @param invitedBy the ID of the user who invited this user
     * @return the created membership
     */
    GroupMembership addUserToGroup(User user, Group group, MembershipStatus status, Long invitedBy);
    
    /**
     * Removes a user from a group
     * @param user the user to remove
     * @param group the group to remove the user from
     */
    void removeUserFromGroup(User user, Group group);
    
    /**
     * Gets all active users in a group
     * @param group the group
     * @return list of active users
     */
    List<User> getActiveUsersInGroup(Group group);
    
    /**
     * Gets all active groups for a user
     * @param user the user
     * @return list of active groups
     */
    List<Group> getActiveGroupsForUser(User user);
    
    /**
     * Finds a membership by user and group
     * @param user the user
     * @param group the group
     * @return the membership if found, null otherwise
     */
    GroupMembership findByUserAndGroup(User user, Group group);
    
    /**
     * Updates the status of a membership
     * @param membership the membership to update
     * @param status the new status
     * @return the updated membership
     */
    GroupMembership updateMembershipStatus(GroupMembership membership, MembershipStatus status);
}