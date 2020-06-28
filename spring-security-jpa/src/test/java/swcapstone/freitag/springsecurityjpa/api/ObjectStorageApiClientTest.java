package swcapstone.freitag.springsecurityjpa.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ObjectStorageApiClientTest {

    private ObjectStorageApiClient objectStorageApiClient = new ObjectStorageApiClient();

    @Disabled("테스트 환경마다 달라서 임시로 disable")
    @Test
    public void uploadTest() throws Exception {
        File data = new File("/Users/choejaeung/Desktop/cat.jpg");
        String objectName = objectStorageApiClient.putObject("woneyhoney1", data);
        assertNotNull(objectName);
        System.out.println(objectName);
    }

}