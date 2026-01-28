package org.recolnat.collection.manager.institution.api.domain.service;

import org.recolnat.collection.manager.connector.api.domain.MediaDetailsOutput;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Objects;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ConditionalOnProperty(value = "media-ws", havingValue = "local")
public class MediaLocalImpl{
    private static final String MEDIA_URL ="https://recolnat-dev.test.mnhn.fr/client/_next/image?url=%2Fclient%2Fimages%2F120122e8-d723-42a8-aa93-8377a94d16f2.png&w=256&q=75";
   
    public MediaDetailsOutput saveLogo(MultipartFile file) throws IOException {
        return (Objects.nonNull(file))?MediaDetailsOutput.builder().url(MEDIA_URL).build(): null;
    }
}
