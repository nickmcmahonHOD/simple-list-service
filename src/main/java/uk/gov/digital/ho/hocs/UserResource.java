package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.digital.ho.hocs.dto.users.PublishUserListRecord;
import uk.gov.digital.ho.hocs.dto.users.UserSetRecord;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.users.UserFileParser;

import java.util.List;

@RestController
@Slf4j
public class UserResource {
    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/users/{department}", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<UserSetRecord> putUsersByGroup(@PathVariable("department") String department, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            log.info("Parsing \"{}\" Users File", department);
            try {
                userService.updateUsersByDepartment(new UserFileParser(file).getLines(), department);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException | ListNotFoundException e) {
                log.info("{} Users not created", department);
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = {"/users/group/{group}",}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserSetRecord> getUsersByGroup(@PathVariable String group) {
        log.info("\"{}\" requested", group);
        try {
            return ResponseEntity.ok(userService.getUsersByGroupName(group));
        } catch (ListNotFoundException e) {
            log.info("\"{}\" not found", group);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/users/dept/{dept}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<UserSetRecord>> getUsersByDept(@PathVariable("dept") String dept) {
        try {
            userService.getUsersByDepartmentName(dept);
            return ResponseEntity.ok().build();
        } catch (ListNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/users/dept/{dept}/publish/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<PublishUserListRecord>> postUsersToAlfresco(@PathVariable("dept") String dept) {
        try {
            userService.publishUsersByDepartmentName(dept);
            return ResponseEntity.ok().build();

        } catch (ListNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (AlfrescoPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
