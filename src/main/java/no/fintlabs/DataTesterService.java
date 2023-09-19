package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.model.CacheObject;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.utdanning.elev.ElevResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import no.fint.cache.model.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DataTesterService {

    public static final String ORG_ID = "fintlabs.no";
    private static final int POPULATE_COUNT = 5;
    private static final int FAIL_FILTER_COUNT = 1;

    private final ElevCacheService elevCacheService;
    private final ResourceGenerator resourceGenerator;
    private boolean firstRun = true;

    public DataTesterService(ElevCacheService elevCacheService, ResourceGenerator resourceGenerator) {
        this.elevCacheService = elevCacheService;
        this.resourceGenerator = resourceGenerator;
        elevCacheService.createCache(ORG_ID);
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 5000)
    public void check() {
        long startTime = System.nanoTime();
        log.info("### Run check ###");

        if (firstRun) {
            Util.logMemoryUsage();
            log.info("Generating data");
            resourceGenerator.generateData(POPULATE_COUNT);
            firstRun = false;
        }

        addToCache(resourceGenerator.getList());
        Util.logMemoryUsage();

        timeElementNotFound(FAIL_FILTER_COUNT);
        checkForManipulationOfData();
        elevCacheService.checkLastUpdated();

        long timeElapsed = System.nanoTime() - startTime;
        double elapsedTimeInSeconds = (double) timeElapsed / 1_000_000_000.0;

        log.info("Total run time: " + String.format("%.1f", elapsedTimeInSeconds) + " seconds");
    }

    // To check if we manipulate any data after running methods from FintCache
    public void checkForManipulationOfData() {
        if (!isDataManipulated()) {
            log.info("Cache data consistent with master data");
        } else {
            throw new IllegalArgumentException("Data has been manipulated");
        }
    }

    public boolean isDataManipulated() {
        var fromCache = elevCacheService.getCache(ORG_ID).get()
                .stream()
                .map(l -> l.getObject()) // don't change into method reference!!
                .toList();

        var orginalData = resourceGenerator.getMap();

        if (fromCache.size() != orginalData.size()) return true;

        for (var cachedElev : fromCache) {
            if (!orginalData.containsKey(Util.getId(cachedElev))) return true;
        }

        return false;
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

    public void timeElementNotFound(int failFilterCount) {
        long startTime = System.nanoTime();

        failFilter(failFilterCount);

        long timeElapsed = System.nanoTime() - startTime;
        double elapsedTimeInSeconds = (double) timeElapsed / 1_000_000_000.0;

        log.info("Time elapsed for " + failFilterCount + " searchs: " + String.format("%.3f", elapsedTimeInSeconds) + " seconds");
    }

    public void addToCache(List<CacheObject<ElevResource>> resouces) {
        elevCacheService.updateCache(ORG_ID, resouces);
        log.info("Elements added: {} (New cache size: {})", resouces.size(), elevCacheService.getCacheSize(ORG_ID));
    }
}
