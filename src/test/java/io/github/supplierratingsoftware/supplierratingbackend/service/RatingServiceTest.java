package io.github.supplierratingsoftware.supplierratingbackend.service;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.*;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.PermIdSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisResourceNotFoundException;
import io.github.supplierratingsoftware.supplierratingbackend.integration.openbis.OpenBisClient;
import io.github.supplierratingsoftware.supplierratingbackend.mapper.RatingMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RatingServiceTest {

    @Mock
    private OpenBisClient openBisClient;

    @Mock
    private RatingMapper ratingMapper;

    private OpenBisProperties properties;

    private RatingService ratingService;

    @Captor
    private ArgumentCaptor<List<SampleCreation>> creationCaptor;

    @Captor
    private ArgumentCaptor<SampleSearchCriteria> criteriaCaptor;

    // Constants
    private static final String DEFAULT_SPACE_CODE = "LIEFERANTENBEWERTUNG";
    private static final String SUPPLIER_PROJECT_CODE = "LIEFERANTEN";
    private static final String SUPPLIER_TYPE_CODE = "LIEFERANT";
    private static final String SUPPLIER_COLLECTION_CODE = "LIEFERANTEN";
    private static final String ORDER_PROJECT_CODE = "BESTELLUNGEN";
    private static final String ORDER_TYPE_CODE = "BESTELLUNG";
    private static final String ORDER_COLLECTION_CODE = "BESTELLUNGEN";
    private static final String RATING_PROJECT_CODE = "BEWERTUNGEN";
    private static final String RATING_TYPE_CODE = "BESTELLBEWERTUNG";
    private static final String RATING_COLLECTION_CODE = "BEWERTUNGEN";

    private static final String FULL_PROJECT_CODE_RATING = "/" + DEFAULT_SPACE_CODE + "/" + RATING_PROJECT_CODE;
    private static final String FULL_COLLECTION_CODE_RATING = FULL_PROJECT_CODE_RATING + "/" + RATING_COLLECTION_CODE;

    private static final String DUMMY_API_URL = "dummyApiUrl";
    private static final String DUMMY_USER = "dummyUser";
    private static final String DUMMY_PASSWORD = "dummyPassword";

    private static final String DUMMY_UUID = UUID.randomUUID().toString();
    private static final String DUMMY_RATING_PERM_ID = RATING_TYPE_CODE + "-" + DUMMY_UUID;
    private static final String DUMMY_ORDER_PERM_ID = ORDER_TYPE_CODE + "-" + DUMMY_UUID;
    private static final String DUMMY_SUPPLIER_PERM_ID = SUPPLIER_TYPE_CODE + "-" + DUMMY_UUID;

    // Rating DTO Constants
    private static final Integer DUMMY_QUALITY = 5;
    private static final String DUMMY_QUALITY_REASON = "Excellent";
    private static final Integer DUMMY_COST = 4;
    private static final String DUMMY_COST_REASON = "Good";
    private static final Integer DUMMY_RELIABILITY = 3;
    private static final String DUMMY_RELIABILITY_REASON = "Average";
    private static final Integer DUMMY_AVAILABILITY = 5;
    private static final String DUMMY_AVAILABILITY_REASON = "Always available";
    private static final String DUMMY_COMMENT = "Great service";
    private static final Double DUMMY_TOTAL_SCORE = 4.25;

    // Supplier DTO Constants
    private static final String DUMMY_SUPPLIER_NAME = "Supplier Name";

    // Helper Methods

    /**
     * Helper method to create a supplier OpenBis sample.
     */
    private OpenBisSample createOpenBisSupplier(String id) {
        return new OpenBisSample(
                new OpenBisPermId(id),
                new OpenBisEntityType(SUPPLIER_TYPE_CODE),
                SUPPLIER_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    /**
     * Helper method to create an order OpenBis sample linked to a supplier.
     */
    private OpenBisSample createOpenBisOrder(String id, String parentSupplierId) {
        List<OpenBisSample> parents = (parentSupplierId == null)
                ? Collections.emptyList()
                : List.of(createOpenBisSupplier(parentSupplierId));

        return new OpenBisSample(
                new OpenBisPermId(id),
                new OpenBisEntityType(ORDER_TYPE_CODE),
                ORDER_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                parents,
                Collections.emptyList()
        );
    }

    /**
     * Helper method to create a rating OpenBis sample linked to an order.
     */
    private OpenBisSample createOpenBisRating(String id, String parentOrderId) {
        List<OpenBisSample> parents = (parentOrderId == null)
                ? Collections.emptyList()
                : List.of(createOpenBisOrder(parentOrderId, DUMMY_SUPPLIER_PERM_ID));

        return new OpenBisSample(
                new OpenBisPermId(id),
                new OpenBisEntityType(RATING_TYPE_CODE),
                RATING_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                parents,
                Collections.emptyList()
        );
    }

    /**
     * Helper method to create a valid RatingCreationDto.
     */
    private RatingCreationDto getRatingCreationDto() {
        return new RatingCreationDto(
                DUMMY_ORDER_PERM_ID,
                DUMMY_QUALITY,
                DUMMY_QUALITY_REASON,
                DUMMY_COST,
                DUMMY_COST_REASON,
                DUMMY_RELIABILITY,
                DUMMY_RELIABILITY_REASON,
                DUMMY_AVAILABILITY,
                DUMMY_AVAILABILITY_REASON,
                DUMMY_COMMENT
        );
    }

    /**
     * Dummy return object for mapper mocks (read).
     */
    private RatingDetailDto getMinimalRatingDetailDto() {
        return new RatingDetailDto(
                DUMMY_QUALITY,
                DUMMY_QUALITY_REASON,
                DUMMY_COST,
                DUMMY_COST_REASON,
                DUMMY_RELIABILITY,
                DUMMY_RELIABILITY_REASON,
                DUMMY_AVAILABILITY,
                DUMMY_AVAILABILITY_REASON,
                DUMMY_TOTAL_SCORE,
                DUMMY_COMMENT,
                DUMMY_RATING_PERM_ID,
                RATING_TYPE_CODE + "-" + DUMMY_UUID,
                DUMMY_ORDER_PERM_ID,
                DUMMY_SUPPLIER_PERM_ID,
                DUMMY_SUPPLIER_NAME
        );
    }

    /**
     * Helper method to create a valid Rating SampleCreation DTO.
     */
    private SampleCreation getRatingSampleCreationDto() {
        return new SampleCreation(
                new SpacePermId(DEFAULT_SPACE_CODE),
                new ProjectIdentifier(FULL_PROJECT_CODE_RATING),
                new ExperimentIdentifier(FULL_COLLECTION_CODE_RATING),
                new EntityTypePermId(RATING_TYPE_CODE),
                RATING_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                List.of(new SamplePermId(DUMMY_ORDER_PERM_ID))
        );
    }

    /**
     * Helper method to create a list of sample permission IDs (returned by createSamples).
     */
    private List<SamplePermId> getRatingSamplePermIds(String permId) {
        return List.of(new SamplePermId(permId));
    }

    /**
     * Setup method for initializing the test environment.
     */
    @BeforeEach
    void setUp() {
        OpenBisProperties.EntityConfig supplierConfig = new OpenBisProperties.EntityConfig(
                SUPPLIER_PROJECT_CODE,
                SUPPLIER_TYPE_CODE,
                SUPPLIER_COLLECTION_CODE
        );

        OpenBisProperties.EntityConfig orderConfig = new OpenBisProperties.EntityConfig(
                ORDER_PROJECT_CODE,
                ORDER_TYPE_CODE,
                ORDER_COLLECTION_CODE
        );

        OpenBisProperties.EntityConfig ratingConfig = new OpenBisProperties.EntityConfig(
                RATING_PROJECT_CODE,
                RATING_TYPE_CODE,
                RATING_COLLECTION_CODE
        );

        properties = new OpenBisProperties(
                DUMMY_API_URL,
                DEFAULT_SPACE_CODE,
                supplierConfig,
                orderConfig,
                ratingConfig
        );

        ratingService = new RatingService(openBisClient, properties, ratingMapper);
    }

    // Testing

    /**
     * Test method to verify that getRatingById successfully retrieves and returns a rating when it exists.
     * <p>
     * It ensures that:
     * <ul>
     *      <li>The OpenBIS client is called to search for the sample by PermID.</li>
     *      <li>The result is wrapped in an Optional.</li>
     * </ul>
     * </p>
     */
    @Test
    void getRatingById_shouldReturnRating_whenFound() {

        // Arrange
        OpenBisSample ratingSample = createOpenBisRating(DUMMY_RATING_PERM_ID, DUMMY_ORDER_PERM_ID);
        RatingDetailDto expectedDto = getMinimalRatingDetailDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(ratingSample));
        when(ratingMapper.toApiDto(any())).thenReturn(expectedDto);
        SampleSearchCriteria expectedCriteria = SampleSearchCriteria.create()
                .with(PermIdSearchCriteria.withId(DUMMY_RATING_PERM_ID))
                .with(io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleTypeSearchCriteria.withCode(RATING_TYPE_CODE));

        // Act
        Optional<RatingDetailDto> result = ratingService.getRatingById(DUMMY_RATING_PERM_ID);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDto);

        // - Verify Search Criteria
        verify(openBisClient).searchSamples(criteriaCaptor.capture(), any());
        SampleSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria)
                .usingRecursiveComparison()
                .isEqualTo(expectedCriteria);
    }

    /**
     * Test method to verify that getRatingById returns an empty Optional when the rating is not found.
     */
    @Test
    void getRatingById_shouldReturnEmpty_whenNotFound() {

        // Arrange
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act
        Optional<RatingDetailDto> result = ratingService.getRatingById(DUMMY_RATING_PERM_ID);

        // Assert
        assertThat(result).isEmpty();
        verify(openBisClient).searchSamples(any(), any());
    }

    /**
     * Test method to verify the complete creation flow of a rating.
     * <p>
     * This test ensures the complex validation sequence:
     * <ol>
     *      <li><b>Check Order:</b> Ensure the linked order exists.</li>
     *      <li><b>Check Duplicate:</b> Ensure no rating already exists for this order.</li>
     *      <li><b>Create:</b> Execute creation in OpenBIS.</li>
     *      <li><b>Refetch:</b> Fetch the newly created rating.</li>
     * </ol>
     * </p>
     */
    @Test
    void createRating_shouldCreateAndReturnRating_whenOrderExistsAndNotRated() {

        // Arrange
        RatingCreationDto creationDto = getRatingCreationDto();
        SampleCreation sampleCreation = getRatingSampleCreationDto();
        OpenBisSample orderSample = createOpenBisOrder(DUMMY_ORDER_PERM_ID, DUMMY_SUPPLIER_PERM_ID);
        OpenBisSample createdRatingSample = createOpenBisRating(DUMMY_RATING_PERM_ID, DUMMY_ORDER_PERM_ID);
        RatingDetailDto expectedDto = getMinimalRatingDetailDto();

        // Sequential Mocks for searchSamples:
        // 1. validateOrderExists -> Returns Order List (Order exists)
        // 2. validateNoExistingRating -> Returns Empty List (No duplicate exists)
        // 3. fetchFreshRating -> Returns Created Rating List (After creation)
        when(openBisClient.searchSamples(any(), any()))
                .thenReturn(List.of(orderSample))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(createdRatingSample));
        when(ratingMapper.toOpenBisCreation(any())).thenReturn(sampleCreation);
        when(openBisClient.createSamples(any())).thenReturn(getRatingSamplePermIds(DUMMY_RATING_PERM_ID));
        when(ratingMapper.toApiDto(any())).thenReturn(expectedDto);

        // Act
        RatingDetailDto result = ratingService.createRating(creationDto);

        // Assert
        assertThat(result).isEqualTo(expectedDto);

        // - Verify Create
        verify(openBisClient).createSamples(creationCaptor.capture());
        assertThat(creationCaptor.getValue().getFirst()).isEqualTo(sampleCreation);

        // - Verify that searches happened 3 times (Order Check, Duplicate Check, Refetch)
        verify(openBisClient, Mockito.times(3)).searchSamples(any(), any());
    }

    /**
     * Test method to verify that createRating throws an exception when the referenced order does not exist.
     */
    @Test
    void createRating_shouldThrowException_whenOrderNotFound() {

        // Arrange
        RatingCreationDto creationDto = getRatingCreationDto();

        // Mock 1st search (Order Existence) to return empty
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert
        Assertions.assertThrows(
                OpenBisResourceNotFoundException.class,
                () -> ratingService.createRating(creationDto)
        );

        // - Verify Create never called
        verify(openBisClient, Mockito.never()).createSamples(any());
    }

    /**
     * Test method to verify that createRating throws an exception when the order is already rated.
     * <p>
     * Ensures strict 1:1 relationship between Order and Rating.
     * </p>
     */
    @Test
    void createRating_shouldThrowException_whenOrderAlreadyRated() {

        // Arrange
        RatingCreationDto creationDto = getRatingCreationDto();
        OpenBisSample orderSample = createOpenBisOrder(DUMMY_ORDER_PERM_ID, DUMMY_SUPPLIER_PERM_ID);
        OpenBisSample existingRating = createOpenBisRating(DUMMY_RATING_PERM_ID, DUMMY_ORDER_PERM_ID);
        when(openBisClient.searchSamples(any(), any()))
                .thenReturn(List.of(orderSample))
                .thenReturn(List.of(existingRating));

        // Act & Assert
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ratingService.createRating(creationDto)
        );

        // - Verify Create never called
        verify(openBisClient, Mockito.never()).createSamples(any());
    }
}