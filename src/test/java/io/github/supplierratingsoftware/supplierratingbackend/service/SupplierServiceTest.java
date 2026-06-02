package io.github.supplierratingsoftware.supplierratingbackend.service;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingStatsDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierUpdateDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.EntityTypePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.ExperimentIdentifier;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.OpenBisEntityType;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.OpenBisPermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.ProjectIdentifier;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.SamplePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.SpacePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.PermIdSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.update.SampleUpdate;
import io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisResourceNotFoundException;
import io.github.supplierratingsoftware.supplierratingbackend.integration.openbis.OpenBisClient;
import io.github.supplierratingsoftware.supplierratingbackend.mapper.OrderMapper;
import io.github.supplierratingsoftware.supplierratingbackend.mapper.SupplierMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private OpenBisClient openBisClient;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private OrderMapper orderMapper;

    private SupplierService supplierService;

    @Captor
    private ArgumentCaptor<List<SampleCreation>> creationCaptor;

    @Captor
    private ArgumentCaptor<RatingStatsDto> statsCaptor;

    @Captor
    private ArgumentCaptor<SampleSearchCriteria> criteriaCaptor;


    // OpenBis property Constants
    private static final String DEFAULT_SPACE_CODE = "LIEFERANTENBEWERTUNG";
    private static final String SUPPLIER_PROJECT_CODE = "LIEFERANTEN";
    private static final String SUPPLIER_TYPE_CODE = "LIEFERANT";
    private static final String SUPPLIER_COLLECTION_CODE = "LIEFERANTEN";
    private static final String FULL_PROJECT_CODE = "/" + DEFAULT_SPACE_CODE + "/" + SUPPLIER_PROJECT_CODE;
    private static final String FULL_COLLECTION_CODE = FULL_PROJECT_CODE + "/" + SUPPLIER_COLLECTION_CODE;
    private static final String DUMMY_API_URL = "dummyApiUrl";
    private static final String DUMMY_USER = "dummyUser";
    private static final String DUMMY_PASSWORD = "dummyPassword";
    private static final String ORDER_PROJECT_CODE = "BESTELLUNGEN";
    private static final String ORDER_TYPE_CODE = "BESTELLUNG";
    private static final String ORDER_COLLECTION_CODE = "BESTELLUNGEN";
    private static final String RATING_TYPE_CODE = "BESTELLBEWERTUNG";
    private static final String RATING_PROJECT_CODE = "BEWERTUNGEN";
    private static final String RATING_COLLECTION_CODE = "BEWERTUNGEN";

    // OpenBis DTO (Supplier) Constants
    private static final String DUMMY_SUPPLIER_NAME = "Test Supplier Name";
    private static final String DUMMY_CUSTOMER_NUMBER = "Test Customer Number";
    private static final String DUMMY_ADDITION = "Test Addition";
    private static final String DUMMY_STREET = "Test Street";
    private static final String DUMMY_PO_BOX = "Test Post Office Box";
    private static final String DUMMY_COUNTRY_LABEL = "CH";
    private static final String DUMMY_ZIP_CODE = "Test Zip Code";
    private static final String DUMMY_CITY = "Test City";
    private static final String DUMMY_WEBSITE = "Test Website";
    private static final String DUMMY_EMAIL = "Test Email";
    private static final String DUMMY_PHONE_NUMBER = "Test Phone Number";
    private static final String DUMMY_VAT_ID = "Test VAT ID";
    private static final String DUMMY_CONDITIONS = "Test Conditions";
    private static final String DUMMY_CUSTOMER_INFO = "Test Customer Info";

    // OpenBis DTO (Rating) Constants
    private static final Double DUMMY_QUALITY_RATING__A = 5.0;
    private static final Double DUMMY_QUALITY_RATING__B = 4.0;
    private static final Double DUMMY_QUALITY_RATING__Av_A_B = (DUMMY_QUALITY_RATING__A + DUMMY_QUALITY_RATING__B) / 2.0;
    private static final Double DUMMY_QUALITY_RATING__C = 4.0;
    private static final Double DUMMY_QUALITY_RATING__Av_A_C = (DUMMY_QUALITY_RATING__A + DUMMY_QUALITY_RATING__C) / 2.0;
    private static final Double DUMMY_COST_RATING__A = 4.0;
    private static final Double DUMMY_COST_RATING__B = 3.0;
    private static final Double DUMMY_COST_RATING__Av_A_B = (DUMMY_COST_RATING__A + DUMMY_COST_RATING__B) / 2.0;
    private static final Double DUMMY_COST_RATING__C = 3.0;
    private static final Double DUMMY_COST_RATING__Av_A_C = (DUMMY_COST_RATING__A + DUMMY_COST_RATING__C) / 2.0;
    private static final Double DUMMY_RELIABILITY_RATING__A = 3.0;
    private static final Double DUMMY_RELIABILITY_RATING__B = 4.0;
    private static final Double DUMMY_RELIABILITY_RATING__Av_A_B = (DUMMY_RELIABILITY_RATING__A + DUMMY_RELIABILITY_RATING__B) / 2.0;
    private static final Double DUMMY_RELIABILITY_RATING__C = 5.0;
    private static final Double DUMMY_RELIABILITY_RATING__Av_A_C = (DUMMY_RELIABILITY_RATING__A + DUMMY_RELIABILITY_RATING__C) / 2.0;
    private static final Double DUMMY_AVAILABILITY_RATING__A = 2.0;
    private static final Double DUMMY_AVAILABILITY_RATING__B = 1.0;
    private static final Double DUMMY_AVAILABILITY_RATING__Av_A_B = (DUMMY_AVAILABILITY_RATING__A + DUMMY_AVAILABILITY_RATING__B) / 2.0;
    private static final Double DUMMY_AVAILABILITY_RATING__Av_A_C = DUMMY_AVAILABILITY_RATING__A;
    private static final Double DUMMY_TOTAL_RATING__A = (DUMMY_QUALITY_RATING__A + DUMMY_COST_RATING__A + DUMMY_RELIABILITY_RATING__A + DUMMY_AVAILABILITY_RATING__A) / 4.0;
    private static final Double DUMMY_TOTAL_RATING__B = (DUMMY_QUALITY_RATING__B + DUMMY_COST_RATING__B + DUMMY_RELIABILITY_RATING__B + DUMMY_AVAILABILITY_RATING__B) / 4.0;
    private static final Double DUMMY_TOTAL_RATING__C = (DUMMY_QUALITY_RATING__C + DUMMY_COST_RATING__C + DUMMY_RELIABILITY_RATING__C) / 3.0;
    private static final Double DUMMY_TOTAL_RATING__Av_A_B = (DUMMY_TOTAL_RATING__A + DUMMY_TOTAL_RATING__B) / 2.0;
    private static final Double DUMMY_TOTAL_RATING__Av_A_C = (DUMMY_TOTAL_RATING__A + DUMMY_TOTAL_RATING__C) / 2.0;
    private static final int DUMMY_RATING_COUNT__A_B = 2;
    private static final int DUMMY_RATING_COUNT__A_C = 2;

    private static final String DUMMY_UUID = UUID.randomUUID().toString();

    private static final String DUMMY_SUPPLIER_PERM_ID = UUID.randomUUID().toString();
    private static final String DUMMY_ORDER_PERM_ID__A = UUID.randomUUID().toString();
    private static final String DUMMY_ORDER_PERM_ID__B = UUID.randomUUID().toString();
    private static final String DUMMY_ORDER_PERM_ID__C = UUID.randomUUID().toString();
    private static final String DUMMY_RATING_PERM_ID__A = UUID.randomUUID().toString();
    private static final String DUMMY_RATING_PERM_ID__B = UUID.randomUUID().toString();
    private static final String DUMMY_RATING_PERM_ID__C = UUID.randomUUID().toString();

    /**
     * Helper method to create a rating sample with specified properties.
     * This includes setting properties for quality, cost, reliability, availability, and total ratings.
     *
     * @param id           The permId of the rating sample.
     * @param quality      The quality rating of the sample.
     * @param cost         The cost rating of the sample.
     * @param reliability  The reliability rating of the sample.
     * @param availability The availability rating of the sample.
     * @param total        The total rating of the sample.
     * @return The created OpenBisSample object.
     */
    private OpenBisSample createRatingSample(String id, Double quality, Double cost, Double reliability, Double availability, Double total) {
        Map<String, String> props = new HashMap<>();

        if (quality != null) props.put(OpenBisSchemaConstants.QUALITY_RATING_PROPERTY, String.valueOf(quality));
        if (cost != null) props.put(OpenBisSchemaConstants.COST_RATING_PROPERTY, String.valueOf(cost));
        if (reliability != null)
            props.put(OpenBisSchemaConstants.RELIABILITY_RATING_PROPERTY, String.valueOf(reliability));
        if (availability != null)
            props.put(OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY, String.valueOf(availability));
        if (total != null) props.put(OpenBisSchemaConstants.TOTAL_SCORE_RATING_PROPERTY, String.valueOf(total));

        return new OpenBisSample(
                new OpenBisPermId(id),
                new OpenBisEntityType(RATING_TYPE_CODE),
                RATING_TYPE_CODE + "-" + DUMMY_UUID,
                props,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    /**
     * Helper method to create a dummy rating sample "A" with predefined properties.
     * This includes the predefined dummy rating score constants with the suffix "__A".
     *
     * @return The created OpenBisSample object representing rating sample "A".
     */
    private OpenBisSample getDummyRatingSampleA() {
        return createRatingSample(
                DUMMY_RATING_PERM_ID__A,
                DUMMY_QUALITY_RATING__A,
                DUMMY_COST_RATING__A,
                DUMMY_RELIABILITY_RATING__A,
                DUMMY_AVAILABILITY_RATING__A,
                DUMMY_TOTAL_RATING__A
        );
    }

    /**
     * Helper method to create a dummy rating sample "B" with predefined properties.
     * This includes the predefined dummy rating score constants with the suffix "__B".
     *
     * @return The created OpenBisSample object representing rating sample "B".
     */
    private OpenBisSample getDummyRatingSampleB() {
        return createRatingSample(
                DUMMY_RATING_PERM_ID__B,
                DUMMY_QUALITY_RATING__B,
                DUMMY_COST_RATING__B,
                DUMMY_RELIABILITY_RATING__B,
                DUMMY_AVAILABILITY_RATING__B,
                DUMMY_TOTAL_RATING__B
        );
    }

    /**
     * Helper method to create a dummy rating sample "C" with predefined properties.
     * This includes the predefined dummy rating score constants with the suffix "__C".
     *
     * @return The created OpenBisSample object representing rating sample "C".
     */
    private OpenBisSample getDummyRatingSampleC() {
        return createRatingSample(
                DUMMY_RATING_PERM_ID__C,
                DUMMY_QUALITY_RATING__C,
                DUMMY_COST_RATING__C,
                DUMMY_RELIABILITY_RATING__C,
                null,
                DUMMY_TOTAL_RATING__C
        );
    }

    /**
     * Helper method to create an OpenBisSample representing an order with the specified permId and list of ratings.
     *
     * @param id      The permId of the order.
     * @param ratings The list of rating samples associated with the order.
     * @return The created OpenBisSample object representing the order.
     */
    private OpenBisSample createOrder(String id, List<OpenBisSample> ratings) {
        return new OpenBisSample(
                new OpenBisPermId(id),
                new OpenBisEntityType(ORDER_TYPE_CODE),
                ORDER_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                Collections.emptyList(),
                ratings
        );
    }

    /**
     * Helper method to create an OpenBisSample representing a supplier with the specified permId and list of orders.
     *
     * @param id     The permId of the supplier.
     * @param orders The list of order samples associated with the supplier.
     * @return The created OpenBisSample object representing the supplier.
     */
    private OpenBisSample createSupplier(String id, List<OpenBisSample> orders) {
        return new OpenBisSample(
                new OpenBisPermId(id),
                new OpenBisEntityType(SUPPLIER_TYPE_CODE),
                SUPPLIER_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                Collections.emptyList(),
                orders
        );
    }


    /**
     * Helper method to create a minimal SupplierDetailDto for testing purposes.
     *
     * @return The created SupplierDetailDto object.
     */
    private SupplierDetailDto getMinimalSupplierDto() {
        return new SupplierDetailDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }


    /**
     * Helper method to create a SampleCreation OpenBIS Dto for a Supplier for testing purposes.
     *
     * @return The created SampleCreation OpenBIS Dto object.
     */
    private SampleCreation getSupplierSampleCreationDto() {
        return new SampleCreation(
                new SpacePermId(DEFAULT_SPACE_CODE),
                new ProjectIdentifier(FULL_PROJECT_CODE),
                new ExperimentIdentifier(FULL_COLLECTION_CODE),
                new EntityTypePermId(SUPPLIER_TYPE_CODE),
                SUPPLIER_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                Collections.emptyList()
        );
    }

    /**
     * Helper method to create a SupplierCreationDto for testing purposes.
     *
     * @return The created SupplierCreationDto object.
     */
    private SupplierCreationDto getSupplierCreationDto() {
        return new SupplierCreationDto(
                DUMMY_SUPPLIER_NAME,
                DUMMY_CUSTOMER_NUMBER,
                DUMMY_ADDITION,
                DUMMY_STREET,
                DUMMY_PO_BOX,
                DUMMY_COUNTRY_LABEL,
                DUMMY_ZIP_CODE,
                DUMMY_CITY,
                DUMMY_WEBSITE,
                DUMMY_EMAIL,
                DUMMY_PHONE_NUMBER,
                DUMMY_VAT_ID,
                DUMMY_CONDITIONS,
                DUMMY_CUSTOMER_INFO
        );
    }

    /**
     * Helper method to create a SupplierUpdateDto for testing purposes.
     *
     * @return The created SupplierUpdateDto object.
     */
    private SupplierUpdateDto getSupplierUpdateDto() {
        return new SupplierUpdateDto(
                DUMMY_SUPPLIER_NAME,
                DUMMY_CUSTOMER_NUMBER,
                null,
                null,
                null,
                DUMMY_COUNTRY_LABEL,
                DUMMY_ZIP_CODE,
                DUMMY_CITY,
                null,
                null,
                null,
                DUMMY_VAT_ID,
                DUMMY_CONDITIONS,
                null
        );
    }

    /**
     * Helper method to create a SampleUpdate OpenBIS Dto for a Supplier for testing purposes.
     *
     * @param permId The supplier's permId.
     * @return The created SampleUpdate object.
     */
    private SampleUpdate getSupplierSampleUpdateDto(String permId) {
        return new SampleUpdate(
                new SamplePermId(permId),
                Collections.emptyMap()
        );
    }

    /**
     * Helper Method to get a List of one SamplePermId for testing purposes.
     *
     * @param permId The supplier's permanent identifier.
     * @return The created List of SamplePermId objects.
     */
    private List<SamplePermId> getSupplierSamplePermIds(String permId) {
        return List.of(new SamplePermId(permId));
    }

    /**
     * Setup method for initializing the test environment.
     * It creates necessary OpenBis properties and initializes the SupplierService with them.
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

        OpenBisProperties properties = new OpenBisProperties(
                DUMMY_API_URL,
                DEFAULT_SPACE_CODE,
                supplierConfig,
                orderConfig,
                ratingConfig
        );

        supplierService = new SupplierService(openBisClient, supplierMapper, properties, orderMapper);
    }

    // Testing

    /**
     * Test method to verify that getAllSuppliers returns an empty list when the OpenBis client returns nothing.
     */
    @Test
    void getAllSuppliers_shouldReturnEmptyList_whenClientReturnsNothing() {

        // Arrange
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<SupplierDetailDto> result = supplierService.getAllSuppliers();

        // Assert
        assertThat(result).isEmpty();
        verify(openBisClient).searchSamples(any(), any());
    }

    /**
     * Test method to verify that getAllSuppliers calculates aggregated stats correctly when all ratings include values
     * for each score.
     * <p>
     * This tests the case where every tested rating has score values for each category. (quality, cost, reliability,
     * and availability)
     * </p>
     * This method also asserts that the getAllSuppliers method returns the expected minimal supplier DTO.
     */
    @Test
    void getAllSuppliers_shouldCalculateAggregatedStatsCorrectlyWhenAllRatingsIncludeValuesForEachScore() {

        // Arrange
        OpenBisSample openBisSupplierSample = createSupplier(DUMMY_SUPPLIER_PERM_ID, List.of(
                createOrder(DUMMY_ORDER_PERM_ID__A, List.of(
                        getDummyRatingSampleA()
                )),
                createOrder(DUMMY_ORDER_PERM_ID__B, List.of(
                        getDummyRatingSampleB()
                ))
        ));
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(openBisSupplierSample));
        when(supplierMapper.toApiDto(any(), any(), any())).thenReturn(getMinimalSupplierDto());

        // Act
        List<SupplierDetailDto> result = supplierService.getAllSuppliers();

        // Assert
        // - Mapper call
        verify(supplierMapper).toApiDto(any(), statsCaptor.capture(), any());

        // - Stats calculation
        RatingStatsDto capturedStats = statsCaptor.getValue();
        assertThat(capturedStats.avgQuality()).isEqualTo(DUMMY_QUALITY_RATING__Av_A_B);
        assertThat(capturedStats.avgCost()).isEqualTo(DUMMY_COST_RATING__Av_A_B);
        assertThat(capturedStats.avgReliability()).isEqualTo(DUMMY_RELIABILITY_RATING__Av_A_B);
        assertThat(capturedStats.avgAvailability()).isEqualTo(DUMMY_AVAILABILITY_RATING__Av_A_B);
        assertThat(capturedStats.avgTotal()).isEqualTo(DUMMY_TOTAL_RATING__Av_A_B);
        assertThat(capturedStats.totalRatingCount()).isEqualTo(DUMMY_RATING_COUNT__A_B);

        // - Return value
        assertThat(result.getFirst()).isEqualTo(getMinimalSupplierDto());
    }

    /**
     * Test method to verify that getAllSuppliers calculates aggregated stats correctly when the ratings include values
     * for different scores.
     * <p>
     * This test ensures that the supplier service correctly aggregates rating statistics when ratings for different
     * scores are present. In some ratings, the optional availability score is missing, but in others it is present.
     * This leads to a situation where the aggregated averages for each score do not automatically lead to the total
     * average score.
     * </p>
     * <p>
     * <pre>
     *     aggregatedTotalScoreAverage == average(all total scores)
     *     aggregatedTotalScoreAverage != average(aggregated averages for each score)
     * </pre>
     * </p>
     * This method also asserts that the getAllSuppliers method returns the expected minimal supplier DTO.
     */
    @Test
    void getAllSuppliers_shouldCalculateAggregatedStatsCorrectlyWhenRatingsIncludeValuesForDifferentScores() {

        // Arrange
        OpenBisSample openBisSupplierSample = createSupplier(DUMMY_SUPPLIER_PERM_ID, List.of(
                createOrder(DUMMY_ORDER_PERM_ID__A, List.of(
                        getDummyRatingSampleA()
                )),
                createOrder(DUMMY_ORDER_PERM_ID__C, List.of(
                        getDummyRatingSampleC()
                ))
        ));
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(openBisSupplierSample));
        when(supplierMapper.toApiDto(any(), any(), any())).thenReturn(getMinimalSupplierDto());

        // Act
        List<SupplierDetailDto> result = supplierService.getAllSuppliers();

        // Assert
        // - Mapper call
        verify(supplierMapper).toApiDto(any(), statsCaptor.capture(), any());

        // - Stats calculation
        RatingStatsDto capturedStats = statsCaptor.getValue();
        assertThat(capturedStats.avgQuality()).isEqualTo(DUMMY_QUALITY_RATING__Av_A_C);
        assertThat(capturedStats.avgCost()).isEqualTo(DUMMY_COST_RATING__Av_A_C);
        assertThat(capturedStats.avgReliability()).isEqualTo(DUMMY_RELIABILITY_RATING__Av_A_C);
        assertThat(capturedStats.avgAvailability()).isEqualTo(DUMMY_AVAILABILITY_RATING__Av_A_C);
        assertThat(capturedStats.avgTotal()).isEqualTo(DUMMY_TOTAL_RATING__Av_A_C);
        assertThat(capturedStats.totalRatingCount()).isEqualTo(DUMMY_RATING_COUNT__A_C);

        // - Return value
        assertThat(result.getFirst()).isEqualTo(getMinimalSupplierDto());
    }

    /**
     * Test method to verify that getSupplierById successfully retrieves and returns a supplier when it exists.
     * <p>
     * It ensures that:
     * <ul>
     *      <li>The OpenBIS client is called to search for the sample.</li>
     *      <li>The mapper is called with the correct OpenBIS sample to convert it to a DTO.</li>
     *      <li>The service returns exactly the mapped SupplierDetailDto.</li>
     * </ul>
     * </p>
     */
    @Test
    void getSupplierById_shouldReturnSupplier_whenFound() {

        // Arrange
        OpenBisSample supplier = createSupplier(DUMMY_SUPPLIER_PERM_ID, Collections.emptyList());
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(supplier));
        when(supplierMapper.toApiDto(any(), any(), any())).thenReturn(getMinimalSupplierDto());

        // Act
        SupplierDetailDto result = supplierService.getSupplierById(DUMMY_SUPPLIER_PERM_ID);

        // Assert
        // - Mapper call
        verify(supplierMapper).toApiDto(eq(supplier), any(), any());

        // - Return value
        assertThat(result).isEqualTo(getMinimalSupplierDto());
    }

    /**
     * Test method to verify that getSupplierById throws an OpenBisResourceNotFoundException
     * when the supplier is not found in OpenBIS.
     * <p>
     * This tests the error handling path where the OpenBIS client returns an empty list,
     * ensuring the service fails gracefully with the correct exception type.
     * </p>
     */
    @Test
    void getSupplierById_shouldThrowException_whenNotFound() {

        // Arrange
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert (Exception Testing)
        org.junit.jupiter.api.Assertions.assertThrows(
                OpenBisResourceNotFoundException.class,
                () -> supplierService.getSupplierById(DUMMY_SUPPLIER_PERM_ID)
        );
    }

    /**
     * Test method to verify the complete creation flow of a supplier.
     * <p>
     * This test ensures the sequence of operations:
     * <ol>
     *      <li>The input DTO is mapped to an OpenBIS creation object.</li>
     *      <li>The OpenBIS client's {@code createSamples} method is called with the captured creation object.</li>
     *      <li>The newly created supplier is immediately fetched back using {@code searchSamples} with the returned PermID.</li>
     *      <li>The fetched sample is mapped back to a read DTO and returned as the result.</li>
     * </ol>
     * </p>
     * It uses ArgumentCaptors to rigorously verify that the OpenBIS client was called with the correct arguments
     * (correct creation data and correct PermID for the refetch).
     */
    @Test
    void createSupplier_shouldCallClientCreateAndRefetch() {

        // Arrange
        SupplierCreationDto creationDto = getSupplierCreationDto();
        when(supplierMapper.toOpenBisCreation(any())).thenReturn(getSupplierSampleCreationDto());
        when(openBisClient.createSamples(any())).thenReturn(getSupplierSamplePermIds(DUMMY_SUPPLIER_PERM_ID));
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(createSupplier(DUMMY_SUPPLIER_PERM_ID, Collections.emptyList())));
        when(supplierMapper.toApiDto(any(), any(), any())).thenReturn(getMinimalSupplierDto());

        // Act
        SupplierDetailDto result = supplierService.createSupplier(creationDto);

        // Assert & Verify
        // - Create call to openBisClient
        verify(openBisClient).createSamples(creationCaptor.capture());
        assertThat(creationCaptor.getValue().getFirst()).isEqualTo(getSupplierSampleCreationDto());

        // - Refetch call to openBisClient
        verify(openBisClient).searchSamples(criteriaCaptor.capture(), any());
        SampleSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        SampleSearchCriteria expectedCriteria = SampleSearchCriteria.create()
                .with(PermIdSearchCriteria.withId(DUMMY_SUPPLIER_PERM_ID));
        assertThat(capturedCriteria)
                .usingRecursiveComparison()
                .isEqualTo(expectedCriteria);

        // - Return value
        assertThat(result).isEqualTo(getMinimalSupplierDto());
    }

    /**
     * Test method to verify the complete update flow of a supplier.
     * <p>
     * This test ensures the sequence of operations:
     * <ol>
     *      <li>The service checks if the supplier exists (via {@code searchSamples}).</li>
     *      <li>The input DTO is mapped to an OpenBIS update object.</li>
     *      <li>The OpenBIS client's {@code updateSamples} method is called with the update object.</li>
     *      <li>The updated supplier is fetched back using {@code searchSamples} (refetch).</li>
     *      <li>The fetched sample is mapped back to a read DTO and returned.</li>
     * </ol>
     * </p>
     */
    @Test
    void updateSupplier_shouldCallClientUpdateAndRefetch() {

        // Arrange
        SupplierUpdateDto updateDto = getSupplierUpdateDto();
        SampleUpdate sampleUpdate = getSupplierSampleUpdateDto(DUMMY_SUPPLIER_PERM_ID);
        OpenBisSample existingSample = createSupplier(DUMMY_SUPPLIER_PERM_ID, Collections.emptyList());
        SupplierDetailDto expectedResult = getMinimalSupplierDto();
        when(supplierMapper.toOpenBisUpdate(any(), any())).thenReturn(sampleUpdate);
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(existingSample));
        when(supplierMapper.toApiDto(any(), any(), any())).thenReturn(expectedResult);

        // Act
        SupplierDetailDto result = supplierService.updateSupplier(DUMMY_SUPPLIER_PERM_ID, updateDto);

        // Assert & Verify
        // - Update call to openBisClient
        verify(openBisClient).updateSamples(List.of(sampleUpdate));

        // - Search call to openBisClient (twice)
        //   - First call: does the supplier exist?
        //   - Second call: refetch supplier
        verify(openBisClient, Mockito.atLeast(2)).searchSamples(any(), any());

        // - Result
        assertThat(result).isEqualTo(expectedResult);
    }

    /**
     * Test method to verify that updateSupplier throws an exception if the supplier does not exist.
     * <p>
     * It ensures that the existence check happens <b>before</b> any update is attempted.
     * </p>
     */
    @Test
    void updateSupplier_shouldThrowException_whenSupplierNotFound() {

        // Arrange
        SupplierUpdateDto updateDto = getSupplierUpdateDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert
        Assertions.assertThrows(
                OpenBisResourceNotFoundException.class,
                () -> supplierService.updateSupplier(DUMMY_SUPPLIER_PERM_ID, updateDto)
        );

        // - No update attempt should be made!
        verify(openBisClient, Mockito.never()).updateSamples(any());
    }
}

