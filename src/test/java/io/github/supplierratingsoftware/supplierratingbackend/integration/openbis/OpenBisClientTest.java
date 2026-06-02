package io.github.supplierratingsoftware.supplierratingbackend.integration.openbis;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.fetchoptions.PropertyFetchOptions;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.fetchoptions.SampleFetchOptions;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.fetchoptions.SampleTypeFetchOptions;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.SamplePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.update.SampleUpdate;
import io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisIntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.junit.jupiter.api.AfterEach;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Integration tests for the {@link OpenBisClient}.
 * <p>
 * This test uses {@link RestClientTest} to load a slice of the application context containing only the client infrastructure.
 * It mocks the HTTP transport layer using {@link MockRestServiceServer}, allowing us to verify the structure of
 * outgoing JSON-RPC requests (serialization) and the handling of incoming JSON-RPC responses (deserialization)
 * without requiring a running OpenBIS server.
 * </p>
 */
@RestClientTest(OpenBisClient.class)
@EnableConfigurationProperties(OpenBisProperties.class)
@TestPropertySource(properties = {
        "openbis.api-url=https://mock-openbis-server.local/openbis/api/v3",
        "openbis.user=dummy-user",
        "openbis.password=dummy-password",
        "openbis.default-space=DEFAULT",
        "openbis.supplier.project-code=SUPPLIER",
        "openbis.supplier.type-code=SUPPLIER_TYPE",
        "openbis.supplier.collection-code=SUPPLIER_COLL",
        "openbis.order.project-code=ORDER",
        "openbis.order.type-code=ORDER_TYPE",
        "openbis.order.collection-code=ORDER_COLL",
        "openbis.rating.project-code=RATING",
        "openbis.rating.type-code=RATING_TYPE",
        "openbis.rating.collection-code=RATING_COLL"
})
public class OpenBisClientTest {

    @Autowired
    private OpenBisClient openBisClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Constants ---
    private static final String DUMMY_API_URL = "https://mock-openbis-server.local/openbis/api/v3";
    private static final String DUMMY_SESSION_TOKEN = "test-session-token-123";
    private static final MediaType JSON_RPC_MEDIA_TYPE = MediaType.parseMediaType("application/json-rpc");

    // --- JSON Path Constants ---
    private static final String JSON_RPC_VERSION_PATH = "$.jsonrpc";
    private static final String JSON_RPC_VERSION = "2.0";
    private static final String JSON_METHOD_PATH = "$.method";
    private static final String JSON_ID_PATH = "$.id";
    private static final String JSON_PARAMS_0_PATH = "$.params[0]"; // Session Token
    private static final String JSON_PARAMS_1_PATH = "$.params[1]"; // Second JSON-RPC parameter (method-specific payload)
    private static final String JSON_PARAMS_1_OPERATOR_PATH = "$.params[1].operator"; // Criteria Operator (verifies object presence)
    private static final String JSON_PARAMS_2_PROPERTIES_TYPE_PATH = "$.params[2].properties['@type']";

    // --- JSON RPC Constants ---
    private static final int JSON_RPC_ID_1 = 1;
    private static final int JSON_RPC_ID_2 = 2;
    private static final int JSON_RPC_ID_3 = 3;
    private static final int JSON_RPC_ID_4 = 4;
    private static final int JSON_RPC_ERROR_CODE = -32000;

    // --- Search Logic & Data Constants ---
    private static final String SEARCH_OPERATOR_AND = "AND";
    private static final int SEARCH_TOTAL_COUNT = 1;
    private static final int EXPECTED_RESULT_SIZE = 1;

    // --- Sample Dummy Data ---
    private static final int SAMPLE_INTERNAL_ID = 1; // @id in JSON
    private static final String SAMPLE_PERM_ID = "20230101-100";
    private static final String SAMPLE_CODE = "TEST_SAMPLE_1";
    private static final String SAMPLE_PROPERTY_KEY = "NAME";
    private static final String SAMPLE_PROPERTY_VALUE = "Test Name";
    private static final String CREATED_PERM_ID = "20230101-101";

    // --- JSON RPC Methods ---
    private static final String SEARCH_METHOD = "searchSamples";
    private static final String CREATE_METHOD = "createSamples";
    private static final String UPDATE_METHOD = "updateSamples";

    // --- PAT Validation Constants ---
    private static final String GET_SESSION_INFO_METHOD = "getSessionInformation";
    private static final String DUMMY_PAT = "test-pat-abc-123";
    private static final String PAT_VALIDATION_FAILED_MESSAGE = "PAT validation failed";
    private static final String EXPECTED_PAT_OWNER_ID = "dummy-user";

    // --- JSON RPC Response Error Messages ---
    private static final String LOGIN_FAILED_MESSAGE = "Login failed";

    // --- Type Discriminators ---
    /**
     * Discriminator for PropertyFetchOptions. Verifying this proves that Jackson
     * is correctly configured to write '@type' for nested polymorphic objects.
     */
    private static final String TYPE_PROPERTY_FETCH_OPTIONS = "as.dto.property.fetchoptions.PropertyFetchOptions";

    // --- JSON Payloads (Requests & Responses) ---

    /**
     * Simulated JSON-RPC success response for a login request.
     * Contains the session token in the 'result' field.
     */
    private static final String LOGIN_SUCCESS_RESPONSE = """
            {
              "jsonrpc": "%s",
              "id": %d,
              "result": "%s"
            }
            """.formatted(JSON_RPC_VERSION, JSON_RPC_ID_1, DUMMY_SESSION_TOKEN);

    /**
     * Simulated JSON-RPC error response for a failed login request.
     * Contains an 'error' object with code and message.
     */
    private static final String LOGIN_ERROR_RESPONSE = """
            {
              "jsonrpc": "%s",
              "id": %d,
              "error": {
                "code": %d,
                "message": "%s",
                "data": null
              }
            }
            """.formatted(JSON_RPC_VERSION, JSON_RPC_ID_1, JSON_RPC_ERROR_CODE, LOGIN_FAILED_MESSAGE);

    /**
     * Simulated JSON-RPC success response for a PAT validation request.
     * Contains the owning user's id under result.person.userId.
     */
    private static final String VALIDATE_PAT_SUCCESS_RESPONSE = """
        {
          "jsonrpc": "%s",
          "id": %d,
          "result": {
            "person": {
              "userId": "%s"
            }
          }
        }
        """.formatted(JSON_RPC_VERSION, JSON_RPC_ID_1, EXPECTED_PAT_OWNER_ID);

    /**
     * Simulated JSON-RPC success response for a search request.
     * Contains a list of {@link OpenBisSample} objects.
     * <p>
     * This Includes '@id' fields to satisfy Jackson's IdentityInfo handling for cycle detection.
     * </p>
     */
    private static final String SEARCH_SUCCESS_RESPONSE = """
            {
              "jsonrpc": "%s",
              "id": %d,
              "result": {
                 "@type": "as.dto.openbis.result.OpenBisSearchResult",
                 "totalCount": %d,
                 "objects": [
                    {
                        "@id": %d,
                        "@type": "as.dto.sample.Sample",
                        "permId": {
                            "@type": "as.dto.sample.id.SamplePermId",
                            "permId": "%s"
                        },
                        "code": "%s",
                        "properties": {
                            "%s": "%s"
                        }
                    }
                 ]
              }
            }
            """.formatted(
            JSON_RPC_VERSION,
            JSON_RPC_ID_2,
            SEARCH_TOTAL_COUNT,
            SAMPLE_INTERNAL_ID,
            SAMPLE_PERM_ID,
            SAMPLE_CODE,
            SAMPLE_PROPERTY_KEY,
            SAMPLE_PROPERTY_VALUE
    );

    /**
     * Simulated JSON-RPC success response for a createSamples request.
     * Contains the list of permIds of the created samples.
     */
    private static final String CREATE_SUCCESS_RESPONSE = """
            {
              "jsonrpc": "%s",
              "id": %d,
              "result": [
                {
                  "@type": "as.dto.sample.id.SamplePermId",
                  "permId": "%s"
                }
              ]
            }
            """.formatted(JSON_RPC_VERSION, JSON_RPC_ID_3, CREATED_PERM_ID);

    /**
     * Simulated JSON-RPC success response for an updateSamples request.
     * The result is typically null (void) for updates.
     */
    private static final String UPDATE_SUCCESS_RESPONSE = """
            {
              "jsonrpc": "%s",
              "id": %d,
              "result": null
            }
            """.formatted(JSON_RPC_VERSION, JSON_RPC_ID_4);

    // --- Helper Methods to create DTOs ---

    /**
     * Creates a dummy {@link SampleCreation} object with minimal values.
     * @return A {@link SampleCreation} object
     */
    private SampleCreation createDummySampleCreation() {
        return new SampleCreation(
                null,
                null,
                null,
                null,
                SAMPLE_CODE,
                null,
                null
        );
    }

    /**
     * Creates a dummy {@link SampleUpdate} object with minimal values.
     * @return A {@link SampleUpdate} object
     */
    private SampleUpdate createDummySampleUpdate() {
        return new SampleUpdate(
                new SamplePermId(SAMPLE_PERM_ID),
                null
        );
    }

    // --- Setup ---

    /**
     * Resets the mock server before each test execution to ensure a clean state.
     */
    @BeforeEach
    void setUp() {
        server.reset();

        // Simulate an HTTP request carrying an Authorization header,
        // so getCurrentToken() finds the PAT and no longer throws.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + DUMMY_SESSION_TOKEN);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    // Reset the simulated request context after each test
    // so it does not leak into other tests.
    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * Verifies that {@link OpenBisClient#searchSamples(SampleSearchCriteria, SampleFetchOptions)}
     * performs the following actions:
     * <ol>
     *      <li>Implicitly performs a login request to get a session token.</li>
     *      <li>Sends a search request containing the session token, search criteria, and fetch options.</li>
     *      <li>Correctly serializes nested polymorphic objects (verifying '@type' fields).</li>
     *      <li>Deserializes the response into a list of {@link OpenBisSample} objects.</li>
     * </ol>
     */
    @Test
    void searchSamples_shouldReturnListOfSamples_whenCriteriaIsValid() {

        // Arrange
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        SampleFetchOptions fetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                null
        );

        // Expectation: Search Request
        server.expect(requestTo(DUMMY_API_URL))
                .andExpect(method(POST))
                .andExpect(jsonPath(JSON_METHOD_PATH, is(SEARCH_METHOD)))
                .andExpect(jsonPath(JSON_PARAMS_0_PATH, is(DUMMY_SESSION_TOKEN)))
                .andExpect(jsonPath(JSON_PARAMS_1_OPERATOR_PATH, is(SEARCH_OPERATOR_AND)))
                .andExpect(jsonPath(JSON_PARAMS_2_PROPERTIES_TYPE_PATH, is(TYPE_PROPERTY_FETCH_OPTIONS)))
                .andRespond(withSuccess(SEARCH_SUCCESS_RESPONSE, JSON_RPC_MEDIA_TYPE));

        // Act
        List<OpenBisSample> results = openBisClient.searchSamples(criteria, fetchOptions);

        // Assert
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(EXPECTED_RESULT_SIZE);
        assertThat(results.getFirst().code()).isEqualTo(SAMPLE_CODE);

        server.verify();
    }

    /**
     * Verifies that {@link OpenBisClient#createSamples(List)} correctly serializes the list of
     * {@link SampleCreation} objects and returns the list of created permIds.
     */
    @Test
    void createSamples_shouldReturnPermIds_whenCreationIsValid() {

        // Arrange
        SampleCreation creation = createDummySampleCreation();

        // Expectation: Create Request
        server.expect(requestTo(DUMMY_API_URL))
                .andExpect(method(POST))
                .andExpect(jsonPath(JSON_METHOD_PATH, is(CREATE_METHOD)))
                .andExpect(jsonPath(JSON_PARAMS_0_PATH, is(DUMMY_SESSION_TOKEN)))
                .andExpect(jsonPath(JSON_PARAMS_1_PATH, hasSize(EXPECTED_RESULT_SIZE)))
                .andRespond(withSuccess(CREATE_SUCCESS_RESPONSE, JSON_RPC_MEDIA_TYPE));

        // Act
        List<SamplePermId> results = openBisClient.createSamples(List.of(creation));

        // Assert
        assertThat(results).hasSize(EXPECTED_RESULT_SIZE);
        assertThat(results.getFirst().permId()).isEqualTo(CREATED_PERM_ID);
        server.verify();
    }

    /**
     * Verifies that {@link OpenBisClient#updateSamples(List)} correctly serializes the list of
     * {@link SampleUpdate} objects and handles the void response.
     */
    @Test
    void updateSamples_shouldSucceed_whenUpdateIsValid() {

        // Arrange
        SampleUpdate update = createDummySampleUpdate();


        // Expectation: Update Request
        server.expect(requestTo(DUMMY_API_URL))
                .andExpect(method(POST))
                .andExpect(jsonPath(JSON_METHOD_PATH, is(UPDATE_METHOD)))
                .andExpect(jsonPath(JSON_PARAMS_0_PATH, is(DUMMY_SESSION_TOKEN)))
                .andExpect(jsonPath(JSON_PARAMS_1_PATH, hasSize(EXPECTED_RESULT_SIZE)))
                .andRespond(withSuccess(UPDATE_SUCCESS_RESPONSE, JSON_RPC_MEDIA_TYPE));

        // Act
        openBisClient.updateSamples(List.of(update));

        // Assert
        server.verify();
    }

    /**
     * Verifies that {@link OpenBisClient#validatePat(String)} sends the PAT to openBIS
     * and returns the owning user id when the token is valid.
     */
    @Test
    void validatePat_shouldReturnUserId_whenPatIsValid() {

        // Arrange: the server expects a "getSessionInformation" request carrying the PAT
        server.expect(requestTo(DUMMY_API_URL))
                .andExpect(method(POST))
                .andExpect(jsonPath(JSON_METHOD_PATH, is(GET_SESSION_INFO_METHOD)))
                .andExpect(jsonPath(JSON_PARAMS_0_PATH, is(DUMMY_PAT)))
                .andRespond(withSuccess(VALIDATE_PAT_SUCCESS_RESPONSE, JSON_RPC_MEDIA_TYPE));

        // Act
        String userId = openBisClient.validatePat(DUMMY_PAT);

        // Assert
        assertThat(userId).isEqualTo(EXPECTED_PAT_OWNER_ID);
        server.verify();
    }

    /**
     * Verifies that {@link OpenBisClient#validatePat(String)} throws an
     * {@link OpenBisIntegrationException} when openBIS returns a JSON-RPC error.
     */
    @Test
    void validatePat_shouldThrowException_whenOpenBisReturnsError() {

        // Arrange: reuse the generic JSON-RPC error response
        server.expect(requestTo(DUMMY_API_URL))
                .andExpect(method(POST))
                .andRespond(withSuccess(LOGIN_ERROR_RESPONSE, JSON_RPC_MEDIA_TYPE));

        // Act & Assert
        assertThatThrownBy(() -> openBisClient.validatePat(DUMMY_PAT))
                .isInstanceOf(OpenBisIntegrationException.class)
                .hasMessageContaining(PAT_VALIDATION_FAILED_MESSAGE);
    }
}