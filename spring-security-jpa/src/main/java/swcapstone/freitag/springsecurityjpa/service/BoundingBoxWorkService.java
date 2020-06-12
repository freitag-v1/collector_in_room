package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swcapstone.freitag.springsecurityjpa.domain.dto.*;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class BoundingBoxWorkService extends ClassificationWorkService {

    // 바운딩 박스 작업을 시작하면 문제 한 세트(5개) 제공
    public List<ProblemDtoWithClassDto> provideBoundingBoxProblems
        (String userId, HttpServletRequest request, HttpServletResponse response) {

        int projectId = requestService.getProjectIdH(request);
        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("해당 프로젝트를 찾을 수 없음");
            response.setHeader("problems", "fail");
            return null;
        }

        List<ProblemEntity> selectedProblems
                = problemRepositoryImpl.labellingProblem(projectId, "작업전", 5);

        if (selectedProblems.isEmpty()) {
            System.out.println("========================");
            System.out.println("selectedProblems.isEmpty()");
            response.setHeader("problems", "fail");
            return null;
        }

        List<ProblemDto> problemSet = ObjectMapperUtils.mapAll(selectedProblems, ProblemDto.class);

        // 클래스 정보도 함께 주기 위해서 ProblemDto -> ProblemDtoWithClassDto 변환
        List<ProblemDtoWithClassDto> problemSetWithClassNames = withClassDtos(problemSet);

        if(problemSetWithClassNames.isEmpty() || problemSetWithClassNames.size() != 5) {
            System.out.println("========================");
            System.out.println("바운딩박스 문제 한 세트(5개)를 만들 수가 없음");
            response.setHeader("problems", "fail");
            return null;
        }

        String dataType = projectEntityWrapper.get().getDataType();
        int historyId = saveLabellingWorkHistory(userId, dataType, problemSet);

        response.setHeader("workHistory", String.valueOf(historyId));
        response.setHeader("problems", "success");

        return problemSetWithClassNames;
    }


    // 바운딩 박스 작업
    public boolean boundingBoxWork(String userId, LinkedHashMap<String, Object> parameterMap,
                                   HttpServletRequest request, HttpServletResponse response) {

        if (parameterMap.isEmpty()) {
            System.out.println("========================");
            System.out.println("아무것도 오지 않았음");
            response.setHeader("map", "fail");
            return false;
        }

        int projectId = requestService.getProjectIdH(request);
        int historyId = requestService.getHistoryIdH(request);

        int problemId = requestService.getProblemIdP(request);

        Optional<ProjectEntity> projectEntityWrapper = projectRepository.findByProjectId(projectId);

        if (projectEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("해당 프로젝트를 찾을 수 없음");
            response.setHeader("project", "fail");
            return false;
        }

        LinkedHashMap<String, String> classNameBoundingBoxCoordinates = new LinkedHashMap<>();

        for(String className : parameterMap.keySet()) {
            classNameBoundingBoxCoordinates.put(className, parameterMap.get(className).toString());
        }

        for(Map.Entry<String, String> entry : classNameBoundingBoxCoordinates.entrySet()) {

            String className = entry.getKey();
            String boxCoordinates = entry.getValue();

            if(saveBoundingBox(problemId, className, boxCoordinates)) {
                // 답이 제대로 저장이 되면 problem_table에서 해당 problem의 validation_status 변경
                updateValidationStatus(historyId, problemId);
            } else {
                // 답이 제대로 저장이 안되면 labellingWorkHistory 삭제 추가 ***
                labellingWorkHistoryRepository.deleteByHistoryId(historyId);
                System.out.println("========================");
                System.out.println("문제의 답을 저장할 수가 없음. 라벨링 작업 기록 삭제되었으니 작업 재시작 요망");
                response.setHeader("answer", "fail - 작업 다시 시작");
                return false;
            }
        }

        if(saveAnswer(problemId, "", userId)) {
            // 교차검증 문제 만들기
            createCrossValidationProblem(projectId, problemId);
            projectService.setProgressData(projectId, 1);
            return true;
        }

        return false;
    }

    @Transactional
    protected boolean saveBoundingBox(int problemId, String className, String boxCoordinate) {

        // 동일 클래스에 대해 바운딩 박스를 여러개 쳤을 경우
        String boundingBoxList[] = boxCoordinate.split("&");

        if (boundingBoxList.length < 1) {
            System.out.println("========================");
            System.out.println("바운딩 박스가 1개 미만임");
            return false;
        }

        for(String boundingBox : boundingBoxList) {

            String coordinates[] = boundingBox.split(" ");
            if (coordinates.length != 4) {
                System.out.println("========================");
                System.out.println("좌표가 4개가 아님");
                return false;
            }

            int boxId = getBoxIdTurn();
            BoundingBoxDto boundingBoxDto = new BoundingBoxDto(boxId, problemId, className, boundingBox);
            boundingBoxRepository.save(boundingBoxDto.toEntity());

        }

        return true;

    }

    @Override
    protected void updateValidationStatus(int historyId, int problemId) {
        Optional<ProblemEntity> problemEntityWrapper = problemRepository.findByProblemId(problemId);

        if (problemEntityWrapper.isEmpty()) {
            System.out.println("========================");
            System.out.println("문제를 찾을 수 없음");
            return;
        }

        problemEntityWrapper.ifPresent(selectProblem -> {
            selectProblem.setValidationStatus("작업후");   // 작업전 -> 작업후
            problemRepository.save(selectProblem);
        });
    }
}
