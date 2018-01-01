package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.dto.users.UserCreateEntityRecord;
import uk.gov.digital.ho.hocs.dto.users.UserCreateRecord;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.model.User;

import java.util.*;

@Service
@Slf4j
public class AlfrescoClient {


    private final static int CHUNK_SIZE = 50;
    private final static String API_ENDPOINT_USERS = "/alfresco/s/importUsersAndGroups/";

    private final String API_USERNAME;
    private final String API_PASSWORD;
    private final String API_HOST;


    @Autowired
    public AlfrescoClient(@Value("${hocs.api.user}") String apiUsername,
                          @Value("${hocs.api.pass}") String apiPassword,
                          @Value("${hocs.api.host}") String apiHost) {

        this.API_USERNAME = apiUsername;
        this.API_PASSWORD = apiPassword;
        this.API_HOST = apiHost;
    }

    public void postRecords(List<User> users) throws AlfrescoPostException {

        List<UserCreateRecord> userList = new ArrayList<>();

        for (int i = 0; i < users.size(); i += CHUNK_SIZE) {

            List<User> usersInChunk = new ArrayList<>();
            for (int j = i; j < i + CHUNK_SIZE && j < users.size(); j++) {
                usersInChunk.add(users.get(j));
            }
            userList.add(UserCreateRecord.create(new HashSet<>(usersInChunk)));
        }

        postBatchedRecords(userList);
    }

    private void postBatchedRecords(List<UserCreateRecord> recordList) throws AlfrescoPostException {

        final String url = API_HOST + API_ENDPOINT_USERS;

        int batch = 1;

        for (UserCreateRecord records : recordList) {

            log.info("Sending batch number: " + batch + " of " + recordList.size());

            UserCreateRecord asRecord = (UserCreateRecord) records;
            Set<UserCreateEntityRecord> users = asRecord.getUsers();
            users.stream().forEach(i -> log.info("Sending user -> " + i.getEmail()));

            int statusCode = postRequest(url, records).getStatusCodeValue();
            if (statusCode != HttpStatus.OK.value()) {
                throw new AlfrescoPostException("Failed to post request payload to Alfresco");
            }

            batch++;
        }

    }

    private <T> ResponseEntity postRequest(String url, T payload) {
        HttpEntity<T> request = new HttpEntity<>(payload, getBasicAuthHeaders());
        return new RestTemplate().exchange(url, HttpMethod.POST, request, String.class);
    }

    private HttpHeaders getBasicAuthHeaders() {
        return new HttpHeaders() {{
            String auth = String.format("%s:%s", API_USERNAME, API_PASSWORD);
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = String.format("Basic %s", new String(encodedAuth));
            set("Authorization", authHeader);
            set("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        }};
    }
}
