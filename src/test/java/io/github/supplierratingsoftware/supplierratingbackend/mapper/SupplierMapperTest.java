package io.github.supplierratingsoftware.supplierratingbackend.mapper;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingStatsDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierUpdateDto;
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

public class SupplierMapperTest {

    private SupplierMapper supplierMapper;

    // OpenBis DTO values (generic)
    private static final String DUMMY_PERM_ID = "Test Supplier Perm ID";
    private static final String DUMMY_SAMPLE_CODE = "Test Sample Code";

    // OpenBis DTO values (Supplier)
    private static final String DUMMY_NAME_PROPERTY = "$NAME";
    private static final String DUMMY_SUPPLIER_NAME = "Test Supplier Name";
    private static final String DUMMY_CUSTOMER_NUMBER = "Test Customer Number";
    private static final String DUMMY_ADDITION = "Test Addition";
    private static final String DUMMY_STREET = "Test Street";
    private static final String DUMMY_PO_BOX = "Test Post Office Box";
    private static final String VALID_EXAMPLE_COUNTRY_LABEL = "CH";
    private static final String VALID_CORRESPONDING_COUNTRY_CODE = "CH";
    private static final String DUMMY_ZIP_CODE = "Test Zip Code";
    private static final String DUMMY_CITY = "Test City";
    private static final String DUMMY_WEBSITE = "Test Website";
    private static final String DUMMY_EMAIL = "Test Email";
    private static final String DUMMY_PHONE_NUMBER = "Test Phone Number";
    private static final String DUMMY_VAT_ID = "Test VAT ID";
    private static final String DUMMY_CONDITIONS = "Test Conditions";
    private static final String DUMMY_CUSTOMER_INFO = "Test Customer Info";

    // OpenBis DTO values (Order)
    private static final String DUMMY_ORDER_NAME = "Test Order";
    private static final String VALID_EXAMPLE_MAIN_CATEGORY_LABEL = "Beschaffung";
    private static final String VALID_EXAMPLE_SUB_CATEGORY_LABEL = "PC Software";
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
    private static final String DUMMY_ORDER_PERM_ID = "Test Order Perm ID";
    private static final String DUMMY_ORDER_TYPE_CODE = "Test Order Type Code";
    private static final String DUMMY_ORDER_RATING_STATUS = "RATED";

    // OpenBis DTO values (Rating)
    private static final String DUMMY_RATING_ID = "Test Rating ID";

    // OpenBIS Supplier Entity Codes
    private static final String SUPPLIER_PROJECT_CODE = "LIEFERANTEN";
    private static final String SUPPLIER_TYPE_CODE = "LIEFERANT";
    private static final String SUPPLIER_COLLECTION_CODE = "LIEFERANTEN";

    // OpenBIS Properties
    private static final String DUMMY_API_URL = "dummyApiUrl";
    private static final String DUMMY_USER = "dummyUser";
    private static final String DUMMY_PASSWORD = "dummyPassword";
    private static final String DEFAULT_SPACE_CODE = "LIEFERANTENBEWERTUNG";
    private static final String FULL_PROJECT_CODE = "/" + DEFAULT_SPACE_CODE + "/" + SUPPLIER_PROJECT_CODE;
    private static final String FULL_COLLECTION_CODE = FULL_PROJECT_CODE + "/" + SUPPLIER_COLLECTION_CODE;

    // Helper Methods

    /**
     * Helper method to create a full property map with all properties.
     *
     * @return Map with all properties.
     */
    private Map<String, String> getFullOpenBisSampleSupplierProperties() {
        Map<String, String> properties = new java.util.HashMap<>(Map.of());
        properties.put(DUMMY_NAME_PROPERTY, DUMMY_SUPPLIER_NAME);
        properties.put(OpenBisSchemaConstants.CUSTOMER_NUMBER_SUPPLIER_PROPERTY, DUMMY_CUSTOMER_NUMBER);
        properties.put(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY, DUMMY_ADDITION);
        properties.put(OpenBisSchemaConstants.STREET_SUPPLIER_PROPERTY, DUMMY_STREET);
        properties.put(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY, DUMMY_PO_BOX);
        properties.put(OpenBisSchemaConstants.COUNTRY_SUPPLIER_PROPERTY, VALID_CORRESPONDING_COUNTRY_CODE);
        properties.put(OpenBisSchemaConstants.ZIP_CODE_SUPPLIER_PROPERTY, DUMMY_ZIP_CODE);
        properties.put(OpenBisSchemaConstants.CITY_SUPPLIER_PROPERTY, DUMMY_CITY);
        properties.put(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY, DUMMY_WEBSITE);
        properties.put(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY, DUMMY_EMAIL);
        properties.put(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY, DUMMY_PHONE_NUMBER);
        properties.put(OpenBisSchemaConstants.VAT_ID_SUPPLIER_PROPERTY, DUMMY_VAT_ID);
        properties.put(OpenBisSchemaConstants.CONDITIONS_SUPPLIER_PROPERTY, DUMMY_CONDITIONS);
        properties.put(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY, DUMMY_CUSTOMER_INFO);
        return properties;
    }

    /**
     * Helper method to create a property map with null values for all properties.
     *
     * @return Map with null values for all properties.
     */
    private Map<String, String> getOpenBisSampleSupplierPropertiesWithNullValues() {
        Map<String, String> properties = new java.util.HashMap<>(Map.of());
        properties.put(DUMMY_NAME_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.CUSTOMER_NUMBER_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.STREET_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.COUNTRY_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.ZIP_CODE_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.CITY_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.VAT_ID_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.CONDITIONS_SUPPLIER_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY, null);
        return properties;
    }

    /**
     * Helper method to create an empty property map.
     *
     * @return OpenBisSample object with all properties.
     */
    private Map<String, String> getEmptyOpenBisSampleSupplierProperties() {
        return new java.util.HashMap<>(Map.of());
    }

    /**
     * Helper method to create a full OpenBisSample object with all properties.
     *
     * @return OpenBisSample object with all properties.
     */
    private OpenBisSample getFullOpenBisSupplierSample() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleSupplierProperties();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBisSample object without any properties.
     *
     * @return OpenBisSample object without any properties.
     */
    private OpenBisSample getOpenBisSupplierSampleWithoutProperties() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, null, null, null);
    }

    /**
     * Helper method to create an OpenBisSample object with null values for all properties.
     *
     * @return OpenBisSample object with null values for all properties.
     */
    private OpenBisSample getOpenBisSupplierSampleWithNullPropertiesValues() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleSupplierPropertiesWithNullValues();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBisSample object with an empty property list.
     *
     * @return OpenBisSample object with an empty property list.
     */
    private OpenBisSample getOpenBisSupplierSampleWithEmptyProperties() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_PERM_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = getEmptyOpenBisSampleSupplierProperties();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBisSample object without a permId.
     *
     * @return OpenBisSample object without permId.
     */
    private OpenBisSample getOpenBisSupplierSampleWithoutPermId() {
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = getFullOpenBisSampleSupplierProperties();
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create a RatingStatsDto object for testing.
     *
     * @return RatingStatsDto object.
     */
    private RatingStatsDto getRatingStatsDto() {
        return new RatingStatsDto(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                4
        );
    }

    /**
     * Helper method to create a full list of OrderDetailDto objects for testing.
     *
     * @return List of OrderDetailDto objects.
     */
    private List<OrderDetailDto> getFullOrdersList() {
        return List.of(
                new OrderDetailDto(
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
                        DUMMY_ORDER_PERM_ID,
                        DUMMY_ORDER_TYPE_CODE,
                        DUMMY_ORDER_RATING_STATUS,
                        DUMMY_PERM_ID,
                        DUMMY_SUPPLIER_NAME,
                        DUMMY_RATING_ID
                )
        );
    }

    private SupplierCreationDto getMinimalSupplierCreationDto() {
        return new SupplierCreationDto(
                DUMMY_SUPPLIER_NAME,
                DUMMY_CUSTOMER_NUMBER,
                null,
                DUMMY_STREET,
                null,
                VALID_EXAMPLE_COUNTRY_LABEL,
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

    private SupplierCreationDto getFullSupplierCreationDto() {
        return new SupplierCreationDto(
                DUMMY_SUPPLIER_NAME,
                DUMMY_CUSTOMER_NUMBER,
                DUMMY_ADDITION,
                DUMMY_STREET,
                DUMMY_PO_BOX,
                VALID_EXAMPLE_COUNTRY_LABEL,
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

    private SupplierUpdateDto getMinimalSupplierUpdateDto() {
        return new SupplierUpdateDto(
                DUMMY_SUPPLIER_NAME,
                DUMMY_CUSTOMER_NUMBER,
                null,
                DUMMY_STREET,
                null,
                VALID_EXAMPLE_COUNTRY_LABEL,
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

    private SupplierUpdateDto getFullSupplierUpdateDto() {
        return new SupplierUpdateDto(
                DUMMY_SUPPLIER_NAME,
                DUMMY_CUSTOMER_NUMBER,
                DUMMY_ADDITION,
                DUMMY_STREET,
                DUMMY_PO_BOX,
                VALID_EXAMPLE_COUNTRY_LABEL,
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
     * Helper method to create a SupplierUpdateDto object with optional fields set to empty strings.
     * This is useful for testing scenarios where fields need to be explicitly cleared.
     *
     * @return SupplierUpdateDto object with all optional fields set to empty strings.
     */
    private SupplierUpdateDto getClearingSupplierUpdateDto() {
        return new SupplierUpdateDto(
                DUMMY_SUPPLIER_NAME,
                DUMMY_CUSTOMER_NUMBER,
                "", // <-- field should get cleared
                DUMMY_STREET,
                "", // <-- field should get cleared
                VALID_EXAMPLE_COUNTRY_LABEL,
                DUMMY_ZIP_CODE,
                DUMMY_CITY,
                "", // <-- field should get cleared
                "", // <-- field should get cleared
                "", // <-- field should get cleared
                DUMMY_VAT_ID,
                DUMMY_CONDITIONS,
                "" // <-- field should get cleared
        );
    }

    // Setup

    /**
     * Setup method to set up the test environment with predefined OpenBis properties.
     */
    @BeforeEach
    void setUp() {

        OpenBisProperties.EntityConfig supplierConfig = new OpenBisProperties.EntityConfig(
                SUPPLIER_PROJECT_CODE,
                SUPPLIER_TYPE_CODE,
                SUPPLIER_COLLECTION_CODE
        );

        OpenBisProperties properties = new OpenBisProperties(
                DUMMY_API_URL,
                DEFAULT_SPACE_CODE,
                supplierConfig,
                null,
                null,
                DUMMY_NAME_PROPERTY
        );

        supplierMapper = new SupplierMapper(properties);
    }

    // toApiDto Tests

    /**
     * Tests that the toApiDto() method returns null if the provided OpenBis sample is null.
     */
    @Test
    void toApiDto_shouldReturnNullIfSamplesNull() {

        // Arrange
        OpenBisSample sample = null;

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, null, null);

        // Assert
        assertThat(result).isNull();
    }

    /**
     * Tests that the toApiDto() method maps vocabulary codes to labels correctly.
     */
    @Test
    void toApiDto_shouldMapVocabularyCodesToLabels() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.country()).isEqualTo(VALID_EXAMPLE_COUNTRY_LABEL);
    }

    /**
     * Tests that the toApiDto() method maps all properties correctly.
     */
    @Test
    void toApiDto_shouldMapAllProperties() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.name()).isEqualTo(DUMMY_SUPPLIER_NAME);
        assertThat(result.customerNumber()).isEqualTo(DUMMY_CUSTOMER_NUMBER);
        assertThat(result.addition()).isEqualTo(DUMMY_ADDITION);
        assertThat(result.street()).isEqualTo(DUMMY_STREET);
        assertThat(result.poBox()).isEqualTo(DUMMY_PO_BOX);
        assertThat(result.zipCode()).isEqualTo(DUMMY_ZIP_CODE);
        assertThat(result.city()).isEqualTo(DUMMY_CITY);
        assertThat(result.website()).isEqualTo(DUMMY_WEBSITE);
        assertThat(result.email()).isEqualTo(DUMMY_EMAIL);
        assertThat(result.phoneNumber()).isEqualTo(DUMMY_PHONE_NUMBER);
        assertThat(result.vatId()).isEqualTo(DUMMY_VAT_ID);
        assertThat(result.conditions()).isEqualTo(DUMMY_CONDITIONS);
        assertThat(result.customerInfo()).isEqualTo(DUMMY_CUSTOMER_INFO);
    }

    /**
     * Tests that the toApiDto() method sets all properties to null if the OpenBis sample does not contain any properties.
     */
    @Test
    void toApiDto_shouldSetAllPropertiesNullIfNullPropertiesObjectProvided() {

        // Arrange
        OpenBisSample sample = getOpenBisSupplierSampleWithoutProperties();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.name()).isNull();
        assertThat(result.customerNumber()).isNull();
        assertThat(result.addition()).isNull();
        assertThat(result.street()).isNull();
        assertThat(result.poBox()).isNull();
        assertThat(result.zipCode()).isNull();
        assertThat(result.city()).isNull();
        assertThat(result.website()).isNull();
        assertThat(result.email()).isNull();
        assertThat(result.phoneNumber()).isNull();
        assertThat(result.vatId()).isNull();
        assertThat(result.conditions()).isNull();
        assertThat(result.customerInfo()).isNull();
    }

    /**
     * Tests that the toApiDto() method sets properties to null if the OpenBis sample does provide them as null.
     */
    @Test
    void toApiDto_shouldSetNotProvidedPropertiesToNull() {

        // Arrange
        OpenBisSample sample = getOpenBisSupplierSampleWithNullPropertiesValues();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.name()).isNull();
        assertThat(result.customerNumber()).isNull();
        assertThat(result.addition()).isNull();
        assertThat(result.street()).isNull();
        assertThat(result.poBox()).isNull();
        assertThat(result.zipCode()).isNull();
        assertThat(result.city()).isNull();
        assertThat(result.website()).isNull();
        assertThat(result.email()).isNull();
        assertThat(result.phoneNumber()).isNull();
        assertThat(result.vatId()).isNull();
        assertThat(result.conditions()).isNull();
        assertThat(result.customerInfo()).isNull();
    }

    /**
     * Tests that the toApiDto() method sets properties to null if the OpenBis sample property list is empty.
     */
    @Test
    void toApiDto_shouldSetAllPropertiesToNullIfOpenBisSamplePropertyListIsEmpty() {

        // Arrange
        OpenBisSample sample = getOpenBisSupplierSampleWithEmptyProperties();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.name()).isNull();
        assertThat(result.customerNumber()).isNull();
        assertThat(result.addition()).isNull();
        assertThat(result.street()).isNull();
        assertThat(result.poBox()).isNull();
        assertThat(result.zipCode()).isNull();
        assertThat(result.city()).isNull();
        assertThat(result.website()).isNull();
        assertThat(result.email()).isNull();
        assertThat(result.phoneNumber()).isNull();
        assertThat(result.vatId()).isNull();
        assertThat(result.conditions()).isNull();
        assertThat(result.customerInfo()).isNull();
    }

    /**
     * Tests that the toApiDto() method sets the permId correctly.
     */
    @Test
    void toApiDto_shouldIncludePermIdIfProvided() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.id()).isEqualTo(DUMMY_PERM_ID);
    }

    /**
     * Tests that the toApiDto() method sets the permId to null if the OpenBis sample does not provide one.
     */
    @Test
    void toApiDto_shouldSetNullPermIdIfNotProvided() {

        // Arrange
        OpenBisSample sample = getOpenBisSupplierSampleWithoutPermId();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.id()).isNull();
    }

    /**
     * Tests that the toApiDto() method sets the code correctly.
     */
    @Test
    void toApiDto_shouldProvideSampleCode() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.code()).isEqualTo(DUMMY_SAMPLE_CODE);
    }

    /**
     * Tests that the toApiDto() method includes the provided stats in the result if they are provided.
     */
    @Test
    void toApiDto_shouldIncludeStatsIfProvided() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.stats()).isNotNull();
        assertThat(result.stats()).isEqualTo(getRatingStatsDto());
    }

    /**
     * Tests that the toApiDto() method sets the stats to null if they are not provided.
     */
    @Test
    void toApiDto_shouldSetStatsToNullIfNotProvided() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, null, getFullOrdersList());

        // Assert
        assertThat(result.stats()).isNull();
    }

    /**
     * Tests that the toApiDto() method includes the provided orders list in the result if it is provided.
     */
    @Test
    void toApiDto_shouldIncludeOrdersListIfProvided() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), getFullOrdersList());

        // Assert
        assertThat(result.orders()).isNotNull();
        assertThat(result.orders()).isEqualTo(getFullOrdersList());
    }

    /**
     * Tests that the toApiDto() method sets the orders list to null if it is not provided.
     */
    @Test
    void toApiDto_shouldSetOrdersToNullIfNotProvided() {

        // Arrange
        OpenBisSample sample = getFullOpenBisSupplierSample();

        // Act
        SupplierDetailDto result = supplierMapper.toApiDto(sample, getRatingStatsDto(), null);

        // Assert
        assertThat(result.orders()).isNull();
    }

    // toOpenBisCreation Tests

    /**
     * Tests that the toOpenBisCreation() method returns the correct space ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectSpaceId() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.spaceId().permId()).isEqualTo(DEFAULT_SPACE_CODE);
    }

    /**
     * Tests that the toOpenBisCreation() method returns the correct project ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectProjectId() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.projectId().identifier()).isEqualTo(FULL_PROJECT_CODE);
    }

    /**
     * Tests that the toOpenBisCreation() method returns the correct experiment ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectExperimentId() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.experimentId().identifier()).isEqualTo(FULL_COLLECTION_CODE);
    }

    /**
     * Tests that the toOpenBisCreation() method returns the correct type ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectTypeId() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.typeId().permId()).isEqualTo(SUPPLIER_TYPE_CODE);
    }

    @Test
    void toOpenBisCreation_shouldGeneratePrefixedSupplierCode() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.code()).startsWith(SUPPLIER_TYPE_CODE + "-");
    }

    /**
     * Tests that the toOpenBisCreation() method maps the country label to the corresponding code.
     *
     * This test ensures that the supplier mapper correctly translates the provided vocabulary label into its respective code
     * which is used in the OpenBIS creation process.
     */
    @Test
    void toOpenBisCreation_shouldMapVocabularyLabelsToCodes() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.COUNTRY_SUPPLIER_PROPERTY, VALID_CORRESPONDING_COUNTRY_CODE);
    }

    /**
     * Tests that the toOpenBisCreation() method sets all required properties for supplier creation.
     */
    @Test
    void toOpenBisCreation_shouldSetAllRequiredProperties() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).containsEntry(DUMMY_NAME_PROPERTY, DUMMY_SUPPLIER_NAME);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CUSTOMER_NUMBER_SUPPLIER_PROPERTY, DUMMY_CUSTOMER_NUMBER);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ZIP_CODE_SUPPLIER_PROPERTY, DUMMY_ZIP_CODE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CITY_SUPPLIER_PROPERTY, DUMMY_CITY);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.VAT_ID_SUPPLIER_PROPERTY, DUMMY_VAT_ID);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONDITIONS_SUPPLIER_PROPERTY, DUMMY_CONDITIONS);
    }

    /**
     * Tests that the toOpenBisCreation() method sets all optional properties for supplier creation if they are provided.
     */
    @Test
    void toOpenBisCreation_shouldSetOptionalPropertiesWhenProvided() {

        // Arrange
        SupplierCreationDto dto = getFullSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY, DUMMY_ADDITION);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY, DUMMY_PO_BOX);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY, DUMMY_WEBSITE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY, DUMMY_EMAIL);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY, DUMMY_PHONE_NUMBER);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY, DUMMY_CUSTOMER_INFO);
    }

    /**
     * Tests that the toOpenBisCreation() method does not set any optional properties for supplier creation if they are null.
     */
    @Test
    void toOpenBisCreation_shouldIgnoreNullValues() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY);
    }

    /**
     * Tests that the toOpenBisCreation() method sets parent IDs to null for supplier creation.
     */
    @Test
    void toOpenBisCreation_shouldSetParentIdsToNull() {

        // Arrange
        SupplierCreationDto dto = getMinimalSupplierCreationDto();

        // Act
        SampleCreation result = supplierMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.parentIds()).isNull();
    }

    // toOpenBisUpdate Tests

    /**
     * Tests that the toOpenBisUpdate() method returns the correct perm ID.
     */
    @Test
    void toOpenBisUpdate_shouldProvideCorrectPermId() {

        // Arrange
        SupplierUpdateDto dto = getMinimalSupplierUpdateDto();

        // Act
        SampleUpdate result = supplierMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.sampleId().permId()).isEqualTo(DUMMY_PERM_ID);
    }

    /**
     * Tests that the toOpenBisUpdate() method maps the country label to the corresponding code.
     */
    @Test
    void toOpenBisUpdate_shouldMapVocabularyLabelsToCodes() {

        // Arrange
        SupplierUpdateDto dto = getMinimalSupplierUpdateDto();

        // Act
        SampleUpdate result = supplierMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.COUNTRY_SUPPLIER_PROPERTY, VALID_CORRESPONDING_COUNTRY_CODE);
    }

    /**
     * Tests that the toOpenBisUpdate() method sets properties present in the DTO.
     */
    @Test
    void toOpenBisUpdate_shouldSetPropertiesPresentInDto() {

        // Arrange
        SupplierUpdateDto dto = getMinimalSupplierUpdateDto();

        // Act
        SampleUpdate result = supplierMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.properties()).containsEntry(DUMMY_NAME_PROPERTY, DUMMY_SUPPLIER_NAME);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CUSTOMER_NUMBER_SUPPLIER_PROPERTY, DUMMY_CUSTOMER_NUMBER);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ZIP_CODE_SUPPLIER_PROPERTY, DUMMY_ZIP_CODE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CITY_SUPPLIER_PROPERTY, DUMMY_CITY);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.VAT_ID_SUPPLIER_PROPERTY, DUMMY_VAT_ID);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CONDITIONS_SUPPLIER_PROPERTY, DUMMY_CONDITIONS);
    }

    /**
     * Tests that the toOpenBisUpdate() method does not set properties not present in the DTO.
     */
    @Test
    void toOpenBisUpdate_shouldIgnoreNullValues() {

        // Arrange
        SupplierUpdateDto dto = getMinimalSupplierUpdateDto();

        // Act
        SampleUpdate result = supplierMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY);
    }

    /**
     * Tests that the toOpenBisUpdate() method sets optional fields when provided in the DTO.
     */
    @Test
    void toOpenBisUpdate_shouldSetOptionalFieldsWhenProvided() {

        // Arrange
        SupplierUpdateDto dto = getFullSupplierUpdateDto();

        // Act
        SampleUpdate result = supplierMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY, DUMMY_ADDITION);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY, DUMMY_PO_BOX);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY, DUMMY_WEBSITE);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY, DUMMY_EMAIL);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY, DUMMY_PHONE_NUMBER);
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY, DUMMY_CUSTOMER_INFO);
    }

    /**
     * Tests that the toOpenBisUpdate method maps empty strings in optional fields to empty strings in the properties map.
     * This ensures that fields can be explicitly cleared by sending an empty string. An empty string gets transmitted and
     * signals OpenBIS to clear the field.
     */
    @Test
    void toOpenBisUpdate_shouldMapEmptyStringsToEmptyProperties() {

        // Arrange
        SupplierUpdateDto dto = getClearingSupplierUpdateDto();

        // Act
        SampleUpdate result = supplierMapper.toOpenBisUpdate(DUMMY_PERM_ID, dto);

        // Assert
        // Verify that empty strings are PASSED to the map (not ignored)
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY, "");
        assertThat(result.properties()).containsEntry(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY, "");
    }
}
