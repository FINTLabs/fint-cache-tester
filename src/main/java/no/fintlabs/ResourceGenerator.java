package no.fintlabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevResources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ResourceGenerator {

    @PostConstruct
    public List<ElevResource> getElevResourcesFromFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new ClassPathResource("elev-data.json").getFile();
            ElevResources elevResources = objectMapper.readValue(file, ElevResources.class);
            return elevResources.getContent();
        } catch (Exception e) {
            log.error(e.toString());
            return new ArrayList<>();
        }
    }

}
