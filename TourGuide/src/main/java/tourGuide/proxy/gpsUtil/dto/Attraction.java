package tourGuide.proxy.gpsUtil.dto;

import java.util.UUID;

public class Attraction extends Location {

    public final String attractionName;
    public final String city;
    public final String state;
    public final UUID attractionId;


    public Attraction(String attractionName, String city, String state, UUID attractionId, double longitude, double latitude) {
        super(longitude, latitude);
        this.attractionName = attractionName;
        this.city = city;
        this.state = state;
        this.attractionId = attractionId;
    }

    public Attraction(String attractionName, String city, String state, double longitude, double latitude){
        this(attractionName,city,state,UUID.randomUUID(),longitude,latitude);
    }

}
