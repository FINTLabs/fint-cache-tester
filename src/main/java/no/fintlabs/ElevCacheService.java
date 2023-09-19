package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.cache.CacheService;
import no.fint.cache.model.CacheObject;
import no.fint.event.model.Event;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.utdanning.elev.Elev;
import no.fint.model.utdanning.elev.ElevActions;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ElevCacheService extends CacheService<ElevResource> {

    private final ResourceGenerator resourceGenerator;
    private long lastUpdated = 0;

    public ElevCacheService(ResourceGenerator resourceGenerator) {
        super(Elev.class.getSimpleName().toLowerCase(), ElevActions.GET_ALL_ELEV, ElevActions.UPDATE_ELEV);
        this.resourceGenerator = resourceGenerator;
    }

    public void checkLastUpdated() {
        List<Long> updatedTimestamps = this.getCache(DataTesterService.ORG_ID).get()
                .streamSince(lastUpdated)
                .map(l->l.getLastUpdated())
                .collect(Collectors.toList());

        if (updatedTimestamps.size() == 0) {
            log.info("Ingen elementer i cachet har fått ny last-updated");
        } else {
            log.info(updatedTimestamps.size() + " elementer har nyere last-updated");
            lastUpdated = Collections.max(updatedTimestamps);
        }
    }

    @Override
    public void onAction(Event event) {
    }

    @Override
    public void populateCache(String s) {

    }
}
