package io.github.supplierratingsoftware.supplierratingbackend.integration.openbis;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisJsonConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.fetchoptions.SampleFetchOptions;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.generic.JsonRpcRequest;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.generic.JsonRpcResponse;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.SamplePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSearchResult;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.update.SampleUpdate;
import io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisIntegrationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Low-level client for communicating with the OpenBIS V3 JSON-RPC API.
 * <p>
 * This component handles the HTTP transport layer, session management (login),
 * and the serialization/deserialization of JSON-RPC messages. It is designed to be
 * unaware of specific business logic, focusing solely on the technical exchange of data.
 * </p>
 *
 * <p><strong>Configuration:</strong><br>
 * Uses {@link OpenBisProperties} for API URL and credentials.
 * Configures a specific {@link JacksonJsonHttpMessageConverter} to handle the "application/json-rpc" MIME type.
 * </p>
 */
@Service
public class OpenBisClient {
    private static final Logger log = LoggerFactory.getLogger(OpenBisClient.class);
    private final RestClient restClient;
    private final OpenBisProperties properties;
    // Same JsonMapper as the HTTP message converter, used to turn request parameters into JSON trees
    // (see createRequest). Reusing it guarantees identical serialization to the actual HTTP request.
    private final JsonMapper jsonMapper;

    /**
     * Constructs an instance of OpenBisClient with the specified RestClient builder and OpenBisProperties.
     *
     * @param properties an OpenBisProperties instance containing the necessary OpenBIS configuration properties
     *                   such as API URL, username, and password.
     * @param restClientBuilder a RestClient builder used to configure the RestClient used by the OpenBisClient.
     */
    public OpenBisClient(OpenBisProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;

        // OpenBIS sends "application/json-rpc" responses instead of "application/json"
        // This custom Jackson-Converter handles this case by adding "application/json-rpc" to the list of supported media types
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, MediaType.parseMediaType(OpenBisJsonConstants.SUPPORTED_MEDIA_TYPE)));
        this.jsonMapper = converter.getMapper();

        // Build the RestClient with the custom Jackson-Converter
        this.restClient = restClientBuilder
                .baseUrl(properties.apiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .configureMessageConverters(converters -> converters.addCustomConverter(converter))
                .build();
    }

    /**
     * Takes the current token from the request header and returns it.
     * @return The current token from the request header.
     * @throws OpenBisIntegrationException if no token is found in the request header.
     */
    private String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        throw new OpenBisIntegrationException("No valid PAT found in the Authorization header. Please log in.");
    }

    /**
     * Validates a Personal Access Token (PAT) against openBIS and returns the owning user id.
     * @param pat The Personal Access Token to validate.
     * @return The user id of the PAT owner.
     * @throws OpenBisIntegrationException if the PAT is invalid or the user data cannot be loaded.
     */
    public String validatePat(String pat) {
        JsonRpcRequest request = createRequest("getSessionInformation", List.of(pat));
        ParameterizedTypeReference<JsonRpcResponse<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};

        JsonRpcResponse<Map<String, Object>> response;
        try {
            response = restClient.post().body(request).retrieve().body(responseType);
        } catch (Exception e) {
            log.error("PAT validation request failed: {}", e.getMessage());
            throw new OpenBisIntegrationException("Invalid PAT: " + e.getMessage(), e);
        }
        validateResponse(response, "PAT validation failed");

        Map<String, Object> result = response.result();
        if (result == null || !result.containsKey("person")) {
            log.error("PAT validation returned no user data");
            throw new OpenBisIntegrationException("User data could not be loaded.");
        }
        Map<String, Object> person = (Map<String, Object>) result.get("person");
        return person.get("userId").toString();
    }

    /**
     * Searches in openBIS for samples based on the specified criteria and fetch options.
     *
     * @param criteria     The search criteria to use for the search.
     * @param fetchOptions The fetch options to use for the search.
     * @return A list of {@link OpenBisSample} objects matching the specified criteria.
     */
    public List<OpenBisSample> searchSamples(SampleSearchCriteria criteria, SampleFetchOptions fetchOptions) {
        // Get the session token from the client
        String sessionToken = getCurrentToken();

        // Create the search request payload
        // Order of parameters in V3 API: [sessionToken, criteria, fetchOptions]
        JsonRpcRequest request = createRequest(OpenBisJsonConstants.SEARCH_SAMPLES_METHOD_NAME, List.of(sessionToken, criteria, fetchOptions));

        // Define the return type of the search request
        // We expect a JsonRpcResponse<OpenBisSearchResult<OpenBisSample>>
        // where the OpenBisSearchResult contains a list of OpenBisSample objects
        ParameterizedTypeReference<JsonRpcResponse<OpenBisSearchResult<OpenBisSample>>> responseType = new ParameterizedTypeReference<>() {};

        // Send the search request
        JsonRpcResponse<OpenBisSearchResult<OpenBisSample>> response;
        try {
            // Send the search request and get the search result as a response (OpenBisSearchResult<OpenBisSample>)
            response = restClient.post()
                    .body(request)
                    .retrieve()
                    .body(responseType);
        } catch (Exception e) {
            log.error("Search request failed: {}", e.getMessage());
            throw new OpenBisIntegrationException("Search request failed: " + e.getMessage(), e);
        }

        // Validate response
        validateResponse(response, "Failed to search for samples");

        // Ensure response result is not null
        if (response.result() == null) {
            log.error("Search returned a `null` result");
            throw new OpenBisIntegrationException("Search returned a `null` result");
        }

        // Success, return the list of samples
        return response.result().objects();
    }

    /**
     * Creates new samples in openBIS.
     *
     * @param samples The list of samples to create.
     * @return A list of permIds of the created samples.
     * @throws RuntimeException if the creation fails.
     */
    public List<SamplePermId> createSamples(List<SampleCreation> samples) {

        // Safety check: Ensure we have samples to create
        if (samples == null || samples.isEmpty()) {
            return List.of();
        }

        // Safety check: Ensure we are logged in
        String sessionToken = getCurrentToken();

        // Prepare Request
        List<Object> params = List.of(sessionToken, samples);
        JsonRpcRequest request = createRequest(OpenBisJsonConstants.CREATE_SAMPLE_METHOD_NAME, params);

        // Define Response Type
        ParameterizedTypeReference<JsonRpcResponse<List<SamplePermId>>> responseType = new ParameterizedTypeReference<>() {};

        // Execute Request
        JsonRpcResponse<List<SamplePermId>> response;
        try {
            response = restClient.post()
                    .body(request)
                    .retrieve()
                    .body(responseType);
        } catch (Exception e) {
            log.error("Creation request failed: {}", e.getMessage());
            throw new OpenBisIntegrationException("Creation request failed: " + e.getMessage(), e);
        }

        // Validate response
        validateResponse(response, "Failed to create samples");

        // Ensure response result is not null
        if (response.result() == null) {
            log.error("Creation returned a `null` result");
            throw new OpenBisIntegrationException("Creation returned a `null` result");
        }

        // Success, return the list of sample perm IDs of the created sample
        return response.result();
    }

    /**
     * Updates existing samples in OpenBIS.
     *
     * @param updates The list of update objects.
     */
    public void updateSamples(List<SampleUpdate> updates) {
        if (updates == null || updates.isEmpty()) return;
        String sessionToken = getCurrentToken();

        // Create the update request payload
        List<Object> params = List.of(sessionToken, updates);
        JsonRpcRequest request = createRequest(OpenBisJsonConstants.UPDATE_SAMPLE_METHOD_NAME, params);

        // Define Response Type (OpenBIS returns "result": null for update requests)
        ParameterizedTypeReference<JsonRpcResponse<Void>> responseType = new ParameterizedTypeReference<>() {};

        // Execute Request
        JsonRpcResponse<Void> response;
        try {
            response = restClient.post()
                    .body(request)
                    .retrieve()
                    .body(responseType);
        } catch (Exception e) {
            log.error("Update request failed: {}", e.getMessage());
            throw new OpenBisIntegrationException("Update request failed: " + e.getMessage(), e);
        }

        // Validate response
        validateResponse(response, "Failed to update samples");
    }

    /**
     * Creates a JSON-RPC request in which every JSON object carrying an {@code @type} also gets an {@code @id}.
     * <p>
     * openBIS 7 requires Jackson object ids ({@code @id}) on all typed objects in the request parameters.
     * Without them, openBIS 7 rejects the request with
     * {@code -32602 "Failed to read method parameter at index N"}.
     * The official Python client pyBIS 7 does the same (see {@code assign_jackson_ids}).
     * openBIS 20.10 accepts the additional ids as well (tested locally with 7.0.0 and 20.10.13.1).
     * </p>
     * <p>
     * Each parameter is first converted into a JSON tree (with the same mapper as the HTTP request),
     * then the ids are added. Simple values like the session token stay unchanged.
     * </p>
     *
     * @param method The JSON-RPC method to invoke on openBIS.
     * @param params The method parameters (session token, criteria, fetch options, ...).
     * @return The request, ready to be sent.
     */
    private JsonRpcRequest createRequest(String method, List<Object> params) {
        List<Object> paramsWithIds = params.stream()
                .map(param -> {
                    JsonNode tree = jsonMapper.valueToTree(param);
                    // Numbering starts at 1 for each parameter (same as pyBIS 7)
                    assignJacksonIds(tree, new AtomicInteger(1));
                    return (Object) tree;
                })
                .toList();
        return new JsonRpcRequest(method, paramsWithIds);
    }

    /**
     * Recursively assigns a consecutive {@code @id} (1, 2, 3, ...) to every JSON object that has an {@code @type}.
     *
     * @param node    The current node in the JSON tree.
     * @param counter The next free id within the current parameter.
     */
    private void assignJacksonIds(JsonNode node, AtomicInteger counter) {
        // Only JSON objects { ... } with "@type" get an id
        if (node instanceof ObjectNode objectNode && objectNode.has(OpenBisJsonConstants.TYPE_JSON_PROPERTY_KEY)) {
            objectNode.put(OpenBisJsonConstants.ID_JSON_PROPERTY_KEY, counter.getAndIncrement());
        }
        // Recursion: objects iterate over their values, arrays over their elements
        for (JsonNode child : node) {
            assignJacksonIds(child, counter);
        }
    }

    /**
     * Validates that the response is not null and does not contain an OpenBIS error.
     *
     * @param response      The response to validate.
     * @param errorMessage  The error message to use if validation fails.
     * @throws OpenBisIntegrationException if the validation fails.
     */
    private void validateResponse(JsonRpcResponse<?> response, String errorMessage) {
        if (response == null) {
            log.error("{}: Received null response from OpenBIS", errorMessage);
            throw new OpenBisIntegrationException(errorMessage + ": Received null response from OpenBIS");
        }

        if (response.hasError()) {
            if (response.error() != null) {
                log.error("Response: {}", response.error());
            }
            String errorDetail = (response.error() != null) ? response.error().toString() : "Unknown Error";
            log.error(errorMessage);
            log.error("Error in OpenBIS Response. Details: {}", errorDetail);
            throw new OpenBisIntegrationException(errorMessage + ": " + errorDetail);
        }
    }
}
