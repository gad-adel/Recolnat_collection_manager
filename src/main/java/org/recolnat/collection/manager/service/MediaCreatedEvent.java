package org.recolnat.collection.manager.service;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder
@Data
public class MediaCreatedEvent {
    // add file name so we can map with sepecimen
    private String mediaName;
    private String mediaUrl;

}
