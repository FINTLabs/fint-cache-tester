package no.fintlabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObject;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevResources;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
@Service
public class ResourceGenerator {

    private Map<String, CacheObject<ElevResource>> data = new HashMap<>();
    private AtomicInteger elevCounter = new AtomicInteger();

    public void generateData(int amount) {

        List<ElevResource> resourcesFromFile = readJsonFile();

        for (int i = 0; i < amount; i++) {

            for (var resource : resourcesFromFile) {

                int id = elevCounter.incrementAndGet();
                ElevResource cloned = SerializationUtils.clone(resource);

                Identifikator identifikator = new Identifikator();
                identifikator.setIdentifikatorverdi(String.valueOf(id));
                cloned.setElevnummer(identifikator);

                CacheObject<ElevResource> cacheObject = new CacheObject<>(cloned, hashCodes(cloned));
                data.put(String.valueOf(id), cacheObject);
            }
        }
    }

    public List<CacheObject<ElevResource>> getList() {
        return data.values().stream().toList();
    }

    public Map<String, ElevResource> getMap() {
        return data.entrySet().stream().collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue().getObject()));
    }

    private List<ElevResource> readJsonFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new ClassPathResource("elev-data.json").getFile();
            return objectMapper.readValue(file, ElevResources.class).getContent();
        } catch (Exception e) {
            log.error(e.toString());
            return new ArrayList<>();
        }
    }

    private int[] hashCodes(ElevResource elev) {
        IntStream.Builder builder = IntStream.builder();
        if (!isNull(elev.getBrukernavn()) && !isEmpty(elev.getBrukernavn().getIdentifikatorverdi())) {
            builder.add(elev.getBrukernavn().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(elev.getElevnummer()) && !isEmpty(elev.getElevnummer().getIdentifikatorverdi())) {
            builder.add(elev.getElevnummer().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(elev.getFeidenavn()) && !isEmpty(elev.getFeidenavn().getIdentifikatorverdi())) {
            builder.add(elev.getFeidenavn().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(elev.getSystemId()) && !isEmpty(elev.getSystemId().getIdentifikatorverdi())) {
            builder.add(elev.getSystemId().getIdentifikatorverdi().hashCode());
        }

        return builder.build().toArray();
    }
}
