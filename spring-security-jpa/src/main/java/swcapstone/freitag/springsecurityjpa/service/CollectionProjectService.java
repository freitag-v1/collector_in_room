package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepository;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Service
public class CollectionProjectService implements ProjectService {

    @Autowired
    ProjectRepository projectRepository;

    private static final int COST_PER_DATA = 50;

    @Override
    public int howManyProjects(String userId) {
        List<ProjectEntity> projectEntityList = projectRepository.findAllByUserId(userId);
        return projectEntityList.size();
    }

    @Transactional
    @Override
    public void createProject(HttpServletRequest request, String userId, String bucketName, HttpServletResponse response)
            throws NullPointerException {

        String projectName = request.getParameter("projectName");
        // String bucketName;  // 의뢰자가 업로드하는 라벨링 데이터를 담을 버킷 - userId+projectName 조합
        // String status;  // 없음, 진행중, 완료
        String workType = request.getParameter("workType"); // collection, boundingBox, classification
        String dataType = request.getParameter("dataType"); // image, audio, text
        String subject = request.getParameter("subject");
        // int difficulty;
        String wayContent = request.getParameter("wayContent");  // 작업 방법
        String conditionContent = request.getParameter("conditionContent");    // 작업 조건
        // String exampleContent;
        String description = request.getParameter("description"); // 프로젝트 설명
        String total = request.getParameter("totalData");
        int totalData = Integer.parseInt(total);    // 의뢰자가 원하는 수집 데이터 개수
        // int progressData;
        // int cost;

        ProjectDto projectDto = new ProjectDto(userId, projectName, bucketName, "없음", workType, dataType, subject,
                0, wayContent, conditionContent, "없음", description, totalData, 0, 0);

        projectRepository.save(projectDto.toEntity());
        System.out.println("데이터 업로드 전 - 그 외 필요한 사용자 입력 필드는 DB 저장 완료");
        response.setHeader("bucketName", bucketName);

    }

    @Transactional
    @Override
    public boolean setExampleContentAndCost(String userId, String exampleContent, HttpServletResponse response) {

        if(exampleContent == null)
            return false;

        System.out.println("exampleContent: "+exampleContent);

        ProjectEntity projectEntity = findNotYetPaidProject(userId);

        if (projectEntity != null) {

            int totalData = projectEntity.getTotalData();
            int cost = calculateBasicCost(totalData);

            if( cost == -1)
                return false;

            projectEntity.setExampleContent(exampleContent);
            projectEntity.setCost(cost);

            // cost response 헤더에 넣기
            response.setHeader("cost", String.valueOf(cost));
            return true;
        }

        return false;
    }

    @Override
    public int calculateBasicCost(int totalData) {
        return COST_PER_DATA * totalData;
    }

    // 결제 미완료된 프로젝트 비용 가져오기
    public int getCost(String userId) {
        ProjectEntity projectEntity = findNotYetPaidProject(userId);

        if(projectEntity != null) {
            return projectEntity.getCost();
        }

        return -1;
    }


    // 결제 미완료된 프로젝트 찾기
    private ProjectEntity findNotYetPaidProject(String userId) {

        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByStatus("없음");

        if (projectEntityWrapper.isPresent()) {
            ProjectEntity projectEntity = projectEntityWrapper.get();

            if(projectEntity.getUserId().equals(userId))
                return projectEntity;
        }

        return null;
    }
}

