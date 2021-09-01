package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtilService gpsUtilService;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;

    public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                gpsUtilService.getUserLocation(user).join();
        return visitedLocation;
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public Map<UUID, Location> getAllUsersLocations() {
        Map<UUID, Location> userLocations = new HashMap<>();
        for (User user : getAllUsers()) {
            userLocations.put(user.getUserId(), Optional.ofNullable(user.getLastVisitedLocation())
                    .map(visitedLocation -> visitedLocation.location)
                    .orElse(null));
        }
        return userLocations;
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        return gpsUtilService.getUserLocation(user)
                .thenApply(visitedLocation -> {
                    user.addToVisitedLocations(visitedLocation);
                    rewardsService.calculateRewards(user).join();
                    return visitedLocation;
                });
    }


    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        return getNearByAttractions(visitedLocation, 5);
    }

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation, int numberOfAttractions) {
        // On collecte toutes les attractions proches
        Map<Attraction, Double> nearbyAttractions = new HashMap<>();
        List<Attraction> attractions = gpsUtilService.getAttractions();
        if (numberOfAttractions >= attractions.size()) {
            return attractions;
        }
        for (Attraction attraction : attractions) {
            double distance = rewardsService.getDistance(attraction, visitedLocation.location);
            if (rewardsService.isWithinAttractionProximity(distance)) {
                nearbyAttractions.put(attraction, distance);
            }
        }
        return nearbyAttractions.entrySet()
                .stream()
                // on tri les attraction par distance
                .sorted(Map.Entry.comparingByValue())
                // on récupère l'attraction
                .map(Map.Entry::getKey)
                // on limit le nombre à retrouner
                .limit(numberOfAttractions)
                .collect(Collectors.toList());
    }


    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}
