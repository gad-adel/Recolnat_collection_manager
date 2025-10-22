package org.recolnat.collection.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.repository.entity.MediaJPA;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class MediaComponent {

    /**
     * Vérifie qu'il n'y a qu'une image de couverture dans la liste (flag isCover à true).
     * si aucun alors on ne fait rien,
     * si il y en a plus qu'une alors on garde la première a isCover: true et
     * les autres sont mises à false.
     */
    public void ensureSingleCoverIsSet(Set<MediaJPA> medias) {
        if (!checkMedias(medias)) {
            return;
        }
        List<MediaJPA> mediaList = new ArrayList<>(medias);
        int foundCoverIndex = -1;
        isMoreThanOneCover(mediaList, foundCoverIndex);
    }

    private boolean checkMedias(Set<MediaJPA> medias) {
        if (medias == null || medias.isEmpty()) {
            log.debug("No media found, skipping cover check.");
            return false;
        }
        return true;
    }

    private void isMoreThanOneCover(List<MediaJPA> mediaList, int foundCoverIndex) {
        for (int i = 0; i < mediaList.size(); i++) {
            MediaJPA media = mediaList.get(i);
            boolean isCurrentlyCover = Boolean.TRUE.equals(media.getIsCover());

            if (isCurrentlyCover) {
                if (foundCoverIndex == -1) {
                    foundCoverIndex = i;
                } else {
                    media.setIsCover(false);
                }
            } else if (media.getIsCover() == null) {
                media.setIsCover(false);
            }
        }
    }
}
