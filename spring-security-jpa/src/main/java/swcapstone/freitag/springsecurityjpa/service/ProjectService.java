package swcapstone.freitag.springsecurityjpa.service;

import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ProjectService {
    int howManyProjects(String userId);
    void createProject(HttpServletRequest request, String userId, String bucketName, HttpServletResponse response);
    boolean setExampleContentAndCost(String userId, String exampleContent, HttpServletResponse response);
    void setStatus(String userId, HttpServletResponse response);
    int calculateBasicCost(int totalData);
    List<ProjectDto> getSearchResults(HttpServletRequest request, HttpServletResponse response);
}
