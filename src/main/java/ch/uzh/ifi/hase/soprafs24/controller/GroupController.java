package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Group Controller
 * This class is responsible for handling all REST request that are related to
 * the group.
 * The controller will receive the request and delegate the execution to the
 * GroupService and finally return the result.
 */
@RestController
public class GroupController {

    private final GroupService groupService;

    GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    public List<GroupGetDTO> getAllGroups(@RequestHeader("Authorization") String token) {
        List<Group> groups = groupService.getGroups(token);
        List<GroupGetDTO> groupGetDTOs = new ArrayList<>();

        for (Group group : groups) {
            groupGetDTOs.add(DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group));
        }
        return groupGetDTOs;
    }

    @GetMapping("/groups/{gid}")
    @ResponseStatus(HttpStatus.OK)
    public GroupGetDTO getGroup(@PathVariable Long gid, @RequestHeader("Authorization") String token) {
        Group group = groupService.getGroupById(gid, token);
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group);
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupGetDTO createGroup(@RequestBody GroupPostDTO groupPostDTO, @RequestHeader("Authorization") String token) {
        Group groupInput = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(groupPostDTO);
        Group createdGroup = groupService.createGroup(groupInput, token);

        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(createdGroup);
    }

    @PutMapping("/groups/{gid}")
    @ResponseStatus(HttpStatus.OK)
    public GroupGetDTO updateGroup(@PathVariable Long gid, @RequestBody GroupPutDTO groupPutDTO, @RequestHeader("Authorization") String token) {
        Group groupInput = DTOMapper.INSTANCE.convertGroupPutDTOtoEntity(groupPutDTO);
        
        Group updatedGroup = groupService.updateGroup(gid, groupInput, token);
        
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(updatedGroup);
    }

    @DeleteMapping("/groups/{gid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long gid, @RequestHeader("Authorization") String token) {
        groupService.deleteGroup(gid, token);
    }

    @DeleteMapping("/groups/{gid}/users/{uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserFromGroup(@PathVariable Long gid, @PathVariable Long uid, @RequestHeader("Authorization") String token) {
        groupService.removeUserFromGroup(gid, uid, token);
    }

}
