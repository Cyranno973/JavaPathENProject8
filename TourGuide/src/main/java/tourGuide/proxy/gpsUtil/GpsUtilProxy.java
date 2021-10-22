package tourGuide.proxy.gpsUtil;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.proxy.gpsUtil.dto.Attraction;
import tourGuide.proxy.gpsUtil.dto.VisitedLocation;
import tourGuide.user.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@FeignClient(name = "GpsUtilProxy", url = "${client.gps.base.url}")
public interface GpsUtilProxy {

    @RequestMapping("/getAttractions")
    List<Attraction> getAttractions();

    @RequestMapping("/getUserLocation")
    CompletableFuture<VisitedLocation> getUserLocation(@RequestParam UUID userId);

    default CompletableFuture<VisitedLocation> getUserLocation(User user) {
        return getUserLocation(user.getUserId());
    }
}
