package tourGuide;

import java.util.*;
import java.util.concurrent.CompletableFuture;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


import org.mockito.Mockito;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxy.gpsUtil.GpsUtilProxy;
import tourGuide.proxy.gpsUtil.dto.Attraction;
import tourGuide.proxy.gpsUtil.dto.Location;
import tourGuide.proxy.gpsUtil.dto.VisitedLocation;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

import static org.junit.Assert.*;

public class TestTourGuideService {

    GpsUtilProxy gpsUtilService = Mockito.mock(GpsUtilProxy.class);

    @Before
    public void init(){
        List<Attraction> attractions = new ArrayList();
        attractions.add(new Attraction("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D));
        attractions.add(new Attraction("Jackson Hole", "Jackson Hole", "WY", 43.582767D, -110.821999D));
        attractions.add(new Attraction("Mojave National Preserve", "Kelso", "CA", 35.141689D, -115.510399D));
        attractions.add(new Attraction("Joshua Tree National Park", "Joshua Tree National Park", "CA", 33.881866D, -115.90065D));
        attractions.add(new Attraction("Buffalo National River", "St Joe", "AR", 35.985512D, -92.757652D));
        attractions.add(new Attraction("Hot Springs National Park", "Hot Springs", "AR", 34.52153D, -93.042267D));
        Mockito.when(gpsUtilService.getAttractions()).thenReturn(attractions);
    }

    @Test
    public void getUserLocation() {
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

        Mockito.when(gpsUtilService.getUserLocation(user)).thenReturn(CompletableFuture.completedFuture(user.getLastVisitedLocation()));

        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
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
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        Mockito.when(gpsUtilService.getUserLocation(user)).thenReturn(CompletableFuture.completedFuture(user.getLastVisitedLocation()));

        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    // Not yet implemented
    @Test
    public void getNearbyAttractions() {
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
        rewardsService.setAttractionProximityRange(Integer.MAX_VALUE);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        Mockito.when(gpsUtilService.getUserLocation(user)).thenReturn(CompletableFuture.completedFuture(user.getLastVisitedLocation()));

        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();

        List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }


    public void getTripDeals() {
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

        Map<UUID, Location> allUsersLocations = tourGuideService.getAllUsersLocations();
        assertEquals(5, allUsersLocations.size());

        for (Map.Entry<UUID, Location> entry : allUsersLocations.entrySet()) {

            UUID id = entry.getKey();
            Location location = entry.getValue();
            assertNotNull(id);
            assertNotNull(location);
        }
    }


}
