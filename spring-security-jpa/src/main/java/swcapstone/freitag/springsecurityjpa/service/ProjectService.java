package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepositoryImpl;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectRepositoryImpl projectRepositoryImpl;

    private static final int COST_PER_DATA = 50;

    public int howManyProjects(String userId) {
        List<ProjectEntity> projectEntityList = projectRepository.findAllByUserId(userId);
        return projectEntityList.size();
    }

    @Transactional
    public void createProject(HttpServletRequest request, String userId, String bucketName, HttpServletResponse response)
            throws NullPointerException {

        String projectName = request.getParameter("projectName");
        // String bucketName;  // 의뢰자가 업로드하는 라벨링 데이터를 담을 버킷 - userId+projectName 조합
        // String status;  // 없음, 진행중, 완료
        String workType = request.getParameter("workType"); // collection / labelling
        String dataType = request.getParameter("dataType"); // image, audio, text / boundingBox, classfication
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
    public boolean setExampleContent(String userId, String exampleContent, HttpServletResponse response) {

        if(exampleContent == null)
            return false;

        // System.out.println("exampleContent: "+exampleContent);

        ProjectEntity projectEntity = findNotYetPaidProject(userId);

        if (projectEntity != null) {
            projectEntity.setExampleContent(exampleContent);
            response.setHeader("example", "success");
            return true;
        }

        response.setHeader("example", "fail");
        return false;
    }

    @Transactional
    public void setCost(String userId, HttpServletResponse response) {

        ProjectEntity projectEntity = findNotYetPaidProject(userId);

        int totalData = projectEntity.getTotalData();
        int cost = calculateBasicCost(totalData);

        if( cost == -1) {
            return;
        }

        projectEntity.setCost(cost);
        response.setHeader("cost", String.valueOf(cost));

    }

    public boolean isCollection(String userId) {
        ProjectEntity projectEntity = findNotYetPaidProject(userId);

        if(projectEntity != null) {
            if(projectEntity.getWorkType().equals("collection"))
                return true;
        }

        return false;
    }

    @Transactional
    public void setStatus(String userId, HttpServletResponse response) {
        ProjectEntity projectEntity = findNotYetPaidProject(userId);

        if (projectEntity != null) {
            projectEntity.setStatus("진행중");
            String status = projectEntity.getStatus();
            // status response 헤더에 넣기
            response.setHeader("status", status);
        }
    }

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


    // 프로젝트 검색 결과 반환
    // workType, dataType, subject, difficulty
    public List<ProjectDto> getSearchResults(HttpServletRequest request, HttpServletResponse response) {

        String workType = request.getParameter("workType"); // collection / labelling
        String dataType = request.getParameter("dataType"); // image, audio, text / boundingBox, classfication
        String subject = request.getParameter("subject");

        String strDifficulty = request.getParameter("difficulty");
        int difficulty = Integer.parseInt(strDifficulty);

        List<ProjectEntity> projectEntityList = projectRepositoryImpl.findDynamicQuery(workType, dataType, subject, difficulty);
        System.out.println("=================");
        System.out.println(projectEntityList.get(0).getUserId());

        if(!projectEntityList.isEmpty()) {
            List<ProjectDto> searchResults = ObjectMapperUtils.mapAll(projectEntityList, ProjectDto.class);

            System.out.println("=================");
            System.out.println(searchResults.get(0).getUserId());

            response.setHeader("search", "success");
            return searchResults;
        }

        response.setHeader("search", "fail");
        return null;
    }
}
