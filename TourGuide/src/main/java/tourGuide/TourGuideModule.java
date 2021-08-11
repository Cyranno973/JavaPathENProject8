package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;

@Configuration
public class TourGuideModule {

    @Bean
    public GpsUtilService getGpsUtilService() {
        return new GpsUtilService();
    }

    @Bean
    public RewardsService getRewardsService() {
        return new RewardsService(getGpsUtilService(), getRewardCentralService());
    }

    @Bean
    public RewardCentralService getRewardCentralService() {
        return new RewardCentralService();
    }

}
