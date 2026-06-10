package io.github.supplierratingsoftware.supplierratingbackend.mapper;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderUpdateDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.OpenBisEntityType;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.OpenBisPermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.update.SampleUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderMapperTest {

    private OrderMapper orderMapper;

    // OpenBis DTO values (generic)
    private static final String DUMMY_PERM_ID = "Test PermId";
    private static final String DUMMY_SAMPLE_CODE = "Test Sample Code";

    // OpenBis DTO values (Order)
    private static final String DUMMY_NAME_PROPERTY = "$NAME";
    private static final String DUMMY_ORDER_NAME = "Test Order";
    private static final String VALID_EXAMPLE_MAIN_CATEGORY_LABEL = "Beschaffung";
    private static final String VALID_CORRESPONDING_MAIN_CATEGORY_CODE = "BESCHAFFUNG";
    private static final String VALID_EXAMPLE_SUB_CATEGORY_LABEL = "PC Software";
    private static final String VALID_CORRESPONDING_SUB_CATEGORY_CODE = "PC_SF";
    private static final String DUMMY_ORDER_REASON = "Test Reason";
    private static final String DUMMY_PURCHASER = "Test Purchaser";
    private static final String DUMMY_ORDER_DATE = "2026-01-01";
    private static final String DUMMY_ORDER_DETAILS = "Test Details";
    private static final String DUMMY_ORDER_FREQUENCY = "Test Frequency";
    private static final String DUMMY_ORDER_CONTACT_NAME = "Test Contact Name";
    private static final String DUMMY_ORDER_CONTACT_EMAIL = "Test Contact Email";
    private static final String DUMMY_ORDER_CONTACT_PHONE = "Test Contact Phone";
    private static final String DUMMY_ORDER_METHOD = "Test Method";
    private static final String DUMMY_ORDER_DELIVERY_DATE = "2026-01-01";
    private static final String DUMMY_ORDER_COMMENT = "Test Comment";

    // OpenBis DTO values (Supplier)
    private static final String DUMMY_SUPPLIER_NAME = "Test Supplier Name";
    private static final String DUMMY_SUPPLIER_ID = "LIEFERANT-12345";

    // OpenBis DTO values (Rating)
    private static final String DUMMY_RATING_ID = "BEWERTUNG-12345";

    // OpenBIS Order Entity Codes
    private static final String ORDER_PROJECT_CODE = "BESTELLUNGEN";
    private static final String ORDER_TYPE_CODE = "BESTELLUNG";
    private static final String ORDER_COLLECTION_CODE = "BESTELLUNGEN";

    // OpenBIS Supplier Entity Codes
    private static final String SUPPLIER_TYPE_CODE = "LIEFERANT";

    // OpenBIS Rating Entity Codes
    private static final String RATING_TYPE_CODE = "BEWERTUNG";

    // OpenBIS Properties
    private static final String DUMMY_API_URL = "dummyApiUrl";
    private static final String DEFAULT_SPACE_CODE = "LIEFERANTENBEWERTUNG";
    private static final String FULL_PROJECT_CODE = "/" + DEFAULT_SPACE_CODE + "/" + ORDER_PROJECT_CODE;
    private static final String FULL_COLLECTION_CODE = FULL_PROJECT_CODE + "/" + ORDER_COLLECTION_CODE;

    // Helper Methods

    /**
     * Helper method to create a full map of properties for an OpenBis sample order.
     *
     * @return A full map of properties for an OpenBis sample order.
     */
    private Map<String, String> getFullOpenBisSampleOrderProperties() {
        Map<String, String> properties = new java.util.HashMap<>(Map.of());
        properties.put(DUMMY_NAME_PROPERTY, DUMMY_ORDER_NAME);
        properties.put(OpenBisSchemaConstants.MAIN_CATEGORY_ORDER_PROPERTY, VALID_CORRESPONDING_MAIN_CATEGORY_CODE);
        properties.put(OpenBisSchemaConstants.SUB_CATEGORY_ORDER_PROPERTY, VALID_CORRESPONDING_SUB_CATEGORY_CODE);
        properties.put(OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY, DUMMY_ORDER_DETAILS);
        properties.put(OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY, DUMMY_ORDER_FREQUENCY);
        properties.put(OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_NAME);
        properties.put(OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_EMAIL);
        properties.put(OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_PHONE);
        properties.put(OpenBisSchemaConstants.ORDER_REASON_ORDER_PROPERTY, DUMMY_ORDER_REASON);
        properties.put(OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY, DUMMY_ORDER_METHOD);
        properties.put(OpenBisSchemaConstants.PURCHASER_ORDER_PROPERTY, DUMMY_PURCHASER);
        properties.put(OpenBisSchemaConstants.ORDER_DATE_ORDER_PROPERTY, DUMMY_ORDER_DATE);
        properties.put(OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY, DUMMY_ORDER_DELIVERY_DATE);
        properties.put(OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY, DUMMY_ORDER_COMMENT);
        return properties;
    }

    /**
     * Helper method to create an OpenBisSample of a Supplier for testing purposes.
     *
     * @return An OpenBisSample of a Supplier with required fields populated.
     */
    private OpenBisSample getOpenBisSupplierSample() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_SUPPLIER_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = new java.util.HashMap<>(Map.of());
        properties.put(DUMMY_NAME_PROPERTY, DUMMY_SUPPLIER_NAME);
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBisSample with null permID and null properties of a Supplier for testing purposes.
     *
     * @return An OpenBisSample of a Supplier with required fields populated.
     */
    private OpenBisSample getOpenBisSupplierWithNullPermIdAndNullPropertiesSample() {
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = new java.util.HashMap<>(Map.of());
        properties.put(DUMMY_NAME_PROPERTY, null);
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBisSample of a Rating for testing purposes.
     *
     * @return An OpenBisSample of a Rating with required fields populated.
     */
    private OpenBisSample getOpenBisRatingSample() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, null, null, null);
    }

    /**
     * Helper method to create an OpenBisSample of a Rating with null permID for testing purposes.
     *
     * @return An OpenBisSample of a Rating with required fields populated.
     */
    private OpenBisSample getOpenBisRatingWithNullPermIdSample() {
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, null, null, null);
    }

    /**
     * Helper method to create an OpenBisSample representing a full Order for testing purposes.
     *
     * @return A full OpenBisSample of an Order with required fields populated.
     */
    private OpenBisSample getFullOpenBisOrderSample() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleOrderProperties();
        List<OpenBisSample> parents = List.of(getOpenBisSupplierSample());
        List<OpenBisSample> children = List.of(getOpenBisRatingSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, children);
    }

    /**
     * Helper method to create an OpenBisSample representing an Order without a Supplier for testing purposes.
     *
     * @return An OpenBisSample of an Order without a Supplier with required fields populated.
     */
    private OpenBisSample getOpenBisOrderSampleWithoutSupplier() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleOrderProperties();
        List<OpenBisSample> children = List.of(getOpenBisRatingSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, children);
    }

    /**
     * Helper method to create an OpenBisSample representing an Order with an empty Supplier list for testing purposes.
     *
     * @return An OpenBisSample of an Order with an empty Supplier list with required fields populated.
     */
    private OpenBisSample getOpenBisOrderSampleWithEmptySupplierList() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleOrderProperties();
        List<OpenBisSample> parents = List.of();
        List<OpenBisSample> children = List.of(getOpenBisRatingSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, children);
    }

    /**
     * Helper method to create an OpenBisSample representing an Order with a supplier with null permId and null properties for testing purposes.
     *
     * @return An OpenBisSample of an Order with with a supplier with null permId and null properties with required fields populated.
     */
    private OpenBisSample getOpenBisOrderSampleWithSupplierWithNullPermIdAndNullProperties() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleOrderProperties();
        List<OpenBisSample> parents = List.of(getOpenBisSupplierWithNullPermIdAndNullPropertiesSample());
        List<OpenBisSample> children = List.of(getOpenBisRatingSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, children);
    }

    /**
     * Helper method to create an OpenBisSample representing an Order without Rating or Supplier for testing purposes.
     *
     * @return An OpenBisSample of an Order without Rating or Supplier with required fields populated.
     */
    private OpenBisSample getOpenBisOrderSampleWithoutSupplierAndWithoutRating() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleOrderProperties();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBisSample representing an Order with an empty Rating list for testing purposes.
     *
     * @return An OpenBisSample of an Order with an empty Rating list with required fields populated.
     */
    private OpenBisSample getOpenBisOrderSampleWithEmptyRatingList() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleOrderProperties();
        List<OpenBisSample> children = List.of();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, children);
    }

    /**
     * Helper method to create an OpenBisSample representing an Order with a rating with null permId for testing purposes.
     *
     * @return An OpenBisSample of an Order with a rating with null permId with required fields populated.
     */
    private OpenBisSample getOpenBisOrderSampleWithRatingWithNullPermId() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleOrderProperties();
        List<OpenBisSample> children = List.of(getOpenBisRatingWithNullPermIdSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, children);
    }

    /**
     * Helper method to create a minimal OrderCreationDto for testing purposes.
     *
     * @return A minimal OrderCreationDto with required fields populated.
     */
    OrderCreationDto getMinimalOrderDto() {
        return new OrderCreationDto(
                DUMMY_ORDER_NAME,
                VALID_EXAMPLE_MAIN_CATEGORY_LABEL,
                VALID_EXAMPLE_SUB_CATEGORY_LABEL,
                null,
                null,
                null,
                null,
                null,
                DUMMY_ORDER_REASON,
                null,
                DUMMY_PURCHASER,
                DUMMY_ORDER_DATE,
                null,
                null,
                DUMMY_SUPPLIER_ID
        );
    }

    /**
     * Helper method to create a full OrderCreationDto for testing purposes.
     *
     * @return A full OrderCreationDto with all fields populated.
     */
    OrderCreationDto getFullOrderDto() {
        return new OrderCreationDto(
                DUMMY_ORDER_NAME,
                VALID_EXAMPLE_MAIN_CATEGORY_LABEL,
                VALID_EXAMPLE_SUB_CATEGORY_LABEL,
                DUMMY_ORDER_DETAILS,
                DUMMY_ORDER_FREQUENCY,
                DUMMY_ORDER_CONTACT_NAME,
                DUMMY_ORDER_CONTACT_EMAIL,
                DUMMY_ORDER_CONTACT_PHONE,
                DUMMY_ORDER_REASON,
                DUMMY_ORDER_METHOD,
                DUMMY_PURCHASER,
                DUMMY_ORDER_DATE,
                DUMMY_ORDER_DELIVERY_DATE,
                DUMMY_ORDER_COMMENT,
                DUMMY_SUPPLIER_ID
        );
    }

    /**
     * Helper method to create a minimal OrderUpdateDto for testing purposes.
     *
     * @return A minimal OrderUpdateDto with required fields populated.
     */
    OrderUpdateDto getMinimalOrderUpdateDto() {
        return new OrderUpdateDto(
                DUMMY_ORDER_NAME,
                VALID_EXAMPLE_MAIN_CATEGORY_LABEL,
                VALID_EXAMPLE_SUB_CATEGORY_LABEL,
                null,
                null,
                null,
                null,
                null,
                DUMMY_ORDER_REASON,
                null,
                DUMMY_PURCHASER,
                DUMMY_ORDER_DATE,
                null,
                null
                );
    }

    /**
     * Helper method to create a full OrderUpdateDto for testing purposes.
     *
     * @return A full OrderUpdateDto with all fields populated.
     */
    OrderUpdateDto getFullOrderUpdateDto() {
        return new OrderUpdateDto(
                DUMMY_ORDER_NAME,
                VALID_EXAMPLE_MAIN_CATEGORY_LABEL,
                VALID_EXAMPLE_SUB_CATEGORY_LABEL,
                DUMMY_ORDER_DETAILS,
                DUMMY_ORDER_FREQUENCY,
                DUMMY_ORDER_CONTACT_NAME,
                DUMMY_ORDER_CONTACT_EMAIL,
                DUMMY_ORDER_CONTACT_PHONE,
                DUMMY_ORDER_REASON,
                DUMMY_ORDER_METHOD,
                DUMMY_PURCHASER,
                DUMMY_ORDER_DATE,
                DUMMY_ORDER_DELIVERY_DATE,
                DUMMY_ORDER_COMMENT
                );
    }

    /**
     * Helper method to create an OrderUpdateDto that clears optional fields for testing.
     * <p>
     * This DTO sets selected optional string fields to empty strings so that tests can
     * verify that applying the update results in those fields being cleared.
     * </p>
     *
     * @return An OrderUpdateDto configured to clear optional fields when applied.
     */
    OrderUpdateDto getClearingOrderUpdateDto() {
        return new OrderUpdateDto(
                DUMMY_ORDER_NAME,
                VALID_EXAMPLE_MAIN_CATEGORY_LABEL,
                VALID_EXAMPLE_SUB_CATEGORY_LABEL,
                "", // <-- field should get cleared
                "", // <-- field should get cleared
                "", // <-- field should get cleared
                "", // <-- field should get cleared
                "", // <-- field should get cleared
                DUMMY_ORDER_REASON,
                "", // <-- field should get cleared
                DUMMY_PURCHASER,
                DUMMY_ORDER_DATE,
                "", // <-- field should get cleared
                "" // <-- field should get cleared
        );
    }

    /**
     * Setup method to set up the test environment with predefined OpenBis properties.
     */
    @BeforeEach
    void setUp() {

        OpenBisProperties.EntityConfig orderConfig = new OpenBisProperties.EntityConfig(
                ORDER_PROJECT_CODE,
                ORDER_TYPE_CODE,
                ORDER_COLLECTION_CODE
        );

        OpenBisProperties properties = new OpenBisProperties(
                DUMMY_API_URL,
                DEFAULT_SPACE_CODE,
                null,
                orderConfig,
                null,
                DUMMY_NAME_PROPERTY
        );

        orderMapper = new OrderMapper(properties);
    }

    // toApiDto(OpenBisSample) Tests

    /**
     * Tests that the toApiDto(OpenBisSample) method returns null if the provided OpenBis sample is null.
     */
    @Test
    void toApiDto_withOpenBisSample_shouldReturnNullIfSampleIsNull() {

        // Arrange
        OpenBisSample sample = null;

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample);

        // Assert
        assertThat(result).isNull();
    }

    /**
     * Tests that the toApiDto(OpenBisSample) method resolves supplier information from the parent sample if available.
     */
    @Test
    void toApiDto_withOpenBisSample_shouldResolveSupplierInfoFromParent() {

        // Arrange
        OpenBisSample sample = getFullOpenBisOrderSample();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isEqualTo(DUMMY_SUPPLIER_ID);
        assertThat(result.supplierName()).isEqualTo(DUMMY_SUPPLIER_NAME);
    }

    /**
     * Tests that the toApiDto(OpenBisSample) method handles an Order without a Supplier correctly.
     */
    @Test
    void toApiDto_withOpenBisSample_shouldHandleOrderWithoutSupplier() {

        // Arrange
        OpenBisSample sample = getOpenBisOrderSampleWithoutSupplier();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that the toApiDto(OpenBisSample) method handles an Order with an empty Supplier list correctly.
     */
    @Test
    void toApiDto_withOpenBisSample_shouldHandleOrderWithEmptySupplierList() {

        // Arrange
        OpenBisSample sample = getOpenBisOrderSampleWithEmptySupplierList();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that the toApiDto(OpenBisSample) method handles an Order with a supplier with null permId and null properties correctly.
     */
    @Test
    void toApiDto_withOpenBisSample_shouldHandleOrderWithSupplierWithNullPermIdAndNullProperties() {

        // Arrange
        OpenBisSample sample = getOpenBisOrderSampleWithSupplierWithNullPermIdAndNullProperties();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    // toApiDto(OpenBisSample, String, String) Tests

    /**
     * Tests that the toApiDto(OpenBisSample, String, String) method maps vocabulary codes to labels correctly.
     */
    @Test
    void toApiDto_withOpenBisSampleAndSupplierIdAndSupplierName_shouldMapVocabularyCodesToLabels() {

        // Arrange
        OpenBisSample sample = getOpenBisOrderSampleWithoutSupplier();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample, DUMMY_SUPPLIER_ID, DUMMY_SUPPLIER_NAME);

        // Assert
        assertThat(result.mainCategory()).isEqualTo(VALID_EXAMPLE_MAIN_CATEGORY_LABEL);
        assertThat(result.subCategory()).isEqualTo(VALID_EXAMPLE_SUB_CATEGORY_LABEL);
    }

    /**
     * Tests that the toApiDto(OpenBisSample, String, String) method resolves rating information from the children sample if available.
     */
    @Test
    void toApiDto_withOpenBisSampleAndSupplierIdAndSupplierName_shouldResolveRatingInfoFromChildren() {

        // Arrange
        OpenBisSample sample = getFullOpenBisOrderSample();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample, DUMMY_SUPPLIER_ID, DUMMY_SUPPLIER_NAME);

        // Assert
        assertThat(result.ratingStatus()).isEqualTo(OpenBisSchemaConstants.RATING_STATUS_RATED_ORDER_PROPERTY);
        assertThat(result.ratingId()).isEqualTo(DUMMY_RATING_ID);
    }

    /**
     * Tests that the toApiDto(OpenBisSample, String, String) method handles rating information when no rating sample is available.
     */
    @Test
    void toApiDto_withOpenBisSampleAndSupplierIdAndSupplierName_shouldHandleRatingInfoInCaseOfOrderWithoutRating() {

        // Arrange
        OpenBisSample sample = getOpenBisOrderSampleWithoutSupplierAndWithoutRating();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample, DUMMY_SUPPLIER_ID, DUMMY_SUPPLIER_NAME);

        // Assert
        assertThat(result.ratingStatus()).isEqualTo(OpenBisSchemaConstants.RATING_STATUS_PENDING_ORDER_PROPERTY);
        assertThat(result.ratingId()).isNull();
    }

    /**
     * Tests that the toApiDto(OpenBisSample, String, String) method handles rating information when list of ratings is empty.
     */
    @Test
    void toApiDto_withOpenBisSampleAndSupplierIdAndSupplierName_shouldHandleRatingInfoInCaseOfOrderWithEmptyRatingList() {

        // Arrange
        OpenBisSample sample = getOpenBisOrderSampleWithEmptyRatingList();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample, DUMMY_SUPPLIER_ID, DUMMY_SUPPLIER_NAME);

        // Assert
        assertThat(result.ratingStatus()).isEqualTo(OpenBisSchemaConstants.RATING_STATUS_PENDING_ORDER_PROPERTY);
        assertThat(result.ratingId()).isNull();
    }

    /**
     * Tests that the toApiDto(OpenBisSample, String, String) method handles rating information when rating sample has null permId.
     */
    @Test
    void toApiDto_withOpenBisSampleAndSupplierIdAndSupplierName_shouldHandleRatingInfoInCaseOfOrderWithRatingWithNullPermId() {

        // Arrange
        OpenBisSample sample = getOpenBisOrderSampleWithRatingWithNullPermId();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample, DUMMY_SUPPLIER_ID, DUMMY_SUPPLIER_NAME);

        // Assert
        assertThat(result.ratingStatus()).isEqualTo(OpenBisSchemaConstants.RATING_STATUS_RATED_ORDER_PROPERTY);
        assertThat(result.ratingId()).isNull();
    }

    /**
     * Tests that the toApiDto(OpenBisSample, String, String) method maps all properties correctly.
     */
    @Test
    void toApiDto_withOpenBisSampleAndSupplierIdAndSupplierName_shouldMapAllProperties() {

        // Arrange
        OpenBisSample sample = getFullOpenBisOrderSample();

        // Act
        OrderDetailDto result = orderMapper.toApiDto(sample, DUMMY_SUPPLIER_ID, DUMMY_SUPPLIER_NAME);

        // Assert
        assertThat(result.name()).isEqualTo(DUMMY_ORDER_NAME);
        assertThat(result.details()).isEqualTo(DUMMY_ORDER_DETAILS);
        assertThat(result.frequency()).isEqualTo(DUMMY_ORDER_FREQUENCY);
        assertThat(result.contactPerson()).isEqualTo(DUMMY_ORDER_CONTACT_NAME);
        assertThat(result.contactEmail()).isEqualTo(DUMMY_ORDER_CONTACT_EMAIL);
        assertThat(result.contactPhone()).isEqualTo(DUMMY_ORDER_CONTACT_PHONE);
        assertThat(result.reason()).isEqualTo(DUMMY_ORDER_REASON);
        assertThat(result.orderMethod()).isEqualTo(DUMMY_ORDER_METHOD);
        assertThat(result.orderedBy()).isEqualTo(DUMMY_PURCHASER);
        assertThat(result.orderDate()).isEqualTo(DUMMY_ORDER_DATE);
        assertThat(result.deliveryDate()).isEqualTo(DUMMY_ORDER_DELIVERY_DATE);
        assertThat(result.orderComment()).isEqualTo(DUMMY_ORDER_COMMENT);
        assertThat(result.id()).isEqualTo(DUMMY_PERM_ID);
    }

    // toOpenBisCreation() Tests

    /**
     * Tests that the toOpenBisCreation method provides the correct space ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideTheCorrectSpaceId() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.spaceId().permId()).isEqualTo(DEFAULT_SPACE_CODE);
    }

    /**
     * Tests that the toOpenBisCreation method provides the correct project ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideTheCorrectProjectId() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.projectId().identifier()).isEqualTo(FULL_PROJECT_CODE);
    }

    /**
     * Tests that the toOpenBisCreation method provides the correct experiment ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideTheCorrectExperimentId() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.experimentId().identifier()).isEqualTo(FULL_COLLECTION_CODE);
    }

    /**
     * Tests that the toOpenBisCreation method provides the correct type ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideTheCorrectTypeId() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.typeId().permId()).isEqualTo(ORDER_TYPE_CODE);
    }

    /**
     * Tests that the toOpenBisCreation method generates a prefixed order code.
     */
    @Test
    void toOpenBisCreation_shouldGeneratePrefixedOrderCode() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.code()).startsWith(ORDER_TYPE_CODE + "-");
    }

    /**
     * Tests that the toOpenBisCreation method maps the main and subcategory labels to the corresponding codes.
     *
     * This test ensures that the mapper correctly translates the provided vocabulary labels into their respective codes
     * for the main and subcategories, which are then used in the OpenBIS creation process.
     */
    @Test
    void toOpenBisCreation_shouldMapVocabularyLabelsToCodes() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        // - Assert that the main category is mapped correctly
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.MAIN_CATEGORY_ORDER_PROPERTY, VALID_CORRESPONDING_MAIN_CATEGORY_CODE);
        // - Assert that the subcategory is mapped correctly
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.SUB_CATEGORY_ORDER_PROPERTY, VALID_CORRESPONDING_SUB_CATEGORY_CODE);
    }

    /**
     * Tests that the toOpenBisCreation method sets all required properties.
     */
    @Test
    void toOpenBisCreation_shouldSetAllRequiredProperties() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).containsEntry(DUMMY_NAME_PROPERTY, DUMMY_ORDER_NAME);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_REASON_ORDER_PROPERTY, DUMMY_ORDER_REASON);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PURCHASER_ORDER_PROPERTY, DUMMY_PURCHASER);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_DATE_ORDER_PROPERTY, DUMMY_ORDER_DATE);
    }

    /**
     * Tests that the toOpenBisCreation method sets all optional properties.
     */
    @Test
    void toOpenBisCreation_shouldSetAllOptionalProperties() {

        // Arrange
        OrderCreationDto dto = getFullOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY, DUMMY_ORDER_DETAILS);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY, DUMMY_ORDER_FREQUENCY);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_NAME);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_EMAIL);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_PHONE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY, DUMMY_ORDER_METHOD);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY, DUMMY_ORDER_DELIVERY_DATE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY, DUMMY_ORDER_COMMENT);
    }

    /**
     * Tests that the toOpenBisCreation method ignores null values for optional properties.
     */
    @Test
    void toOpenBisCreation_shouldIgnoreNullValues() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY);
    }

    /**
     * Tests that the toOpenBisCreation method provides the corresponding parent supplier permId.
     */
    @Test
    void toOpenBisCreation_shouldProvideTheCorrespondingParentSupplierPermId() {

        // Arrange
        OrderCreationDto dto = getMinimalOrderDto();

        // Act
        SampleCreation result = orderMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.parentIds().getFirst().permId()).isEqualTo(DUMMY_SUPPLIER_ID);
    }

    // toOpenBisUpdate() Tests

    /**
     * Tests that the toOpenBisUpdate method provides the correct permId of the order to be updated.
     */
    @Test
    void toOpenBisUpdate_shouldProvideTheCorrectPermId() {

        // Arrange
        OrderUpdateDto dto = getMinimalOrderUpdateDto();

        // Act
        SampleUpdate result = orderMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.sampleId().permId()).isEqualTo(DUMMY_PERM_ID);
    }

    /**
     * Tests that the toOpenBisUpdate method maps vocabulary labels to their corresponding codes.
     */
    @Test
    void toOpenBisUpdate_shouldMapVocabularyLabelsToCodes() {

        // Arrange
        OrderUpdateDto dto = getMinimalOrderUpdateDto();

        // Act
        SampleUpdate result = orderMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        // - Assert that the main category is mapped correctly
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.MAIN_CATEGORY_ORDER_PROPERTY, VALID_CORRESPONDING_MAIN_CATEGORY_CODE);
        // - Assert that the subcategory is mapped correctly
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.SUB_CATEGORY_ORDER_PROPERTY, VALID_CORRESPONDING_SUB_CATEGORY_CODE);
    }

    /**
     * Tests that the toOpenBisUpdate method sets properties present in the provided DTO.
     */
    @Test
    void toOpenBisUpdate_shouldSetPropertiesPresentInDto() {

        // Arrange
        OrderUpdateDto dto = getMinimalOrderUpdateDto();

        // Act
        SampleUpdate result = orderMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.properties()).containsEntry(DUMMY_NAME_PROPERTY, DUMMY_ORDER_NAME);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_REASON_ORDER_PROPERTY, DUMMY_ORDER_REASON);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PURCHASER_ORDER_PROPERTY, DUMMY_PURCHASER);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_DATE_ORDER_PROPERTY, DUMMY_ORDER_DATE);
    }

    /**
     * Tests that the toOpenBisUpdate method ignores null values in the provided DTO.
     */
    @Test
    void toOpenBisUpdate_shouldIgnoreNullValues() {

        // Arrange
        OrderUpdateDto dto = getMinimalOrderUpdateDto();

        // Act
        SampleUpdate result = orderMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY);
    }

    @Test
    void toOpenBisUpdate_shouldSetOptionalFieldsWhenProvided() {

        // Arrange
        OrderUpdateDto dto = getFullOrderUpdateDto();

        // Act
        SampleUpdate result = orderMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY, DUMMY_ORDER_DETAILS);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY, DUMMY_ORDER_FREQUENCY);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_NAME);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_EMAIL);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY, DUMMY_ORDER_CONTACT_PHONE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY, DUMMY_ORDER_METHOD);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY, DUMMY_ORDER_DELIVERY_DATE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY, DUMMY_ORDER_COMMENT);
    }

    /**
     * Tests that the toOpenBisUpdate method maps empty strings in optional fields to empty strings in the properties map.
     * This ensures that fields can be explicitly cleared by sending an empty string. An empty string gets transmitted and
     * signals OpenBIS to clear the field.
     */
    @Test
    void toOpenBisUpdate_shouldMapEmptyStringsToEmptyProperties() {

        // Arrange
        OrderUpdateDto dto = getClearingOrderUpdateDto();

        // Act
        SampleUpdate result = orderMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        // Verify that empty strings are PASSED to the map (not ignored)
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY, "");
    }
}
