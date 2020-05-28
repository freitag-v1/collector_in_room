package swcapstone.freitag.springsecurityjpa.service;

public class AdvancedWorkService extends WorkService {

    // 교차검증 - Voting!
    // Voting을 통해 validationStatus가 작업후 -> 교차검증완료 변경되면
    // 해당 problem의 project의 progressData++

/*
    private MultipartFile getFile(MultipartHttpServletRequest uploadRequest) {
        MultipartFile uploadFile = uploadRequest.getFile("file");
        return uploadFile;
    }
*/
}
