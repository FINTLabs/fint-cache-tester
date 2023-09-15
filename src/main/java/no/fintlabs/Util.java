package no.fintlabs;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.utdanning.elev.ElevResource;

@Slf4j
public class Util {

    public static String getId(ElevResource elevResource) {
        return elevResource.getElevnummer().getIdentifikatorverdi();
    }

    public static void logMemoryUsage() {

        System.gc();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            log.error("Couldn't sleep");
//        }

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double usedMemoryInMB = (double) usedMemory / (1024 * 1024);

        log.info("Memory usage: {}MB", String.format("%.2f", usedMemoryInMB));
    }
}
