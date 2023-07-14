package com.interviews.jai.geico.eras.controllers;

import java.util.Collections;
import java.util.SortedSet;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interviews.jai.geico.eras.domain.Assistant;
import com.interviews.jai.geico.eras.domain.Customer;
import com.interviews.jai.geico.eras.domain.Geolocation;
import com.interviews.jai.geico.eras.domain.ReleaseProvider;
import com.interviews.jai.geico.eras.domain.RoadsideAssistanceService;
import com.interviews.jai.geico.eras.domain.SearchCriteria;
import com.interviews.jai.geico.eras.domain.UserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/eras/providers")
@RequiredArgsConstructor
@Slf4j
public class ERAServiceController {

	private final RoadsideAssistanceService service;

	@GetMapping("/search")
	public ResponseEntity<SortedSet<Assistant>> getNearbyEmergencyServiceProviders(
			@RequestBody SearchCriteria criterion) {
		try {
			log.info(criterion.toString());
			return new ResponseEntity<SortedSet<Assistant>>(
					service.findNearestAssistants(new Geolocation(criterion.glat(), criterion.glong()),
							criterion.radius()),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception occured in Updating the Assistant.", e);
			return new ResponseEntity<SortedSet<Assistant>>(Collections.emptySortedSet(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/update")
	public ResponseEntity<String> updateEmergencyProvidersLocation(@RequestBody UserDetails userInfo) {
		try {
			Geolocation geoLocation = new Geolocation(userInfo.glat(), userInfo.glong());
			service.updateAssistantLocation(new Assistant(userInfo.name(), geoLocation, null), geoLocation);
			return new ResponseEntity<>("Successfully updated Providers location.", HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception occured in Updating the Assistant.", e);
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/reserve")
	public ResponseEntity<String> reserveProvider(@RequestBody UserDetails userInfo) {
		try {
			Geolocation geoLocation = new Geolocation(userInfo.glat(), userInfo.glong());
			return new ResponseEntity<>(String.format("Emergency Assistance Provdier %s, will be assisting you.",
					service.reserveAssistant(new Customer(userInfo.name(), geoLocation), geoLocation)
							.map(s -> s.name()).get()),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception occured in reserving the Assistant.", e);
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/release")
	public ResponseEntity<String> releaseProvider(@RequestBody ReleaseProvider releaseProvider) {
		try {
			service.releaseAssistant(new Customer(releaseProvider.customerName(), null),
					new Assistant(releaseProvider.assistantName(), null, null));
			return new ResponseEntity<String>(String.format("Emergency Assistant :: %s, is successfully released.",
					releaseProvider.assistantName()), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception occured in releasing the Assistant", e);
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
