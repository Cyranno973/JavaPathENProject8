package tourGuide;
import static org.junit.Assert.*;

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
import tourGuide.proxy.gpsUtil.dto.VisitedLocation;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

public class TestRewardsService {

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
    public void userGetRewards() {

        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());


        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        Mockito.when(gpsUtilService.getUserLocation(user)).thenReturn(CompletableFuture.completedFuture(user.getLastVisitedLocation()));
        tourGuideService.trackUserLocation(user).join();
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        assertTrue(userRewards.size() == 1);
    }

    @Test
    public void isWithinAttractionProximity() {
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions() {
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentralService());
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0)).join();
        List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
        tourGuideService.tracker.stopTracking();

        assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());
    }

}
