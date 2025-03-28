package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

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
  @Mapping(target = "groups", ignore = true)
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "groups", target = "groupIds", qualifiedByName = "convertGroupsToIds")
  UserGetDTO convertEntityToUserGetDTO(User user);

  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "image", target = "image")
  @Mapping(source = "adminId", target = "adminId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "users", ignore = true)
  Group convertGroupPostDTOtoEntity(GroupPostDTO groupPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "image", target = "image")
  @Mapping(source = "adminId", target = "adminId")
  @Mapping(source = "users", target = "users", qualifiedByName = "convertUsers")
  GroupGetDTO convertEntityToGroupGetDTO(Group group);

  @Named("convertUsers")
  default List<UserGetDTO> convertUsers(List<User> users) {
    if (users == null) return null;
    return users.stream()
        .map(this::convertEntityToUserGetDTO)
        .collect(Collectors.toList());
  }

  @Named("convertGroupsToIds")
  default List<Long> convertGroupsToIds(List<Group> groups) {
    if (groups == null) return null;
    return groups.stream()
        .map(Group::getId)
        .collect(Collectors.toList());
  }
}
