package tourGuide.gpsUtils.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class GpsUtilController {
    private final GpsUtil gpsUtil;
    private final List<Attraction> attractionList;

    public GpsUtilController() {
        gpsUtil = new GpsUtil();
        attractionList = gpsUtil.getAttractions();
    }

    @RequestMapping("/getAttractions")
    public List<Attraction> getAttractions() {
        return attractionList;
    }

    @RequestMapping("/getUserLocation")
    public VisitedLocation getUserLocation(@RequestParam String userId) {
        return gpsUtil.getUserLocation(UUID.fromString(userId));
    }

}
