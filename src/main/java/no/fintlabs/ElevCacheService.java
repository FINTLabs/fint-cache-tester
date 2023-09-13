package no.fintlabs;

import no.fint.cache.CacheService;
import no.fint.event.model.Event;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.utdanning.elev.Elev;
import no.fint.model.utdanning.elev.ElevActions;
import org.springframework.stereotype.Service;

@Service
public class ElevCacheService extends CacheService<ElevResource> {

    private final ResourceGenerator resourceGenerator;

    public ElevCacheService(ResourceGenerator resourceGenerator) {
        super(Elev.class.getSimpleName().toLowerCase(), ElevActions.GET_ALL_ELEV, ElevActions.UPDATE_ELEV);
        this.resourceGenerator = resourceGenerator;
    }


    @Override
    public void onAction(Event event) {

    }

    @Override
    public void populateCache(String s) {

    }
}
