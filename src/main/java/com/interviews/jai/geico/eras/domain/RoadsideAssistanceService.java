package com.interviews.jai.geico.eras.domain;

import java.util.Optional;
import java.util.SortedSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoadsideAssistanceService {

    // The Key in Redis which will be used to updated geo locations.
    public static final String ASSISTNATS_KEY = "providers_loc_idx";
    
    // Assuming here to a fixed value but this could be a paramater in the api call.
    public static final Distance radius_limit = new Distance(100.0d);

    @Autowired
    private final GeoOperations<String, String> geoOperations;

    public RoadsideAssistanceService(GeoOperations<String, String> geoOperations) {
        this.geoOperations = geoOperations;
    }

    /**
     * This method is used to update the location of the roadside assistance service
     * provider.
     *
     * @param assistant         represents the roadside assistance service provider
     * @param assistantLocation represents the location of the roadside assistant
     */
    public void updateAssistantLocation(Assistant assistant, Geolocation assistantLocation) {
        Point point = new Point(assistantLocation.glong(), assistantLocation.glat());
        geoOperations.add(ASSISTNATS_KEY, point, assistant.name());
    }

    /**
     * This method returns a collection of roadside assistants ordered by their
     * distance from the input geo location.
     *
     * @param geolocation - geolocation from which to search for assistants
     * @param limit       - the number of assistants to return
     * @return a sorted collection of assistants ordered ascending by distance from
     *         geoLocation
     */
    public SortedSet<Assistant> findNearestAssistants(Geolocation geolocation, int limit) {
        Point point = new Point(geolocation.glat(), geolocation.glong());
        GeoSearchCommandArgs searchOptions = GeoSearchCommandArgs.newGeoSearchArgs();
        searchOptions.includeCoordinates();
        searchOptions.includeDistance();
        if(limit > 0) {
            searchOptions.limit(limit);
        }
        searchOptions.sortAscending();

        GeoResults<GeoLocation<String>> results = geoOperations.search(ASSISTNATS_KEY, 
                                                                        GeoReference.fromCoordinate(point), 
                                                                        radius_limit, searchOptions);
        log.info(Optional.ofNullable(results).toString());
        return null;
    }

    /**
     * This method reserves an assistant for a Geico customer that is stranded on
     * the roadside due to a disabled vehicle.
     *
     * @param customer         - Represents a Geico customer
     * @param customerLocation - Location of the customer
     * @return The Assistant that is on their way to help
     */
    public Optional<Assistant> reserveAssistant(Customer customer, Geolocation customerLocation) {
        return Optional.empty();
    }

    /**
     * This method releases an assistant either after they have completed work, or
     * the customer no longer needs help.
     *
     * @param customer  - Represents a Geico customer
     * @param assistant - An assistant that was previously reserved by the customer
     */
    public void releaseAssistant(Customer customer, Assistant assistant) {

    }

}
