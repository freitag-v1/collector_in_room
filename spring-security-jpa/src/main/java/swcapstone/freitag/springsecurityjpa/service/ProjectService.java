package swcapstone.freitag.springsecurityjpa.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ProjectService {
    int howManyProjects(String userId);
    void createProject(HttpServletRequest request, String userId, String bucketName, HttpServletResponse response);
    boolean setExampleContentAndCost(String userId, String exampleContent, HttpServletResponse response);
    // void setCost(String userId, int totalData, HttpServletResponse response);
    int calculateBasicCost(int totalData);
}
