package swcapstone.freitag.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swcapstone.freitag.project.domain.ProjectDto;
import swcapstone.freitag.project.domain.ProjectRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class CollectionProjectService implements ProjectService {

    @Autowired
    ProjectRepository projectRepository;

    @Override
    public boolean createProject(HttpServletRequest request, HttpServletResponse response)
            throws NullPointerException {

        String userId = request.getParameter("userId");
        String projectName = request.getParameter("projectName");
        // int bucketId;  // 의뢰자가 업로드하는 라벨링 데이터를 담을 버킷
        // String status;  // 없음, 진행중, 완료
        String workType = request.getParameter("workType");
        String dataType = request.getParameter("dataType");
        String subject = request.getParameter("subject");
        // int difficulty;
        String wayContent = request.getParameter("wayContent");  // 작업 방법
        String conditionContent = request.getParameter("conditionContent");    // 작업 조건
        // int exampleContent;
        String description = request.getParameter("description"); // 프로젝트 설명
        // int totalData;
        // int progressData;
        // int cost;

        ProjectDto projectDto = new ProjectDto(userId, projectName, 0, "없음", workType, dataType, subject,
                0, wayContent, conditionContent, 0, description, 0, 0, 0);

        if(projectRepository.save(projectDto.toEntity()) != null) {
            System.out.println("데이터 업로드 전 - 그 외 필요한 사용자 입력 필드는 DB 저장 완료");
            response.setHeader("BasicInfo", "success");
            return true;
        }

        System.out.println("데이터 업로드 전 - 그 외 필요한 사용자 입력 필드를 DB에 저장할 수 없음");
        response.setHeader("BasicInfo", "fail");
        return false;
    }
}
