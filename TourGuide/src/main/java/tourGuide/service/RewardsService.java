package tourGuide.service;

import org.springframework.stereotype.Service;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tourGuide.proxy.gpsUtil.GpsUtilProxy;
import tourGuide.proxy.gpsUtil.dto.Attraction;
import tourGuide.proxy.gpsUtil.dto.Location;
import tourGuide.proxy.gpsUtil.dto.VisitedLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtilProxy gpsUtilService;
    private final RewardCentralService rewardCentralService;

    public RewardsService(GpsUtilProxy gpsUtilService, RewardCentralService rewardCentralService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
    }

    public void setAttractionProximityRange(int attractionProximityRange) {
        this.attractionProximityRange = attractionProximityRange;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    public CompletableFuture<Void> calculateRewards(User user) {
        List<VisitedLocation> userLocations = new ArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtilService.getAttractions();
        // Pour construire une Reward pour l'utilisateur on a besoin du rewardPoint et de la localisation asscociée
        // on collecte les résultats des appels asynchrones pour chaque attraction
        Map<Attraction, Integer> rewardPoints = new HashMap<>();
        // on collecte la localisation de l'utilisateur pour chaque attraction
        Map<Attraction, VisitedLocation> rewardVisitedLocations = new HashMap<>();

        // on collecte les appels asynchrones pour chaque attraction
        Map<Attraction, CompletableFuture<Void>> rewardFutures = new HashMap<>();


        //On parcours les lieux visité par l'utilisateur
        for (VisitedLocation visitedLocation : userLocations) {
            //on parcours les lieux gps des attractions
            for (Attraction attraction : attractions) {
                // on regard si l'user à deja des reward sur cette attraction
                if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                    //on verifie que la position de l'utilisateur est proche de l'attractions
                    if (nearAttraction(visitedLocation, attraction)) {
                        //on lui attribut le reward
                        rewardFutures.putIfAbsent(
                                attraction, rewardCentralService.getAttractionRewardPoints(attraction, user)
                                        .thenAccept(rewardPoint -> rewardPoints.put(attraction, rewardPoint))
                        );
                        rewardVisitedLocations.putIfAbsent(
                                attraction,
                                visitedLocation
                        );


                    }
                }
            }
        }
        return CompletableFuture.allOf(rewardFutures.values().toArray(new CompletableFuture[0]))
                .thenAccept(v -> rewardPoints.forEach((attraction, rewardPoint) -> user.addUserReward(
                        new UserReward(
                                rewardVisitedLocations.get(attraction),
                                attraction,
                                rewardPoint
                        ))
                ));

    }

    public boolean isWithinAttractionProximity(double distance) {
        return distance <= attractionProximityRange;
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return isWithinAttractionProximity(getDistance(attraction, location));
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }
}
