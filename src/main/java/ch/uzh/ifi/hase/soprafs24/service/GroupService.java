package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Group;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class GroupService {

    private final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository,
                       @Qualifier("userRepository") UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public List<Group> getGroups() {
        return this.groupRepository.findAll();
    }

    public Group getGroupById(Long groupId) {
        return this.groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Group with ID " + groupId + " was not found"));
    }

    public Group createGroup(Group newGroup) {
        final Long adminId = newGroup.getAdminId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Admin user with ID " + adminId + " was not found"));
        
        newGroup.setAdminId(admin.getId());
        newGroup.setUsers(List.of(admin));
        
        admin.getGroups().add(newGroup);

        newGroup = groupRepository.save(newGroup);
        userRepository.save(admin);
        
        groupRepository.flush();
        userRepository.flush();

        return newGroup;
    }

    public Group addUserToGroup(Long groupId, Long userId) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "User with ID " + userId + " was not found"));

        group.getUsers().add(user);
        user.getGroups().add(group);
        
        userRepository.save(user);
        groupRepository.save(group);
        
        groupRepository.flush();
        userRepository.flush();

        return group;
    }
}
