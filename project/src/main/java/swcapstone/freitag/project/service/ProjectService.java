package swcapstone.freitag.project.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ProjectService {
    public boolean createProject(HttpServletRequest request, HttpServletResponse response);
}
