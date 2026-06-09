package io.github.supplierratingsoftware.supplierratingbackend.mapper;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.OpenBisEntityType;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.OpenBisPermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RatingMapperTest {

    private RatingMapper ratingMapper;

    // OpenBis DTO values (generic)
    private static final String DUMMY_SAMPLE_CODE = "Test Sample Code";

    // OpenBis DTO values (Rating)
    private static final String DUMMY_QUALITY_RATING_SCORE_FLOAT_STRING = "1.0";
    private static final Integer DUMMY_QUALITY_RATING_SCORE_INT = 1;
    private static final String DUMMY_QUALITY_RATING_SCORE_INTEGER_STRING = "1";
    private static final String DUMMY_QUALITY_RATING_SCORE_NOT_A_NUMBER_STRING = "This is not a number.";
    private static final String DUMMY_QUALITY_RATING_REASON = "Test Quality Reason";
    private static final String DUMMY_COST_RATING_SCORE_FLOAT_STRING = "2.0";
    private static final Integer DUMMY_COST_RATING_SCORE_INT = 2;
    private static final String DUMMY_COST_RATING_SCORE_INTEGER_STRING = "2";
    private static final String DUMMY_COST_RATING_SCORE_NOT_A_NUMBER_STRING = "This is not a number.";
    private static final String DUMMY_COST_RATING_REASON = "Test Cost Reason";
    private static final String DUMMY_RELIABILITY_RATING_SCORE_FLOAT_STRING = "4.0";
    private static final Integer DUMMY_RELIABILITY_RATING_SCORE_INT = 4;
    private static final String DUMMY_RELIABILITY_RATING_SCORE_INTEGER_STRING = "4";
    private static final String DUMMY_RELIABILITY_RATING_SCORE_NOT_A_NUMBER_STRING = "This is not a number.";
    private static final String DUMMY_RELIABILITY_RATING_REASON = "Test Reliability Reason";
    private static final String DUMMY_AVAILABILITY_RATING_SCORE_FLOAT_STRING = "5.0";
    private static final Integer DUMMY_AVAILABILITY_RATING_SCORE_INT = 5;
    private static final String DUMMY_AVAILABILITY_RATING_SCORE_INTEGER_STRING = "5";
    private static final String DUMMY_AVAILABILITY_RATING_SCORE_NOT_A_NUMBER_STRING = "This is not a number.";
    private static final String DUMMY_AVAILABILITY_RATING_REASON = "Test Availability Reason";
    private static final String DUMMY_TOTAL_SCORE_OPTIONAL_RATING_STRING = "3.0";
    private static final String DUMMY_TOTAL_SCORE_MANDATORY_RATING_STRING = "2.33";
    private static final String DUMMY_RATING_COMMENT = "Test Rating Comment";
    private static final String DUMMY_RATING_ID = "BEWERTUNG-12345";

    // OpenBis DTO values (Order)
    private static final String DUMMY_ORDER_ID = "BESTELLUNG-12345";

    // OpenBis DTO values (Supplier)
    private static final String DUMMY_SUPPLIER_NAME = "Test Supplier Name";
    private static final String DUMMY_SUPPLIER_ID = "LIEFERANT-12345";

    // OpenBIS Rating Entity Codes
    private static final String RATING_PROJECT_CODE = "BEWERTUNGEN";
    private static final String RATING_TYPE_CODE = "BESTELLBEWERTUNG";
    private static final String RATING_COLLECTION_CODE = "BEWERTUNGEN";

    // OpenBIS Order Entity Codes
    private static final String ORDER_TYPE_CODE = "BESTELLUNG";

    // OpenBIS Supplier Entity Codes
    private static final String SUPPLIER_TYPE_CODE = "LIEFERANT";

    // OpenBIS Properties
    private static final String DUMMY_NAME_PROPERTY = "$NAME";
    private static final String DUMMY_API_URL = "dummyApiUrl";
    private static final String DEFAULT_SPACE_CODE = "LIEFERANTENBEWERTUNG";
    private static final String FULL_PROJECT_CODE = "/" + DEFAULT_SPACE_CODE + "/" + RATING_PROJECT_CODE;
    private static final String FULL_COLLECTION_CODE = FULL_PROJECT_CODE + "/" + RATING_COLLECTION_CODE;

    /**
     * Helper method to create a sample rating properties map for OpenBIS.
     *
     * @return A map containing sample rating properties for OpenBIS.
     */
    private Map<String, String> getOpenBisSampleRatingProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OpenBisSchemaConstants.QUALITY_RATING_PROPERTY, DUMMY_QUALITY_RATING_SCORE_FLOAT_STRING);
        properties.put(OpenBisSchemaConstants.QUALITY_REASON_RATING_PROPERTY, DUMMY_QUALITY_RATING_REASON);
        properties.put(OpenBisSchemaConstants.COST_RATING_PROPERTY, DUMMY_COST_RATING_SCORE_FLOAT_STRING);
        properties.put(OpenBisSchemaConstants.COST_REASON_RATING_PROPERTY, DUMMY_COST_RATING_REASON);
        properties.put(OpenBisSchemaConstants.RELIABILITY_RATING_PROPERTY, DUMMY_RELIABILITY_RATING_SCORE_FLOAT_STRING);
        properties.put(OpenBisSchemaConstants.RELIABILITY_REASON_RATING_PROPERTY, DUMMY_RELIABILITY_RATING_REASON);
        properties.put(OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY, DUMMY_AVAILABILITY_RATING_SCORE_FLOAT_STRING);
        properties.put(OpenBisSchemaConstants.AVAILABILITY_REASON_RATING_PROPERTY, DUMMY_AVAILABILITY_RATING_REASON);
        properties.put(OpenBisSchemaConstants.TOTAL_SCORE_RATING_PROPERTY, DUMMY_TOTAL_SCORE_OPTIONAL_RATING_STRING);
        properties.put(OpenBisSchemaConstants.RATING_COMMENT_RATING_PROPERTY, DUMMY_RATING_COMMENT);
        return properties;
    }

    /**
     * Helper method to create a sample rating properties map for OpenBIS with null values.
     *
     * @return A map containing sample rating properties for OpenBIS with null values.
     */
    private Map<String, String> getOpenBisSampleRatingPropertiesWithNullValues() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OpenBisSchemaConstants.QUALITY_RATING_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.COST_RATING_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.RELIABILITY_RATING_PROPERTY, null);
        properties.put(OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY, null);
        return properties;
    }

    /**
     * Helper method to create a sample rating properties map for OpenBIS with not a number values.
     *
     * @return A map containing sample rating properties for OpenBIS with not a number values.
     */
    private Map<String, String> getOpenBisSampleRatingPropertiesWithNotANumberValues() {
        Map<String, String> properties = new HashMap<>();
        properties.put(OpenBisSchemaConstants.QUALITY_RATING_PROPERTY, DUMMY_QUALITY_RATING_SCORE_NOT_A_NUMBER_STRING);
        properties.put(OpenBisSchemaConstants.COST_RATING_PROPERTY, DUMMY_COST_RATING_SCORE_NOT_A_NUMBER_STRING);
        properties.put(OpenBisSchemaConstants.RELIABILITY_RATING_PROPERTY, DUMMY_RELIABILITY_RATING_SCORE_NOT_A_NUMBER_STRING);
        properties.put(OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY, DUMMY_AVAILABILITY_RATING_SCORE_NOT_A_NUMBER_STRING);
        return properties;
    }

    /**
     * Helper method to create a sample supplier properties map for OpenBIS.
     *
     * @return A map containing sample supplier properties for OpenBIS.
     */
    private Map<String, String> getOpenBisSampleSupplierProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(DUMMY_NAME_PROPERTY, DUMMY_SUPPLIER_NAME);
        return properties;
    }

    /**
     * Helper method to create an OpenBIS Supplier sample.
     *
     * @return An OpenBIS sample.
     */
    private OpenBisSample getOpenBisSupplierSample() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_SUPPLIER_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleSupplierProperties();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBIS Supplier sample with null permId.
     *
     * @return An OpenBIS sample with null permId.
     */
    private OpenBisSample getOpenBisSupplierSampleWithNullPermId() {
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleSupplierProperties();
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBIS Supplier sample without properties.
     *
     * @return An OpenBIS Supplier sample without properties.
     */
    private OpenBisSample getOpenBisSupplierSampleWithoutProperties() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_SUPPLIER_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(SUPPLIER_TYPE_CODE);
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, null, null, null);
    }

    /**
     * Helper method to create an OpenBIS Order sample.
     *
     * @return An OpenBIS Order sample.
     */
    private OpenBisSample getOpenBisOrderSample() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_ORDER_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        List<OpenBisSample> parents = List.of(getOpenBisSupplierSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, null, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Order sample with null permId.
     *
     * @return An OpenBIS Order sample with null permId.
     */
    private OpenBisSample getOpenBisOrderSampleWithNullPermId() {
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        List<OpenBisSample> parents = List.of(getOpenBisSupplierSample());
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, null, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Order sample without parents.
     *
     * @return An OpenBIS Order sample without parents.
     */
    private OpenBisSample getOpenBisOrderSampleWithoutParents() {
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, null, null, null);
    }

    /**
     * Helper method to create an OpenBIS Order sample with empty parents.
     *
     * @return An OpenBIS Order sample with empty parents.
     */
    private OpenBisSample getOpenBisOrderSampleWithEmptyParents() {
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        List<OpenBisSample> parents = List.of();
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, null, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Order sample with null parent supplier.
     *
     * @return An OpenBIS Order sample with a null parent supplier.
     */
    private OpenBisSample getOpenBisOrderSampleWithNullParentSupplier() {
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        List<OpenBisSample> parents = Collections.singletonList(null);
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, null, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Order sample with parent supplier with null permId.
     *
     * @return An OpenBIS Order sample with a parent supplier having null permId.
     */
    private OpenBisSample getOpenBisOrderSampleWithParentSupplierWithNullPermId() {
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        List<OpenBisSample> parents = List.of(getOpenBisSupplierSampleWithNullPermId());
        return new OpenBisSample(null, entityType, DUMMY_SAMPLE_CODE, null, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Order sample with a parent supplier without properties.
     *
     * @return An OpenBIS Order sample with a parent supplier without properties.
     */
    private OpenBisSample getOpenBisOrderSampleWithParentSupplierWithoutProperties() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_ORDER_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(ORDER_TYPE_CODE);
        List<OpenBisSample> parents = List.of(getOpenBisSupplierSampleWithoutProperties());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, null, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample.
     *
     * @return An OpenBIS Rating sample.
     */
    private OpenBisSample getOpenBisRatingSample() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with null score values.
     *
     * @return An OpenBIS Rating sample with null score values.
     */
    private OpenBisSample getOpenBisRatingSampleWithNullScoreValues() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingPropertiesWithNullValues();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with null properties.
     *
     * @return An OpenBIS Rating sample with null properties.
     */
    private OpenBisSample getOpenBisRatingSampleWithNullProperties() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        List<OpenBisSample> parents = List.of(getOpenBisOrderSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, null, parents, null);
    }

    private OpenBisSample getOpenBisRatingSampleWithNotANumberScoreValues() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingPropertiesWithNotANumberValues();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSample());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample without parents.
     *
     * @return An OpenBIS Rating sample without parents.
     */
    private OpenBisSample getOpenBisRatingSampleWithoutParents() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with empty parents.
     *
     * @return An OpenBIS Rating sample with empty parents.
     */
    private OpenBisSample getOpenBisRatingSampleWithEmptyParents() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of();
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with null parent order.
     *
     * @return An OpenBIS Rating sample with a null parent order.
     */
    private OpenBisSample getOpenBisRatingSampleWithNullParentOrder() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = Collections.singletonList(null);
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, null, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with parent order with null permId.
     *
     * @return An OpenBIS Rating sample with a parent order having null permId.
     */
    private OpenBisSample getOpenBisRatingSampleWithParentOrderWithNullPermId() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSampleWithNullPermId());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample without grandparents.
     *
     * @return An OpenBIS Rating sample without grandparents.
     */
    private OpenBisSample getOpenBisRatingSampleWithoutGrandparents() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSampleWithoutParents());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with empty grandparents.
     *
     * @return An OpenBIS Rating sample with empty grandparents.
     */
    private OpenBisSample getOpenBisRatingSampleWithEmptyGrandparents() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSampleWithEmptyParents());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with null grandparent supplier.
     *
     * @return An OpenBIS Rating sample with a null grandparent supplier.
     */
    private OpenBisSample getOpenBisRatingSampleWithNullGrandparentSupplier() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSampleWithNullParentSupplier());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with grandparent supplier with null permId.
     *
     * @return An OpenBIS Rating sample with a grandparent supplier having null permId.
     */
    private OpenBisSample getOpenBisRatingSampleWithGrandparentSupplierWithNullPermId() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSampleWithParentSupplierWithNullPermId());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create an OpenBIS Rating sample with a grandparent supplier without properties.
     *
     * @return An OpenBIS Rating sample with a grandparent supplier without properties.
     */
    private OpenBisSample getOpenBisRatingSampleWithGrandparentSupplierWithoutProperties() {
        OpenBisPermId permId = new OpenBisPermId(DUMMY_RATING_ID);
        OpenBisEntityType entityType = new OpenBisEntityType(RATING_TYPE_CODE);
        Map<String, String> properties = getOpenBisSampleRatingProperties();
        List<OpenBisSample> parents = List.of(getOpenBisOrderSampleWithParentSupplierWithoutProperties());
        return new OpenBisSample(permId, entityType, DUMMY_SAMPLE_CODE, properties, parents, null);
    }

    /**
     * Helper method to create a rating creation DTO with all fields set.
     *
     * @return A rating creation DTO with all fields set.
     */
    RatingCreationDto getFullRatingCreationDto() {
        return new RatingCreationDto(
                DUMMY_ORDER_ID,
                DUMMY_QUALITY_RATING_SCORE_INT,
                DUMMY_QUALITY_RATING_REASON,
                DUMMY_COST_RATING_SCORE_INT,
                DUMMY_COST_RATING_REASON,
                DUMMY_RELIABILITY_RATING_SCORE_INT,
                DUMMY_RELIABILITY_RATING_REASON,
                DUMMY_AVAILABILITY_RATING_SCORE_INT,
                DUMMY_AVAILABILITY_RATING_REASON,
                DUMMY_RATING_COMMENT
        );
    }

    /**
     * Helper method to create a minimal rating creation DTO with only required fields.
     *
     * @return A minimal rating creation DTO with only required fields.
     */
    RatingCreationDto getMinimalRatingCreationDto() {
        return new RatingCreationDto(
                DUMMY_ORDER_ID,
                DUMMY_QUALITY_RATING_SCORE_INT,
                DUMMY_QUALITY_RATING_REASON,
                DUMMY_COST_RATING_SCORE_INT,
                DUMMY_COST_RATING_REASON,
                DUMMY_RELIABILITY_RATING_SCORE_INT,
                DUMMY_RELIABILITY_RATING_REASON,
                null,
                null,
                null
        );
    }

    /**
     * Setup method to set up the test environment with predefined OpenBIS properties.
     */
    @BeforeEach
    void setUp() {

        OpenBisProperties.EntityConfig ratingConfig = new OpenBisProperties.EntityConfig(
                RATING_PROJECT_CODE,
                RATING_TYPE_CODE,
                RATING_COLLECTION_CODE
        );

        OpenBisProperties properties = new OpenBisProperties(
                DUMMY_API_URL,
                DEFAULT_SPACE_CODE,
                null,
                null,
                ratingConfig,
                DUMMY_NAME_PROPERTY
        );

        ratingMapper = new RatingMapper(properties);
    }

    /**
     * Tests that toApiDto returns null if the OpenBis sample is null.
     */
    @Test
    void toApiDto_shouldReturnNullIfSampleIsNull() {

        // Arrange
        OpenBisSample sample = null;

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result).isNull();
    }

    /**
     * Tests that toApiDto returns null score if the property is null.
     */
    @Test
    void toApiDto_shouldReturnNullScoreIfPropertyIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithNullScoreValues();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.quality()).isNull();
        assertThat(result.cost()).isNull();
        assertThat(result.reliability()).isNull();
        assertThat(result.availability()).isNull();
    }

    /**
     * Tests that toApiDto returns null score if the property is blank.
     */
    @Test
    void toApiDto_shouldReturnNullScoreIfPropertyIsBlank() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithNullProperties();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.quality()).isNull();
        assertThat(result.cost()).isNull();
        assertThat(result.reliability()).isNull();
        assertThat(result.availability()).isNull();
    }

    /**
     * Tests that toApiDto maps all rating score properties to Integer values.
     */
    @Test
    void toApiDto_shouldMapValidNumericStringPropertyToIntegerScore() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSample();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.quality()).isEqualTo(DUMMY_QUALITY_RATING_SCORE_INT);
        assertThat(result.cost()).isEqualTo(DUMMY_COST_RATING_SCORE_INT);
        assertThat(result.reliability()).isEqualTo(DUMMY_RELIABILITY_RATING_SCORE_INT);
        assertThat(result.availability()).isEqualTo(DUMMY_AVAILABILITY_RATING_SCORE_INT);
    }

    /**
     * Tests that toApiDto returns null score if the property cannot be cast to an Integer.
     */
    @Test
    void toApiDto_shouldReturnNullScoreIfPropertyIsNotANumber() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithNotANumberScoreValues();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.quality()).isNull();
        assertThat(result.cost()).isNull();
        assertThat(result.reliability()).isNull();
        assertThat(result.availability()).isNull();
    }

    /**
     * Tests that toApiDto maps all reasons directly.
     */
    @Test
    void toApiDto_shouldMapReasonPropertiesDirectly() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSample();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.qualityReason()).isEqualTo(DUMMY_QUALITY_RATING_REASON);
        assertThat(result.costReason()).isEqualTo(DUMMY_COST_RATING_REASON);
        assertThat(result.reliabilityReason()).isEqualTo(DUMMY_RELIABILITY_RATING_REASON);
        assertThat(result.availabilityReason()).isEqualTo(DUMMY_AVAILABILITY_RATING_REASON);
    }

    /**
     * Tests that toApiDto maps the permId correctly.
     */
    @Test
    void toApiDto_shouldMapIdentifiersIfPresent() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSample();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.id()).isEqualTo(DUMMY_RATING_ID);
        assertThat(result.code()).isEqualTo(DUMMY_SAMPLE_CODE);
    }

    /**
     * Tests that toApiDto returns null orderId if the parents list is null.
     */
    @Test
    void toApiDto_shouldReturnNullOrderIdIfParentsListIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithoutParents();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.orderId()).isNull();
    }

    /**
     * Tests that toApiDto returns null orderId if the parents list is empty.
     */
    @Test
    void toApiDto_shouldReturnNullOrderIdIfParentsListIsEmpty() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithEmptyParents();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.orderId()).isNull();
    }

    /**
     * Tests that toApiDto returns null orderId if the first parent is null.
     */
    @Test
    void toApiDto_shouldReturnNullOrderIdIfFirstParentIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithNullParentOrder();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.orderId()).isNull();
    }

    /**
     * Tests that toApiDto returns null orderId if the first parent permId is null.
     */
    @Test
    void toApiDto_shouldReturnNullOrderIdIfParentPermIdIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithParentOrderWithNullPermId();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.orderId()).isNull();
    }

    /**
     * Tests that toApiDto maps the order permId correctly.
     */
    @Test
    void toApiDto_shouldMapOrderIdFromFirstParent() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSample();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.orderId()).isEqualTo(DUMMY_ORDER_ID);
    }

    /**
     * Tests that toApiDto returns null supplier info if the parents list is null.
     * (Id is null and name is null)
     */
    @Test
    void toApiDto_shouldReturnNullSupplierInfoIfRatingParentsIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithoutParents();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that toApiDto returns null supplier info if the parents list is empty.
     * (Id is null and name is null)
     */
    @Test
    void toApiDto_shouldReturnNullSupplierInfoIfRatingParentsIsEmpty() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithEmptyParents();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that toApiDto returns null supplier info if order sample is null.
     * (Id is null and name is null)
     */
    @Test
    void toApiDto_shouldReturnNullSupplierInfoIfOrderSampleIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithNullParentOrder();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that toApiDto returns null supplier info if order parents list is null.
     * (Id is null and name is null)
     */
    @Test
    void toApiDto_shouldReturnNullSupplierInfoIfOrderParentsIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithoutGrandparents();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that toApiDto returns null supplier info if order parents list is empty.
     * (Id is null and name is null)
     */
    @Test
    void toApiDto_shouldReturnNullSupplierInfoIfOrderParentsIsEmpty() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithEmptyGrandparents();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that toApiDto returns null supplier info if grandparent supplier sample is null.
     * (Id is null and name is null)
     */
    @Test
    void toApiDto_shouldReturnNullSupplierInfoIfSupplierSampleIsNull() {

       // Arrange
       OpenBisSample sample = getOpenBisRatingSampleWithNullGrandparentSupplier();

       // Act
       RatingDetailDto result = ratingMapper.toApiDto(sample);

       // Assert
       assertThat(result.supplierId()).isNull();
       assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that toApiDto returns null supplier id if grandparent permId is null.
     */
    @Test
    void toApiDto_shouldReturnNullSupplierIdIfSupplierPermIdIsNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithGrandparentSupplierWithNullPermId();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isNull();
    }

    /**
     * Tests that toApiDto maps the supplier id from the grandparent's permId.
     */
    @Test
    void toApiDto_shouldMapSupplierIdFromGrandparent() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSample();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierId()).isEqualTo(DUMMY_SUPPLIER_ID);
    }

    /**
     * Tests that toApiDto sets supplierName to null if grandparent supplier properties are null.
     */
    @Test
    void toApiDto_shouldReturnNullSupplierNameIfSupplierPropertiesAreNull() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSampleWithGrandparentSupplierWithoutProperties();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierName()).isNull();
    }

    /**
     * Tests that toApiDto maps the supplier name from the grandparent's properties.
     */
    @Test
    void toApiDto_shouldMapSupplierNameFromGrandparent() {

        // Arrange
        OpenBisSample sample = getOpenBisRatingSample();

        // Act
        RatingDetailDto result = ratingMapper.toApiDto(sample);

        // Assert
        assertThat(result.supplierName()).isEqualTo(DUMMY_SUPPLIER_NAME);
    }

    /**
     * Tests that toOpenBisCreation provides the correct space ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectSpaceId() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.spaceId().permId()).isEqualTo(DEFAULT_SPACE_CODE);
    }

    /**
     * Tests that toOpenBisCreation provides the correct project ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectProjectId() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.projectId().identifier()).isEqualTo(FULL_PROJECT_CODE);
    }

    /**
     * Tests that toOpenBisCreation provides the correct experiment ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectExperimentId() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.experimentId().identifier()).isEqualTo(FULL_COLLECTION_CODE);
    }

    /**
     * Tests that toOpenBisCreation provides the correct type ID.
     */
    @Test
    void toOpenBisCreation_shouldProvideCorrectTypeId() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.typeId().permId()).isEqualTo(RATING_TYPE_CODE);
    }

    /**
     * Tests that toOpenBisCreation generates a rating code with the correct prefix.
     */
    @Test
    void toOpenBisCreation_shouldGeneratePrefixedRatingCode() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.code()).startsWith(RATING_TYPE_CODE + "-");
    }

    /**
     * Tests that toOpenBisCreation maps integer scores from RatingCreationDto to string properties in SampleCreation.
     */
    @Test
    void toOpenBisCreation_shouldMapIntegerScoresToStringProperties() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties().get(OpenBisSchemaConstants.QUALITY_RATING_PROPERTY)).isEqualTo(DUMMY_QUALITY_RATING_SCORE_INTEGER_STRING);
        assertThat(result.properties().get(OpenBisSchemaConstants.COST_RATING_PROPERTY)).isEqualTo(DUMMY_COST_RATING_SCORE_INTEGER_STRING);
        assertThat(result.properties().get(OpenBisSchemaConstants.RELIABILITY_RATING_PROPERTY)).isEqualTo(DUMMY_RELIABILITY_RATING_SCORE_INTEGER_STRING);
        assertThat(result.properties().get(OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY)).isEqualTo(DUMMY_AVAILABILITY_RATING_SCORE_INTEGER_STRING);
    }

    /**
     * Tests that toOpenBisCreation maps reasons from RatingCreationDto to the corresponding properties in SampleCreation.
     */
    @Test
    void toOpenBisCreation_shouldMapReasonsToProperty() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties().get(OpenBisSchemaConstants.QUALITY_REASON_RATING_PROPERTY)).isEqualTo(DUMMY_QUALITY_RATING_REASON);
        assertThat(result.properties().get(OpenBisSchemaConstants.COST_REASON_RATING_PROPERTY)).isEqualTo(DUMMY_COST_RATING_REASON);
        assertThat(result.properties().get(OpenBisSchemaConstants.RELIABILITY_REASON_RATING_PROPERTY)).isEqualTo(DUMMY_RELIABILITY_RATING_REASON);
        assertThat(result.properties().get(OpenBisSchemaConstants.AVAILABILITY_REASON_RATING_PROPERTY)).isEqualTo(DUMMY_AVAILABILITY_RATING_REASON);
    }

    /**
     * Tests that toOpenBisCreation ignores null optional properties.
     */
    @Test
    void toOpenBisCreation_shouldIgnoreNullOptionalProperties() {

        // Arrange
        RatingCreationDto dto = getMinimalRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.AVAILABILITY_REASON_RATING_PROPERTY);
        assertThat(result.properties()).doesNotContainKey(OpenBisSchemaConstants.RATING_COMMENT_RATING_PROPERTY);
    }

    /**
     * Tests that toOpenBisCreation calculates the total score from mandatory fields and rounds to two decimal places.
     */
    @Test
    void toOpenBisCreation_shouldCalculateTotalScoreFromMandatoryFieldsAndRoundToTwoDecimalPlaces() {

        // Arrange
        RatingCreationDto dto = getMinimalRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties().get(OpenBisSchemaConstants.TOTAL_SCORE_RATING_PROPERTY)).isEqualTo(DUMMY_TOTAL_SCORE_MANDATORY_RATING_STRING);
    }

    /**
     * Tests that toOpenBisCreation calculates the total score including availability.
     */
    @Test
    void toOpenBisCreation_shouldCalculateTotalScoreIncludingAvailability() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.properties().get(OpenBisSchemaConstants.TOTAL_SCORE_RATING_PROPERTY)).isEqualTo(DUMMY_TOTAL_SCORE_OPTIONAL_RATING_STRING);
    }

    /**
     * Tests that toOpenBisCreation maps the orderId from RatingCreationDto to the parentIds list of SampleCreation.
     */
    @Test
    void toOpenBisCreation_shouldMapOrderIdToParentIdsList() {

        // Arrange
        RatingCreationDto dto = getFullRatingCreationDto();

        // Act
        SampleCreation result = ratingMapper.toOpenBisCreation(dto);

        // Assert
        assertThat(result.parentIds().getFirst().permId()).isEqualTo(DUMMY_ORDER_ID);
    }
}
