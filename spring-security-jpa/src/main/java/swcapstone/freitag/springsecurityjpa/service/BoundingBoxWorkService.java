package swcapstone.freitag.springsecurityjpa.service;

import org.springframework.stereotype.Service;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDto;
import swcapstone.freitag.springsecurityjpa.domain.dto.ProblemDtoWithClassDto;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProblemEntity;
import swcapstone.freitag.springsecurityjpa.domain.entity.ProjectEntity;
import swcapstone.freitag.springsecurityjpa.utils.ObjectMapperUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        int historyId = saveLabellingWorkHistory(userId, dataType, problemSetWithClassNames);

        response.setHeader("workHistory", String.valueOf(historyId));
        response.setHeader("problems", "success");

        return problemSetWithClassNames;
    }


    // 바운딩 박스 작업
    public boolean boundingBoxWork(String userId, LinkedHashMap<String, Object> parameterMap,
                                   HttpServletRequest request, HttpServletResponse response) {

        int projectId = requestService.getProjectIdH(request);
        int historyId = requestService.getHistoryIdH(request);

        if(parameterMap.size() != 5) {
            // 답이 제대로 안오면 labellingWorkHistory 삭제 추가 ***
            labellingWorkHistoryRepository.deleteByHistoryId(historyId);
            System.out.println("========================");
            System.out.println("문제의 답이 5개가 아님. 라벨링 작업 기록 삭제되었으니 작업 재시작 요망");
            response.setHeader("answer", "fail - 작업 다시 시작");
            return false;
        }

        LinkedHashMap<String, String> problemIdAnswerMap = new LinkedHashMap<>();

        for(String problemId : parameterMap.keySet()) {
            problemIdAnswerMap.put(problemId, parameterMap.get(problemId).toString());
        }

        // 문제 하나씩
        for(Map.Entry<String, String> entry : problemIdAnswerMap.entrySet()) {

            String strProblemId = entry.getKey();
            int problemId = Integer.parseInt(strProblemId);

            String answer = entry.getValue();

            if(saveAnswer(problemId, answer, userId)) {
                // 답이 제대로 저장이 되면 problem_table에서 해당 problem의 validation_status 변경
                updateValidationStatus(historyId, problemId);
                // 교차검증 문제 만들기
                createCrossValidationProblem(projectId, problemId);
            } else {
                // 답이 제대로 저장이 안되면 labellingWorkHistory 삭제 추가 ***
                labellingWorkHistoryRepository.deleteByHistoryId(historyId);
                System.out.println("========================");
                System.out.println("문제의 답을 저장할 수가 없음. 라벨링 작업 기록 삭제되었으니 작업 재시작 요망");
                response.setHeader("answer", "fail - 작업 다시 시작");
                return false;
            }

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
