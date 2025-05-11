package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.MembershipStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Activity;
import ch.uzh.ifi.hase.soprafs24.entity.CalendarEntries;
import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.GroupMembership;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "password", target = "password")
  @Mapping(source = "username", target = "username")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "token", ignore = true) 
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "activeGroups", ignore = true)
  @Mapping(target = "birthday", ignore = true)
  @Mapping(target = "duration", ignore = true)
  @Mapping(target = "name", ignore = true)
  @Mapping(target = "profilePicture", ignore = true)
  @Mapping(target = "startTime", ignore = true)
  @Mapping(target = "timezone", ignore = true)

  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(expression = "java(convertActiveGroupsToIds(user))", target = "groupIds")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "birthday", target = "birthday")
  @Mapping(source = "timezone", target = "timezone")
  @Mapping(source = "profilePicture", target = "profilePicture")
  UserGetDTO convertEntityToUserGetDTO(User user);

  @Mapping(source = "startTime", target = "startTime")
  @Mapping(source = "duration", target = "duration")
  @Mapping(source = "status", target = "status")
  UserTimerPutDTO convertEntityToUserTimerPutDTO(User user);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "image", target = "image")
  @Mapping(target = "adminId", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "activeUsers", ignore = true)
  @Mapping(target = "calendarEntries", ignore = true)
  Group convertGroupPostDTOtoEntity(GroupPostDTO groupPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "image", target = "image")
  @Mapping(source = "adminId", target = "adminId")
  @Mapping(expression = "java(convertActiveUsers(group))", target = "users")
  GroupGetDTO convertEntityToGroupGetDTO(Group group);
  
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "image", target = "image")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "adminId", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  @Mapping(target = "activeUsers", ignore = true)
  @Mapping(target = "calendarEntries", ignore = true)
  Group convertGroupPutDTOtoEntity(GroupPutDTO groupPutDTO);
  
  @Mapping(source = "id", target = "id")
  @Mapping(source = "group.id", target = "groupId")
  @Mapping(source = "group.name", target = "groupName")
  @Mapping(source = "invitedBy", target = "inviterId")
  @Mapping(source = "user.id", target = "inviteeId")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "invitedAt", target = "invitedAt")
  InvitationGetDTO convertMembershipToInvitationGetDTO(GroupMembership membership);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "group.id", target = "groupId")
  @Mapping(source = "group.name", target = "groupName")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "invitedBy", target = "invitedBy")
  @Mapping(source = "invitedAt", target = "invitedAt")
  UserMembershipDTO convertMembershipToUserMembershipDTO(GroupMembership membership);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "invitedBy", target = "invitedBy")
  @Mapping(source = "invitedAt", target = "invitedAt")
  GroupMembershipDTO convertMembershipToGroupMembershipDTO(GroupMembership membership);

  @Named("convertActiveUsers")
  default List<UserGetDTO> convertActiveUsers(Group group) {
    if (group.getMemberships() == null) return new ArrayList<>();
    return group.getMemberships().stream()
        .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
        .map(m -> m.getUser())
        .map(this::convertEntityToUserGetDTO)
        .toList();
  }

  @Named("convertActiveGroupsToIds")
  default List<Long> convertActiveGroupsToIds(User user) {
    if (user.getMemberships() == null) return new ArrayList<>();
    return user.getMemberships().stream()
        .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
        .map(m -> m.getGroup().getId())
        .toList();
  }


  @Mapping(source = "username", target = "username")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "password", target = "password")
  @Mapping(source = "birthday", target = "birthday")
  @Mapping(source = "timezone", target = "timezone")
  @Mapping(source = "profilePicture", target = "profilePicture")
  UserPutDTO convertEntityToUserPutDTO(User user);

  @Mapping(source = "startDateTime", target = "startDateTime")
  @Mapping(source = "endDateTime", target = "endDateTime")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  Activity convertActivityPostDTOtoEntity(ActivityPostDTO activityPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "startDateTime", target = "startDateTime")
  @Mapping(source = "endDateTime", target = "endDateTime")
  ActivityGetDTO convertEntityToActivityGetDTO(Activity activity);


  @Mapping(target = "group", expression = "java(mapGroupId(calendarEntryPostDTO.getGroupId()))")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "startTime", target = "startTime")
  @Mapping(source = "endTime", target = "endTime")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdByUsername", ignore = true)
  CalendarEntries convertCalendarEntryPostDTOtoEntity(CalendarEntriesPostDTO calendarEntryPostDTO);
  
  @Named("mapGroupId")
  default Group mapGroupId(Long groupId) {
    if (groupId == null) return null;
    Group group = new Group();
    group.setId(groupId);
    return group;
}


  @Mapping(source = "id", target = "id")
  @Mapping(source = "group.id", target = "groupId")
  @Mapping(source = "createdByUsername", target = "createdByUsername")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "startTime", target = "startTime")
  @Mapping(source = "endTime", target = "endTime")
  CalendarEntriesGetDTO convertEntityToCalendarEntryGetDTO(CalendarEntries calendarEntry);

}
