package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GpsUtilService {
    private final GpsUtil gpsUtil;

    public GpsUtilService() {
        this.gpsUtil = new GpsUtil();
    }

    //Un service qui permet de gerer un pool de thread pour des traitements simultanés (il va gérer les ressource by @Mohamed.)
    private final ExecutorService executorService = Executors.newFixedThreadPool(10000);

    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }

    public CompletableFuture<VisitedLocation> getUserLocation(User user) {
        return CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executorService);
    }

}
