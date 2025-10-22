package org.recolnat.collection.manager.connector.api;

import org.recolnat.collection.manager.connector.api.domain.MediathequeOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface MediathequeService {

    ResponseEntity<MediathequeOutput> savePicture(MultipartFile file) throws IOException;

    ResponseEntity<Void> deletePicture(String uid);
}
