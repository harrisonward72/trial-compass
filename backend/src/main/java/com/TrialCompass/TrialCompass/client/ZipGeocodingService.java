package com.TrialCompass.TrialCompass.client;

import com.TrialCompass.TrialCompass.exception.ExternalServiceException;
import com.TrialCompass.TrialCompass.exception.InvalidSearchException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//makes zip code coordinates - trials api filters by coordinates not zip
@Service
public class ZipGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(ZipGeocodingService.class);

    //json ignore so unknown properties get ignored
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ZipResponse(List<Place> places){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Place(String latitude, String longitude) {}

    private final RestClient restClient;

    //zip location doesnt change, a cache can hold that value
    private final Map<String, GeoPoint> cache = new ConcurrentHashMap<>();

    public ZipGeocodingService(@Value("${app.geocoding.base-url}") String baseUrl) {
        //use a timeout so if the outside service takes too long the code moves on
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));

        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public GeoPoint lookup(String zip) {
        GeoPoint cached = cache.get(zip);
        if(cached != null) {
            return cached;
        }

        try {
            ZipResponse response = restClient.get().uri("/us/{zip}", zip).retrieve().body(ZipResponse.class);

            if(response == null || response.places() == null || response.places().isEmpty()) {
                throw new InvalidSearchException("Enter a valid zipcode");
            }

            Place place = response.places().get(0);
            GeoPoint point = new GeoPoint(
                    Double.parseDouble(place.latitude()),
                    Double.parseDouble(place.longitude()));
            cache.put(zip, point);
            return point;
        } catch (HttpClientErrorException.NotFound e){
            throw new InvalidSearchException("We couldn't find that zipcode");
        } catch (RestClientException | NumberFormatException e) {
            log.error("Zip lookup failed for {}: {}", e.getMessage());
            throw new ExternalServiceException("Location is temporarily unavailable. Try searching without the zip code", e);
        }
    }
}
