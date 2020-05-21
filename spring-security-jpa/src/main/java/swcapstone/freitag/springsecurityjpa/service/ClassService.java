package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.ClassDto;
import swcapstone.freitag.springsecurityjpa.domain.repository.ClassRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class ClassService {

    @Autowired
    ClassRepository classRepository;

    @Transactional
    public void createClass(String bucketName, HttpServletRequest request, HttpServletResponse response) {

        String[] classNameList = request.getParameterValues("className");
        String strProjectId = request.getParameter("projectId");

        int projectId = Integer.parseInt(strProjectId);

        if(classNameList == null) {
            response.setHeader("class", "fail");
            return;
        }

        for(String className : classNameList) {
            ClassDto classDto = new ClassDto(projectId, className);
            if(classRepository.save(classDto.toEntity()) == null) {
                response.setHeader("class", "fail");
                return;
            }
        }

        response.setHeader("bucketName", bucketName);
        response.setHeader("class", "success");
        return;

    }
}
