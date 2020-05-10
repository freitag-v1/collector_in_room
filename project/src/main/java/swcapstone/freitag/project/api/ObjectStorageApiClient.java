package swcapstone.freitag.project.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ObjectStorageApiClient {

    private static final String CHARSET_NAME = "UTF-8";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String AWS_ALGORITHM = "AWS4-HMAC-SHA256";

    private static final String SERVICE_NAME = "s3";
    private static final String REQUEST_TYPE = "aws4_request";

    private static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyyMMdd\'T\'HHmmss\'Z\'");

    private static final String REGION_NAME = "kr-standard";
    private static final String ENDPOINT = "https://kr.object.ncloudstorage.com";

    private static final String ACCESS_KEY = "sQG5BeaHcnvvqK4FI01A";
    private static final String SECRET_KEY = "mvNVjSac240XvnrK4qF39HpoMvvtMQMzUnnNHaRV";

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    HttpHeaders httpHeaders;

    private static byte[] sign(String stringData, byte[] key) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        byte[] data = stringData.getBytes(CHARSET_NAME);
        Mac e = Mac.getInstance(HMAC_ALGORITHM);
        e.init(new SecretKeySpec(key, HMAC_ALGORITHM));
        return e.doFinal(data);
    }

    private static String hash(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest e = MessageDigest.getInstance(HASH_ALGORITHM);
        e.update(text.getBytes(CHARSET_NAME));
        return Hex.encode(e.digest()).toString();
    }

    // Authorization 헤더
    private static String getAuthorization(String accessKey, String scope, String signedHeaders, String signature) {
        String signingCredentials = accessKey + "/" + scope;
        String credential = "Credential=" + signingCredentials;
        String signerHeaders = "SignedHeaders=" + signedHeaders;
        String signatureHeader = "Signature=" + signature;

        StringBuilder authHeaderBuilder = new StringBuilder().append(AWS_ALGORITHM).append(" ")
                .append(credential).append(", ")
                .append(signerHeaders).append(", ")
                .append(signatureHeader);

        return authHeaderBuilder.toString();
    }

    // Authorization 헤더 - Credential
    private static String getScope(String datestamp, String regionName) {
        StringBuilder scopeBuilder = new StringBuilder().append(datestamp).append("/")
                .append(regionName).append("/")
                .append(SERVICE_NAME).append("/")
                .append(REQUEST_TYPE);
        return scopeBuilder.toString();
    }

    // Authorization 헤더 - SignedHeaders
    private static String getSignedHeaders(MultiValueMap<String, String> sortedHeaders) {
        StringBuilder signedHeadersBuilder = new StringBuilder();
        for (String headerName : sortedHeaders.keySet()) {
            signedHeadersBuilder.append(headerName.toLowerCase()).append(";");
        }
        return signedHeadersBuilder.toString();
    }

    // Authorization 헤더 - Signature
    private static String getStandardizedHeaders(MultiValueMap<String, String> sortedHeaders) {
        StringBuilder standardizedHeadersBuilder = new StringBuilder();
        for (String headerName : sortedHeaders.keySet()) {
            standardizedHeadersBuilder.append(headerName.toLowerCase()).append(":").append(sortedHeaders.get(headerName)).append("\n");
        }

        return standardizedHeadersBuilder.toString();
    }

    // Authorization 헤더 - Signature
    private static String getStandardizedQueryParameters(String queryString) throws UnsupportedEncodingException {
        TreeMap<String, String> sortedQueryParameters = new TreeMap<>();
        // sort by key name
        if (queryString != null && !queryString.isEmpty()) {
            String[] queryStringTokens = queryString.split("&");
            for (String field : queryStringTokens) {
                String[] fieldTokens = field.split("=");
                if (fieldTokens.length > 0) {
                    if (fieldTokens.length > 1) {
                        sortedQueryParameters.put(fieldTokens[0], fieldTokens[1]);
                    } else {
                        sortedQueryParameters.put(fieldTokens[0], "");
                    }
                }
            }
        }

        StringBuilder standardizedQueryParametersBuilder = new StringBuilder();
        int count = 0;
        for (String key : sortedQueryParameters.keySet()) {
            if (count > 0) {
                standardizedQueryParametersBuilder.append("&");
            }
            standardizedQueryParametersBuilder.append(key).append("=");

            if (sortedQueryParameters.get(key) != null && !sortedQueryParameters.get(key).isEmpty()) {
                standardizedQueryParametersBuilder.append(URLEncoder.encode(sortedQueryParameters.get(key), CHARSET_NAME));
            }

            count++;
        }
        return standardizedQueryParametersBuilder.toString();
    }

    // Authorization 헤더 - Signature
    private static String getCanonicalRequest(String method, URI uri, String standardizedQueryParameters, String standardizedHeaders, String signedHeaders) {
        StringBuilder canonicalRequestBuilder = new StringBuilder().append(method).append("\n")
                .append(uri.getPath()).append("\n")
                .append(standardizedQueryParameters).append("\n")
                .append(standardizedHeaders).append("\n")
                .append(signedHeaders).append("\n")
                .append(UNSIGNED_PAYLOAD);

        return canonicalRequestBuilder.toString();
    }

    // Authorization 헤더 - Signature
    private static String getStringToSign(String timestamp, String scope, String canonicalRequest) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        StringBuilder stringToSignBuilder = new StringBuilder(AWS_ALGORITHM)
                .append("\n")
                .append(timestamp).append("\n")
                .append(scope).append("\n")
                .append(hash(canonicalRequest));

        return stringToSignBuilder.toString();
    }

    // Authorization 헤더 - Signature
    private static String getSignature(String secretKey, String datestamp, String regionName, String stringToSign) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        byte[] kSecret = ("AWS4" + secretKey).getBytes(CHARSET_NAME);
        byte[] kDate = sign(datestamp, kSecret);
        byte[] kRegion = sign(regionName, kDate);
        byte[] kService = sign(SERVICE_NAME, kRegion);
        byte[] signingKey = sign(REQUEST_TYPE, kService);

        return Hex.encode(sign(stringToSign, signingKey)).toString();
    }

    private static HttpHeaders authorization(HttpRequest request, String regionName, String accessKey, String secretKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {

        HttpHeaders headers = request.getHeaders();
        URI uri = request.getURI();
        String method = request.getMethodValue();

        Date now = new Date();
        DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
        String datestamp = DATE_FORMATTER.format(now);
        String timestamp = TIME_FORMATTER.format(now);

        // X-Amz-Date 헤더
        headers.add("X-Amz-Date", timestamp);

        // X-Amz-Content-Sha256 헤더
        headers.add("X-Amz-Content-Sha256", UNSIGNED_PAYLOAD);

        String standardizedQueryParameters = getStandardizedQueryParameters(uri.getQuery());

        String signedHeaders = getSignedHeaders(headers);
        String standardizedHeaders = getStandardizedHeaders(headers);

        String canonicalRequest = getCanonicalRequest(method, uri, standardizedQueryParameters, standardizedHeaders, signedHeaders);
        System.out.println("> canonicalRequest :");
        System.out.println(canonicalRequest);

        String scope = getScope(datestamp, REGION_NAME);

        String stringToSign = getStringToSign(timestamp, scope, canonicalRequest);
        System.out.println("> stringToSign :");
        System.out.println(stringToSign);

        String signature = getSignature(secretKey, datestamp, regionName, stringToSign);

        // Authorization 헤더
        String authorization = getAuthorization(accessKey, scope, signedHeaders, signature);
        headers.add("Authorization", authorization);

        // Object Storage를 이용하기 위해 필요한 헤더 구성 완료
        return headers;
    }


    public void putObject(String bucketName, File uploadFile) throws Exception {

        String objectName = uploadFile.getName();

        URI uri = new URI(ENDPOINT + "/" + bucketName + "/" + objectName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.add("Host", uri.getHost());

        HttpHeaders finalHeaders = headers;
        HttpRequest request = new HttpRequest() {
            @Override
            public String getMethodValue() {
                return "HOST";
            }

            @Override
            public URI getURI() {
                return uri;
            }

            @Override
            public HttpHeaders getHeaders() {
                return finalHeaders;
            }

        };

        headers = authorization(request, REGION_NAME, ACCESS_KEY, SECRET_KEY);

        Resource resource = new FileSystemResource(uploadFile);
        HttpEntity<Resource> entity = new HttpEntity(resource, headers);

        System.out.println("==================================================");
        System.out.println(entity.getHeaders());
        System.out.println("==================================================");
        System.out.println(entity.getBody());
        System.out.println("==================================================");

        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplate = restTemplateBuilder.errorHandler(new RestTemplateResponseErrorHandler()).build();

        HttpMessageConverter converter = new ResourceHttpMessageConverter();
        restTemplate.getMessageConverters().add(converter);

        ResponseEntity<Resource> response = restTemplate.postForEntity(ENDPOINT, entity, Resource.class);

        // ResponseEntity<Resource> response = restTemplate.exchange(ENDPOINT, HttpMethod.POST, entity, (Class<Resource>) null);
        System.out.println("Result - status ("+ response.getStatusCode() + ") has body: " + response.hasBody());

    }
}
