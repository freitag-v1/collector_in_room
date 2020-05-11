package swcapstone.freitag.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swcapstone.freitag.project.domain.ProjectDto;
import swcapstone.freitag.project.domain.ProjectEntity;
import swcapstone.freitag.project.domain.ProjectRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

@Service
public class CollectionProjectService implements ProjectService {

    @Autowired
    ProjectRepository projectRepository;

    private static final int COST_PER_DATA = 100;

    @Override
    public int howManyProjects(String userId) {
        List<ProjectEntity> projectEntityList = projectRepository.findAllByUserId(userId);
        return projectEntityList.size();
    }

    @Override
    public void createProject(HttpServletRequest request, String bucketName, HttpServletResponse response)
            throws NullPointerException {

        String userId = request.getParameter("userId");
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
                0, wayContent, conditionContent, null, description, totalData, 0, 0);


        projectRepository.save(projectDto.toEntity());
        System.out.println("데이터 업로드 전 - 그 외 필요한 사용자 입력 필드는 DB 저장 완료");
        response.setHeader("bucketName", bucketName);

    }

    @Override
    public boolean setExampleContent(/*HttpServletRequest request, */String exampleContent, HttpServletResponse response) {
        /*
        String token = request.getHeader(JwtProperties.HEADER_STRING);

        if(token != null) {
            // 토큰을 파싱해서 Decode!
            String userId = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET.getBytes()))
                    .build()
                    .verify(token.replace(JwtProperties.TOKEN_PREFIX, ""))
                    .getSubject(); */

        // 실제로는 위에처럼 하는데 임시로 userId = woneyhoney로 박아놓겠음
        String userId = "woneyhoney";
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserId(userId);

        if(projectEntityWrapper != null) {
            projectEntityWrapper.ifPresent(selectProject -> {
                selectProject.setExampleContent(exampleContent);
            });
            // cost 설정 및 response 헤더에 넣기
            setCost(userId, projectEntityWrapper.get().getTotalData(), response);
            return true;
        }

        return false;
    }

    @Override
    public void setCost(String userId, int totalData, HttpServletResponse response) {

        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserId(userId);

        int cost = calculateBasicCost(totalData);

        if(projectEntityWrapper != null) {
            projectEntityWrapper.ifPresent(selectProject -> {
                selectProject.setCost(cost);
            });

            response.setHeader("cost", String.valueOf(cost));
            return;
        }

    }

    @Override
    public int calculateBasicCost(int totalData) {
        return COST_PER_DATA * totalData;
    }
}
