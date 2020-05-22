package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import swcapstone.freitag.springsecurityjpa.api.ObjectStorageApiClient;
import swcapstone.freitag.springsecurityjpa.domain.dto.ClassDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProjectDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ClassEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.domain.repository.ClassRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepository;
import swcapstone.freitag.springsecurityjpa.domain.repository.ProjectRepositoryImpl;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ClassRepository classRepository;
    @Autowired
    ProjectRepositoryImpl projectRepositoryImpl;
    @Autowired
    ObjectStorageApiClient objectStorageApiClient;

    private static final int COST_PER_DATA = 50;
    private int projectIdTurn;

    public int howManyProjects(String userId) {
        List<ProjectEntity> projectEntityList = projectRepository.findAllByUserId(userId);
        return projectEntityList.size();
    }

    @Transactional
    public void createProject(HttpServletRequest request, String userId, String bucketName, HttpServletResponse response)
            throws NullPointerException {

        ++projectIdTurn;
        int projectId = this.projectIdTurn;
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

        ProjectDto projectDto = new ProjectDto(projectId, userId, projectName, bucketName, "없음", workType, dataType, subject,
                0, wayContent, conditionContent, "없음", description, totalData, 0, 0);

        if(projectRepository.save(projectDto.toEntity()) != null) {
            response.setHeader("create", "success");
            response.setHeader("projectId", String.valueOf(projectId));
            return;
        }

        response.setHeader("create", "fail");
        return;
    }

    @Transactional
    public void uploadExampleContent(String userId, HttpServletRequest request, MultipartFile file, HttpServletResponse response) throws Exception {

        String fileName = file.getOriginalFilename();
        String bucketName = request.getHeader("bucketName");

        File destinationFile = new File("/Users/woneyhoney/Desktop/files/" + fileName);
        // MultipartFile.transferTo() : 요청 시점의 임시 파일을 로컬 파일 시스템에 영구적으로 복사하는 역할을 수행
        file.transferTo(destinationFile);

        String exampleContent = objectStorageApiClient.putObject(bucketName, destinationFile);

        // 예시 데이터 object의 Etag를 exampleContent로 지정하고 cost 설정하고 헤더에 붙이기
        if(setExampleContent(userId, exampleContent, response)) {
            // 수집 프로젝트는 예시 데이터 업로드 성공하면 바로 결제할 수 있도록 cost 계산해서 헤더에 쓰기
            if(isCollection(userId))
                setCost(userId, response);

            // System.out.println("status: 없음 - 결제만 하면 됨. 그 외 프로젝트 생성 작업은 모두 완료");
            else
                response.setHeader("bucketName", bucketName);
        }
    }

    @Transactional
    public boolean setExampleContent(String userId, String exampleContent, HttpServletResponse response) {

        if(exampleContent == null)
            return false;

        // System.out.println("exampleContent: "+exampleContent);
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserIdAndStatus(userId, "없음");

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setExampleContent(exampleContent);

            projectRepository.save(selectProject);
            response.setHeader("example", "success");
        });

        return  true;
    }

    @Transactional
    public void setCost(String userId, HttpServletResponse response) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserIdAndStatus(userId, "없음");

        int totalData = projectEntityWrapper.get().getTotalData();
        int cost = calculateBasicCost(totalData);

        if( cost == -1) {
            return;
        }

        System.out.println("<cost>");
        System.out.println(cost);

        projectEntityWrapper.ifPresent(selectProject -> {
           selectProject.setCost(cost);

            projectRepository.save(selectProject);
           response.setHeader("cost", String.valueOf(cost));
        });

    }

    private boolean isCollection(String userId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserIdAndStatus(userId, "없음");

        if(projectEntityWrapper.isPresent() &
            projectEntityWrapper.get().getWorkType().equals("collection"))
            return true;

        return false;
    }

    @Transactional
    public void setStatus(String userId, HttpServletResponse response) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserIdAndStatus(userId, "없음");

        projectEntityWrapper.ifPresent(selectProject -> {
            selectProject.setStatus("진행중");

            projectRepository.save(selectProject);
            response.setHeader("status", "진행중");
        });
    }

    public int calculateBasicCost(int totalData) {
        return COST_PER_DATA * totalData;
    }

    // 결제 미완료된 프로젝트 비용 가져오기
    public int getCost(String userId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserIdAndStatus(userId, "없음");

        if(projectEntityWrapper.isPresent()) {
            return projectEntityWrapper.get().getCost();
        }

        return -1;
    }


    protected String getBucketName(String userId) {
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByUserIdAndStatus(userId, "없음");

        if(projectEntityWrapper.isPresent()) {
            return projectEntityWrapper.get().getBucketName();
        }

        return null;
    }

    // 프로젝트 검색 결과 반환
    // workType, dataType, subject, difficulty
    public List<ProjectDtoWithClassDto> getSearchResults(HttpServletRequest request, HttpServletResponse response) {

        String workType = request.getParameter("workType"); // collection / labelling
        String dataType = request.getParameter("dataType"); // image, audio, text / boundingBox, classfication
        String subject = request.getParameter("subject");

        String strDifficulty = request.getParameter("difficulty");
        int difficulty = Integer.parseInt(strDifficulty);

        List<ProjectEntity> projectEntityList = projectRepositoryImpl.findDynamicQuery(workType, dataType, subject, difficulty);

        if(!projectEntityList.isEmpty()) {
            List<ProjectDto> searchResults = ObjectMapperUtils.mapAll(projectEntityList, ProjectDto.class);

            // 클래스 정보도 함께 주기 위해서 ProjectDto -> ProjectDtoWithClassDto 변환
            List<ProjectDtoWithClassDto> searchResultsWithClassNames = withClassDtos(searchResults);

            response.setHeader("search", "success");
            return searchResultsWithClassNames;
        }

        response.setHeader("search", "fail");
        return null;
    }

    private List<ProjectDtoWithClassDto> withClassDtos(List<ProjectDto> projectDtos) {

        if(projectDtos.isEmpty())
            return null;

        List<ProjectDtoWithClassDto> results = new ArrayList<>();

        for(ProjectDto p : projectDtos) {

            int projectId = p.getProjectId();

            List<ClassEntity> classEntities = classRepository.findAllByProjectId(projectId);
            List<ClassDto> classNameList = ObjectMapperUtils.mapAll(classEntities, ClassDto.class);

            ProjectDtoWithClassDto pc = new ProjectDtoWithClassDto(p, classNameList);
            results.add(pc);
        }

        return results;
    }


    @Transactional
    public void createClass(String userId, HttpServletRequest request, HttpServletResponse response) {

        String[] classNameList = request.getParameterValues("className");
        String strProjectId = request.getParameter("projectId");

        if(classNameList == null) {
            response.setHeader("class", "fail");
            return;
        }

        int projectId = Integer.parseInt(strProjectId);

        for(String className : classNameList) {
            ClassDto classDto = new ClassDto(projectId, className);
            if(classRepository.save(classDto.toEntity()) == null) {
                response.setHeader("class", "fail");
                return;
            }
        }

        String bucketName = getBucketName(userId);

        response.setHeader("bucketName", bucketName);
        response.setHeader("class", "success");
        return;

    }
}
