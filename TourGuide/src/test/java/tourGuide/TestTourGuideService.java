package tourGuide;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestTourGuideService {

    @Test
    public void getUserLocation() {
        GpsUtilService gpsUtil = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    // Not yet implemented
    @Test
    public void getNearbyAttractions() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
        rewardsService.setAttractionProximityRange(Integer.MAX_VALUE);


        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();

        List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }

    public void getTripDeals() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(10, providers.size());
    }
    @Test
    public void getAllUsersLocations() {
        InternalTestHelper.setInternalUserNumber(5);
        TourGuideService tourGuideService = new TourGuideService(null, null);

        Map<UUID, Location> allUserLocations = tourGuideService.getAllUsersLocations();
        assertEquals(5, allUserLocations.size());
        for (Map.Entry<UUID, Location> entry : allUserLocations.entrySet()) {
            UUID id = entry.getKey();
            Location location = entry.getValue();
            assertNotNull(id);
            assertNotNull(location);
        }
    }
}
