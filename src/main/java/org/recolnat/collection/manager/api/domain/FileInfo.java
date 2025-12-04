package org.recolnat.collection.manager.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo {
    private MediaType mediaType;
    private byte[] data;
}
