package swcapstone.freitag.springsecurityjpa.api;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ObjectStorageApiClientTest {

    private ObjectStorageApiClient objectStorageApiClient = new ObjectStorageApiClient();

    @Ignore
    @Test
    public void uploadTest() throws Exception {
        File data = new File("/Users/choejaeung/Desktop/cat.jpg");
        String objectName = objectStorageApiClient.putObject("woneyhoney1", data);
        assertNotNull(objectName);
        System.out.println(objectName);
    }

}