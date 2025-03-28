package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GroupPostDTO;
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
    @ResponseBody
    public List<GroupGetDTO> getAllGroups() {
        List<Group> groups = groupService.getGroups();
        List<GroupGetDTO> groupGetDTOs = new ArrayList<>();

        for (Group group : groups) {
            groupGetDTOs.add(DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group));
        }
        return groupGetDTOs;
    }

    @GetMapping("/groups/{gid}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GroupGetDTO getGroup(@PathVariable Long gid) {
        Group group = groupService.getGroupById(gid);
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group);
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public GroupGetDTO createGroup(@RequestBody GroupPostDTO groupPostDTO) {
        Group groupInput = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(groupPostDTO);

        Group createdGroup = groupService.createGroup(groupInput);

        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(createdGroup);
    }

    @PostMapping("/groups/{gid}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GroupGetDTO addUserToGroup(@PathVariable Long gid, @RequestBody Long userId) {
        Group updatedGroup = groupService.addUserToGroup(gid, userId);
        
        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(updatedGroup);
    }
}
