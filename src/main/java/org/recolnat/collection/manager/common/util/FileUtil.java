package org.recolnat.collection.manager.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
@Slf4j
public class FileUtil {

    public static boolean createFile(Path pathFichier, MultipartFile file) throws IOException {
        log.debug("Création du fichier {} ", pathFichier);
        // Création du répertoire s'il n'existe pas
        File fileDirectory = pathFichier.getParent().toFile();
        if (!fileDirectory.exists() && !fileDirectory.mkdirs()) {
            log.error("Impossible de créer le dossier : {}", fileDirectory);
        }

        // Création du nouveau fichier
        byte[] content = file == null ? new byte[]{} : file.getBytes();

        Files.write(pathFichier, content);
        return Files.exists(pathFichier);
    }


    public static File getFileFromDirectory(String directory, String filename) throws Exception {
        log.debug("Récupération du fichier {} dans {}", filename, directory);

        if (StringUtils.isEmpty(filename)) {
            throw new Exception("Fichier inexistant");
        }
        File fichier = Paths.get(directory, filename).toFile();
        if (!fichier.exists()) {
            throw new Exception("Fichier inexistant");
        }
        return fichier;
    }

    public static HttpHeaders getHttpHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("file-name", fileName);
        headers.setContentDispositionFormData("file", fileName);
        return headers;
    }


    public record FileResource(String filename, InputStreamResource inputStreamResource) {
    }

}
