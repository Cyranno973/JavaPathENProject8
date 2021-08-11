package tourGuide.service;

import gpsUtil.location.Attraction;
import rewardCentral.RewardCentral;
import tourGuide.user.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RewardCentralService{
    private final RewardCentral rewardCentral = new RewardCentral();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10000);

    public CompletableFuture<Integer> getAttractionRewardPoints(Attraction attraction, User user) {
        return CompletableFuture.supplyAsync(() -> rewardCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId()), executorService);
    }


}
