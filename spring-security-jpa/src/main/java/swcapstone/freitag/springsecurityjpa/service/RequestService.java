package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class RequestService {

    // Parameter
    protected String getClassName(HttpServletRequest request) {
        String className = request.getParameter("className");
        return className;
    }

    // Header
    public int getProjectId(HttpServletRequest request) {
        String strProjectId = request.getHeader("projectId");
        return Integer.parseInt(strProjectId);
    }

    protected String getBucketName(HttpServletRequest request) {
        String bucketName = request.getHeader("bucketName");
        return bucketName;
    }

    protected int getHistoryId(HttpServletRequest request) {
        String strHistoryId = request.getHeader("historyId");
        return Integer.parseInt(strHistoryId);
    }

    protected String getDataType(HttpServletRequest request) {
        String dataType = request.getHeader("dataType");    // boundingBox or classification
        return dataType;
    }
}
