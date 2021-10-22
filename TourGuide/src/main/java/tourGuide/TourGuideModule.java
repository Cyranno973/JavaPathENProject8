package tourGuide;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tourGuide.proxy.gpsUtil.GpsUtilProxy;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
@EnableFeignClients
@Configuration
public class TourGuideModule {

    @Bean
    public RewardsService getRewardsService(GpsUtilProxy getGpsUtilProxy) {
        return new RewardsService(getGpsUtilProxy, getRewardCentralService());
    }

    @Bean
    public RewardCentralService getRewardCentralService() {
        return new RewardCentralService();
    }

}
