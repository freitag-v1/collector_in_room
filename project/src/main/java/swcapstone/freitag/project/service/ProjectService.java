package swcapstone.freitag.project.service;

import swcapstone.freitag.project.domain.ProjectDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ProjectService {
    int howManyProjects(String userId);
    void createProject(HttpServletRequest request, String bucketName, HttpServletResponse response);
    boolean setExampleContent(/*HttpServletRequest request, */String exampleContent, HttpServletResponse response);
    void setCost(String userId, int totalData, HttpServletResponse response);
    int calculateBasicCost(int totalData);
}
