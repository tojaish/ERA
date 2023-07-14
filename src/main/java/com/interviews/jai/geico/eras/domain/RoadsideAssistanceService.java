package com.interviews.jai.geico.eras.domain;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Metrics;
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
    public static final Distance radius_limit = new Distance(100000.0d, Metrics.KILOMETERS);

    // Cache to be swapped out with Redis or Permanent Storage.
    private final Map<String, String> reservations = new HashMap<>();

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
        Point point = new Point(assistantLocation.glat(), assistantLocation.glong());
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
        searchOptions.limit(limit > 0 ? limit : 5);
        searchOptions.sortAscending();

        GeoResults<GeoLocation<String>> matchedAssistants = geoOperations.search(ASSISTNATS_KEY,
                GeoReference.fromCoordinate(point),
                radius_limit, searchOptions);

        Supplier<TreeSet<Assistant>> assistantSorter = () -> new TreeSet<Assistant>(
                Comparator.comparingDouble(Assistant::distance));

        // Filter out Assistants who are reserved and Map results.
        SortedSet<Assistant> availableAssistants = Optional.ofNullable(matchedAssistants)
                .get().getContent().stream()
                .filter(s -> !reservations.containsValue(s.getContent().getName()))
                .map(s -> new Assistant(s.getContent().getName(),
                        new Geolocation(s.getContent().getPoint().getX(), s.getContent().getPoint().getY()),
                        s.getDistance().getValue()))
                .collect(Collectors.toCollection(assistantSorter));

        log.debug("Available Assistants :: " + availableAssistants);
        return availableAssistants;
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
        SortedSet<Assistant> availableAssitants = this.findNearestAssistants(customerLocation, 5);
        Optional<Assistant> selectedAssistant = Optional.ofNullable(availableAssitants.first());
        // Assuming, we do not allow for re-booking a different Assitant
        if(selectedAssistant.isPresent() && !reservations.containsKey(customer.name())){
            reservations.put(customer.name(), selectedAssistant.get().name());
            return selectedAssistant;
        }else{
            return Optional.empty();
        }
    }

    /**
     * This method releases an assistant either after they have completed work, or
     * the customer no longer needs help.
     *
     * @param customer  - Represents a Geico customer
     * @param assistant - An assistant that was previously reserved by the customer
     */
    public void releaseAssistant(Customer customer, Assistant assistant) {
        if(reservations.containsKey(customer.name())) {
            if(reservations.get(customer.name()).equalsIgnoreCase(assistant.name()))
                reservations.remove(customer.name());
        }
    }

}
