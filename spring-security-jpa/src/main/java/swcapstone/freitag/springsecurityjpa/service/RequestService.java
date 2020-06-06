package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class RequestService {

    // Parameter
    protected String getClassNameP(HttpServletRequest request) {
        return request.getParameter("className");
    }

    protected String getProjectNameP(HttpServletRequest request) {
        return request.getParameter("projectName");
    }

    protected String getWorkTypeP(HttpServletRequest request) {
        return request.getParameter("workType");
    }

    protected String getDataTypeP(HttpServletRequest request) {
        return request.getParameter("dataType");
    }

    protected String getSubjectP(HttpServletRequest request) {
        return request.getParameter("subject");
    }

    protected String getWayContentP(HttpServletRequest request) {
        return request.getParameter("wayContent");
    }

    protected String getConditionContentP(HttpServletRequest request) {
        return request.getParameter("conditionContent");
    }

    protected String getDescriptionP(HttpServletRequest request) {
        return request.getParameter("description");
    }

    protected int getTotalDataP(HttpServletRequest request) {
        String strTotalData = request.getParameter("totalData");   // 라벨링은 -1로 설정
        return Integer.parseInt(strTotalData);
    }

    protected String[] getClassNameListP(HttpServletRequest request) {
        return request.getParameterValues("className");
    }

    public int getProjectIdP(HttpServletRequest request) {
        String strProjectId = request.getParameter("projectId");
        return Integer.parseInt(strProjectId);
    }

    protected int getDifficultyP(HttpServletRequest request) {
        String strDifficulty = request.getParameter("difficulty");
        return Integer.parseInt(strDifficulty);
    }

    // Header
    public int getProjectIdH(HttpServletRequest request) {
        String strProjectId = request.getHeader("projectId");
        return Integer.parseInt(strProjectId);
    }

    protected String getBucketNameH(HttpServletRequest request) {
        String bucketName = request.getHeader("bucketName");
        return bucketName;
    }

    protected int getHistoryIdH(HttpServletRequest request) {
        String strHistoryId = request.getHeader("historyId");
        return Integer.parseInt(strHistoryId);
    }

    protected String getDataTypeH(HttpServletRequest request) {
        String dataType = request.getHeader("dataType");    // boundingBox or classification
        return dataType;
    }
}
