package swcapstone.freitag.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swcapstone.freitag.project.api.ObjectStorageApiClient;

@Service
public class ObjectStorageService {

    @Autowired
    ObjectStorageApiClient objectStorageApiClient;

}
