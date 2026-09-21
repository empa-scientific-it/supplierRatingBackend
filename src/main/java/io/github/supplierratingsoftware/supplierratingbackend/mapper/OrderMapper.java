package io.github.supplierratingsoftware.supplierratingbackend.mapper;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderUpdateDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.*;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.update.SampleUpdate;
import io.github.supplierratingsoftware.supplierratingbackend.util.OpenBisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Component responsible for mapping technical OpenBIS samples to the domain-specific {@link OrderDetailDto} and vice versa.
 * <p>
 * Handles both directions:
 * <ul>
 *     <li>READ: {@link OpenBisSample} -> {@link OrderDetailDto}</li>
 *     <li>WRITE: {@link OrderCreationDto} -> {@link SampleCreation}</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OpenBisProperties properties;

    /**
     * Converts a generic {@link OpenBisSample} into a {@link OrderDetailDto}.
     * This method attempts to resolve the supplier from the sample's parents.
     * (READ direction)
     *
     * @param sample The raw sample object from OpenBIS.
     * @return The DTO representation.
     */
    public OrderDetailDto toApiDto(OpenBisSample sample) {
        if (sample == null) return null;

        // Try to resolve supplier from parent
        String supplierId = null;
        String supplierName = null;
        if (sample.parents() != null && !sample.parents().isEmpty()) {
            OpenBisSample parent = sample.parents().getFirst();
            if (parent.permId() != null) supplierId = parent.permId().permId();
            if (parent.properties() != null) {
                supplierName = parent.properties().get(properties.nameProperty());
            }
        }

        return toApiDto(sample, supplierId, supplierName);
    }

    /**
     * Converts a generic {@link OpenBisSample} into a {@link OrderDetailDto}
     * using provided supplier ID and name.
     * (READ direction)
     *
     * @param sample The raw sample object from OpenBIS. Can be null.
     * @param supplierId The supplier ID to use in the DTO. Can be null.
     * @param supplierName The supplier name to use in the DTO. Can be null.
     * @return The DTO representation of the order, or null if the sample is null.
     */
    public OrderDetailDto toApiDto(OpenBisSample sample, String supplierId, String supplierName) {
        if (sample == null) return null;

        Map<String, String> props = sample.properties();

        if (props == null) props = Map.of();

        // Mapping Codes (openBIS Code for openBIS V3 API) -> Labels (user-friendly term for REST-API)
        String rawMainCategory = props.get(OpenBisSchemaConstants.MAIN_CATEGORY_ORDER_PROPERTY);
        String mainCategory = OpenBisSchemaConstants.ORDER_MAIN_CATEGORY_MAPPING_CODE_TO_LABEL.getOrDefault(rawMainCategory, rawMainCategory);
        String rawSubCategory = props.get(OpenBisSchemaConstants.SUB_CATEGORY_ORDER_PROPERTY);
        String subCategory = OpenBisSchemaConstants.ORDER_SUB_CATEGORY_MAPPING_CODE_TO_LABEL.getOrDefault(rawSubCategory, rawSubCategory);

        String ratingStatus = OpenBisSchemaConstants.RATING_STATUS_PENDING_ORDER_PROPERTY;
        String ratingId = null;

        if (sample.children() != null && !sample.children().isEmpty()) {
            OpenBisSample rating = sample.children().getFirst();
            if (rating != null) {
                // Business Logic: 1 Order has max. 1 Rating.
                ratingStatus = OpenBisSchemaConstants.RATING_STATUS_RATED_ORDER_PROPERTY;
                ratingId = rating.permId() != null ? rating.permId().permId() : null;
            }
        }

        return new OrderDetailDto(
                props.get(properties.nameProperty()),
                mainCategory,
                subCategory,
                props.get(OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.ORDER_REASON_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.PURCHASER_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.ORDER_DATE_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY),
                props.get(OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY),
                sample.permId() != null ? sample.permId().permId() : null,
                sample.code(),
                ratingStatus,
                supplierId,
                supplierName,
                ratingId
        );
    }

    /**
     * Converts an {@link OrderCreationDto} into an openBIS {@link SampleCreation} object.
     * (WRITE Direction)
     * <p>
     * Generates a unique code: BESTELLUNG-<UUID>
     * Assigns technical identifiers (Space, Project, Experiment).
     * <strong>Crucial:</strong> Links the order to its parent supplier via {@code parentIds}.
     * </p>
     *
     * @param dto The creation request from the API.
     * @return The openBIS creation payload.
     */
    public SampleCreation toOpenBisCreation(OrderCreationDto dto) {
        // Generate unique code: BESTELLUNG-<UUID>
        String code = properties.order().typeCode() + "-" + UUID.randomUUID();

        // Prepare Identifiers
        EntityTypePermId typeId = new EntityTypePermId(properties.order().typeCode());
        SpacePermId spaceId = new SpacePermId(properties.defaultSpace());

        ProjectIdentifier projectId = new ProjectIdentifier(
                OpenBisUtils.buildIdentifier(properties.defaultSpace(), properties.order().projectCode())
        );

        ExperimentIdentifier experimentId = new ExperimentIdentifier(
                OpenBisUtils.buildIdentifier(
                        properties.defaultSpace(),
                        properties.order().projectCode(),
                        properties.order().collectionCode()
                )
        );

        // Mapping Labels (user-friendly term for REST-API) -> Codes (openBIS Code for openBIS V3 API)
        String mainCategoryCode = OpenBisSchemaConstants.ORDER_MAIN_CATEGORY_MAPPING_LABEL_TO_CODE.getOrDefault(dto.mainCategory(), dto.mainCategory());
        String subCategoryCode = OpenBisSchemaConstants.ORDER_SUB_CATEGORY_MAPPING_LABEL_TO_CODE.getOrDefault(dto.subCategory(), dto.subCategory());

        // Map Properties
        Map<String, String> props = new HashMap<>();

        // Mandatory
        props.put(properties.nameProperty(), dto.name());
        props.put(OpenBisSchemaConstants.MAIN_CATEGORY_ORDER_PROPERTY, mainCategoryCode);
        props.put(OpenBisSchemaConstants.SUB_CATEGORY_ORDER_PROPERTY, subCategoryCode);
        props.put(OpenBisSchemaConstants.ORDER_REASON_ORDER_PROPERTY, dto.reason());
        props.put(OpenBisSchemaConstants.PURCHASER_ORDER_PROPERTY, dto.orderedBy());
        props.put(OpenBisSchemaConstants.ORDER_DATE_ORDER_PROPERTY, dto.orderDate());

        // Optional
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY, dto.details());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY, dto.frequency());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY, dto.contactPerson());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY, dto.contactEmail());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY, dto.contactPhone());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY, dto.orderMethod());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY, dto.deliveryDate());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY, dto.orderComment());

        // Link Parent (Supplier)
        List<SamplePermId> parentIds = null;
        parentIds = List.of(new SamplePermId(dto.supplierId()));

        return new SampleCreation(
                spaceId,
                projectId,
                experimentId,
                typeId,
                code,
                props,
                parentIds // <-- Linked Supplier
        );
    }

    /**
     * Converts an {@link OrderUpdateDto} into an openBIS {@link SampleUpdate} object.
     * (WRITE Direction)
     *
     * <p><strong>Important:</strong> This mapping strictly excludes parent relationships (Suppliers).
     * The supplier of an order cannot be changed via update.</p>
     *
     * @param permId The stable identifier (PermID) of the order to update.
     * @param dto    The update data for the order.
     * @return The openBIS update object for the order.
     */
    public SampleUpdate toOpenBisUpdate(String permId, OrderUpdateDto dto) {
        // Identifier
        SamplePermId sampleId = new SamplePermId(permId);

        // Mapping Labels (user-friendly term for REST-API) -> Codes (openBIS Code for openBIS V3 API)
        String mainCategoryCode = OpenBisSchemaConstants.ORDER_MAIN_CATEGORY_MAPPING_LABEL_TO_CODE.getOrDefault(dto.mainCategory(), dto.mainCategory());
        String subCategoryCode = OpenBisSchemaConstants.ORDER_SUB_CATEGORY_MAPPING_LABEL_TO_CODE.getOrDefault(dto.subCategory(), dto.subCategory());

        // Map Properties
        Map<String, String> props = new HashMap<>();

        // Mandatory Fields (Enforced by @NotBlank in DTO)
        props.put(properties.nameProperty(), dto.name());
        props.put(OpenBisSchemaConstants.MAIN_CATEGORY_ORDER_PROPERTY, mainCategoryCode);
        props.put(OpenBisSchemaConstants.SUB_CATEGORY_ORDER_PROPERTY, subCategoryCode);
        props.put(OpenBisSchemaConstants.ORDER_REASON_ORDER_PROPERTY, dto.reason());
        props.put(OpenBisSchemaConstants.PURCHASER_ORDER_PROPERTY, dto.orderedBy());
        props.put(OpenBisSchemaConstants.ORDER_DATE_ORDER_PROPERTY, dto.orderDate());

        // Optional Fields
        // Use putForUpdate to allow clearing values via empty strings
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.DESCRIPTION_ORDER_PROPERTY, dto.details());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.FREQUENCY_ORDER_PROPERTY, dto.frequency());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.CONTACT_NAME_ORDER_PROPERTY, dto.contactPerson());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.CONTACT_EMAIL_ORDER_PROPERTY, dto.contactEmail());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.CONTACT_PHONE_ORDER_PROPERTY, dto.contactPhone());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.ORDER_METHOD_ORDER_PROPERTY, dto.orderMethod());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.DELIVERY_DATE_ORDER_PROPERTY, dto.deliveryDate());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.ORDER_COMMENT_ORDER_PROPERTY, dto.orderComment());

        return new SampleUpdate(sampleId, props);
    }
}
