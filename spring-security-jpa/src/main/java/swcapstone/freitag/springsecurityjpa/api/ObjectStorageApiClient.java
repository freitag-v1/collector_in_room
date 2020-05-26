package swcapstone.freitag.springsecurityjpa.api;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ObjectStorageApiClient {

    private static final String REGION_NAME = "kr-standard";
    private static final String ENDPOINT = "https://kr.object.ncloudstorage.com";

    private static final String ACCESS_KEY = "sQG5BeaHcnvvqK4FI01A";
    private static final String SECRET_KEY = "mvNVjSac240XvnrK4qF39HpoMvvtMQMzUnnNHaRV";


    // S3 client
    final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ENDPOINT, REGION_NAME))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
            .build();


    public String putObject(String bucketName, File uploadFile) throws Exception {

        String objectName = uploadFile.getName();

        try {
            /*PutObjectResult putObjectResult = */s3.putObject(bucketName, objectName, uploadFile);
            System.out.format("Object %s has been created.\n", objectName);

            return objectName;

        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean putBucket(String bucketName) {

        if (s3.doesBucketExistV2(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
            return false;
        }

        // Create two CORS rules.
        List<CORSRule.AllowedMethods> rule1AM = new ArrayList<CORSRule.AllowedMethods>();
        rule1AM.add(CORSRule.AllowedMethods.PUT);
        // rule1AM.add(CORSRule.AllowedMethods.POST);
        rule1AM.add(CORSRule.AllowedMethods.DELETE);
        CORSRule rule1 = new CORSRule().withId("CORSRule1").withAllowedMethods(rule1AM)
                .withAllowedOrigins(Arrays.asList("http://*.example.com"));

        List<CORSRule.AllowedMethods> rule2AM = new ArrayList<CORSRule.AllowedMethods>();
        rule2AM.add(CORSRule.AllowedMethods.GET);
        rule2AM.add(CORSRule.AllowedMethods.POST);
        CORSRule rule2 = new CORSRule().withId("CORSRule2").withAllowedMethods(rule2AM)
                .withAllowedOrigins(Arrays.asList("*")).withMaxAgeSeconds(3000)
                .withExposedHeaders(Arrays.asList("x-amz-server-side-encryption"));

        List<CORSRule> rules = new ArrayList<CORSRule>();
        rules.add(rule1);
        rules.add(rule2);

        // Add the rules to a new CORS configuration.
        BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration();
        configuration.setRules(rules);

        // Add the configuration to the bucket.
        s3.setBucketCrossOriginConfiguration(bucketName, configuration);

        // end

        s3.createBucket(bucketName);

        System.out.format("Bucket %s has been created.\n", bucketName);
        return true;

    }

    public List<String> listObjects(String bucketName) {

        List<String> objectNameList = new ArrayList<>();

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withMaxKeys(300);
        ObjectListing objectListing = s3.listObjects(listObjectsRequest);

        for(S3ObjectSummary s : objectListing.getObjectSummaries()) {
            String objectName = s.getKey();
            objectNameList.add(objectName);
        }

        return objectNameList;
    }

    public boolean objectExists(String bucketName, String objectName) {

        List<String> objectNameList = listObjects(bucketName);

        if (objectNameList.contains(objectName))
            return true;

        System.out.println(bucketName + " 에 " + objectName + " 없음! ");
        return false;
    }

    public OutputStream getObject(String bucketName, String objectName) throws IOException {

        String downloadPath = "/Users/woneyhoney/Desktop/downloadPury";
        S3Object s3Object = s3.getObject(bucketName, objectName);
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadPath));
        byte[] bytesArray = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = s3ObjectInputStream.read(bytesArray)) != -1) {
            outputStream.write(bytesArray, 0, bytesRead);
        }
        outputStream.close();
        s3ObjectInputStream.close();
        System.out.format("Object %s has been downloaded.\n", objectName);

        return outputStream;
    }
}



