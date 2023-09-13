package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObject;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.utdanning.elev.ElevResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
@Service
public class DataTesterService {

    private static final String ORG_ID = "fintlabs.no";

    private final ElevCacheService elevCacheService;
    private final ResourceGenerator resourceGenerator;
    private List<ElevResource> elevResources;

    public DataTesterService(ElevCacheService elevCacheService, ResourceGenerator resourceGenerator) {
        this.elevCacheService = elevCacheService;
        this.resourceGenerator = resourceGenerator;
        elevResources = new ArrayList<>();
    }

    @PostConstruct
    public void startup() {
        int populateCount = 5;
        elevCacheService.createCache(ORG_ID);

        logMemoryUsage();
        populateCache(populateCount);
        copyElevData();
        logMemoryUsage();

        timeElementNotFound();
        checkForManipulationOfData();
    }

    public void copyElevData() {
        elevResources = elevCacheService.getCache(ORG_ID).get().stream().map(CacheObject::getObject).toList();
    }

    // To check if we manipulate any data after running methods from FintCache
    public void checkForManipulationOfData() {
        if (!isDataManipulated()) {
            log.info("Data was not changed");
        } else {
            throw new IllegalArgumentException("Data has been manipulated");
        }
    }

    public boolean isDataManipulated() {
        List<ElevResource> fromCache = elevCacheService.getCache(ORG_ID).get().stream().map(CacheObject::getObject).toList();
        if (fromCache.size() != elevResources.size()) return true;
        log.info("done 1");

        for (var elevResource : fromCache) {
            if (!elevResources.contains(elevResource)) return true;
        }
        log.info("done 2");

        for (var elevResource : elevResources) {
            if (!fromCache.contains(elevResource)) return true;
        }
        log.info("done 3");

        return false;
    }

    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double usedMemoryInMB = (double) usedMemory / (1024 * 1024);

        log.info("Memory usage: {}MB", String.format("%.2f", usedMemoryInMB));
    }

    public void failFilter(int amount) {
        for (int i = 0; i < amount; i++) {
            elevCacheService.getOne(ORG_ID, 123, (resource) -> Optional
                    .ofNullable(resource)
                    .map(ElevResource::getBrukernavn)
                    .map(Identifikator::getIdentifikatorverdi)
                    .map(s -> s.equals("bob"))
                    .orElse(false));
        }
    }

    public void timeElementNotFound() {
        long startTime = System.nanoTime();

        failFilter(1);

        long timeElapsed = System.nanoTime() - startTime;
        double elapsedTimeInSeconds = (double) timeElapsed / 1_000_000_000.0;

        log.info("Time elapsed: " + String.format("%.4f", elapsedTimeInSeconds) + " seconds");
    }


    public void populateCache(int amount) {
        for (int i = 0; i < amount; i++) {
            List<CacheObject<ElevResource>> collect = resourceGenerator.getElevResourcesFromFile().stream()
                    .map(j -> new CacheObject<>(j, hashCodes(j)))
                    .collect(Collectors.toList());
            elevCacheService.addCache(ORG_ID, collect);
        }
        log.info("Cache size: {}", elevCacheService.getCacheSize(ORG_ID));
    }

    public void populateCache() {
        populateCache(1);
    }

    int[] hashCodes(ElevResource elev) {
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
