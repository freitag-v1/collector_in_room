package swcapstone.freitag.springsecurityjpa.api;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import org.springframework.stereotype.Service;

import java.io.File;
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

    // private static final String BUCKET_NAME = "woneyhoney";


    public String putObject(String bucketName, File uploadFile) throws Exception {

        String objectName = uploadFile.getName();

        try {
            PutObjectResult putObjectResult = s3.putObject(bucketName, objectName, uploadFile);
            System.out.format("Object %s has been created.\n", objectName);

            String eTag = putObjectResult.getETag();
            // System.out.println("eTag: "+eTag);
            return eTag;

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
            return true;
        }

        s3.createBucket(bucketName);
        System.out.format("Bucket %s has been created.\n", bucketName);
        return true;

    }
}



