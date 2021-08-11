package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtilService gpsUtilService;
    private final RewardCentralService rewardCentralService;

    public RewardsService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardCentralService = rewardCentralService;
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
        List<CompletableFuture<Void>> rewardPoints = new ArrayList<>();


        //On parcours les lieux visité par l'utilisateur
        for (VisitedLocation visitedLocation : userLocations) {
            //on parcours les lieux gps des attractions
            for (Attraction attraction : attractions) {
                // on regard si l'user à deja des reward sur cette attraction
                if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
                    //on verifie que la position de l'utilisateur est proche de l'attractions
                    if (nearAttraction(visitedLocation, attraction)) {
                        //on lui attribut le reward
                        rewardPoints.add(
                                rewardCentralService.getAttractionRewardPoints(attraction, user)
                                        .thenAccept(reward -> user.addUserReward(new UserReward(visitedLocation, attraction, reward)))
                        );

                    }
                }
            }
        }
        return CompletableFuture.allOf(rewardPoints.toArray(new CompletableFuture[0]));
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
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
