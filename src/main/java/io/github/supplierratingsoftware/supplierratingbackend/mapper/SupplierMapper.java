package io.github.supplierratingsoftware.supplierratingbackend.mapper;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingStatsDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierUpdateDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.EntityTypePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.ExperimentIdentifier;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.ProjectIdentifier;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.SamplePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.SpacePermId;
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
 * Component responsible for mapping technical OpenBIS samples to the domain-specific {@link SupplierDetailDto} and vice versa.
 * <p>
 * Handles both directions:
 * <ul>
 *     <li>READ: {@link OpenBisSample} -> {@link SupplierDetailDto}</li>
 *     <li>WRITE: {@link SupplierCreationDto} -> {@link SampleCreation}</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class SupplierMapper {

    private final OpenBisProperties properties;

    /**
     * Converts a generic {@link OpenBisSample} into a {@link SupplierDetailDto}.
     * (READ direction)
     *
     * @param sample The raw sample object from OpenBIS. Can be null.
     * @param stats  The rating statistics for the supplier. Can be null.
     * @param orders The orders belonging to the supplier. Can be null for List views.
     * @return The mapped {@link SupplierDetailDto}, or {@code null} if the input sample was null.
     */
    public SupplierDetailDto toApiDto(OpenBisSample sample, RatingStatsDto stats, List<OrderDetailDto> orders) {
        if (sample == null) return null;

        Map<String, String> props = sample.properties(); // Access the Map at the record.

        if (props == null) props = Map.of();

        // Mapping Codes (openBIS Code for openBIS V3 API) -> Labels (user-friendly term for REST-API)
        String rawCountry = props.get(OpenBisSchemaConstants.COUNTRY_SUPPLIER_PROPERTY);
        String country = OpenBisSchemaConstants.SUPPLIER_COUNTRY_MAPPING_CODE_TO_LABEL.getOrDefault(rawCountry, rawCountry);

        return new SupplierDetailDto(
                props.get(properties.nameProperty()),
                props.get(OpenBisSchemaConstants.CUSTOMER_NUMBER_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.STREET_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY),
                country,
                props.get(OpenBisSchemaConstants.ZIP_CODE_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.CITY_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.VAT_ID_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.CONDITIONS_SUPPLIER_PROPERTY),
                props.get(OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY),
                sample.permId() != null ? sample.permId().permId() : null,
                sample.code(),
                stats,
                orders
        );
    }

    /**
     * Converts a {@link SupplierCreationDto} into an openBIS {@link SampleCreation} object.
     * (WRITE Direction)
     * <p>
     * Generates a unique code and assigns technical identifiers (Space, Project, Experiment).
     * </p>
     *
     * @param dto The creation request from the API.
     * @return The openBIS creation payload.
     */
    public SampleCreation toOpenBisCreation(SupplierCreationDto dto) {
        // Generate unique code: LIEFERANT-<UUID>
        String code = properties.supplier().typeCode() + "-" + UUID.randomUUID();

        // Prepare Identifiers based on configuration
        EntityTypePermId typeId = new EntityTypePermId(properties.supplier().typeCode());
        SpacePermId spaceId = new SpacePermId(properties.defaultSpace());

        // Project Identifier
        ProjectIdentifier projectId = new ProjectIdentifier(
                OpenBisUtils.buildIdentifier(properties.defaultSpace(), properties.supplier().projectCode())
        );

        // Experiment Identifier
        ExperimentIdentifier experimentId = new ExperimentIdentifier(
                OpenBisUtils.buildIdentifier(
                        properties.defaultSpace(),
                        properties.supplier().projectCode(),
                        properties.supplier().collectionCode()
                )
        );

        // Mapping Labels (user-friendly term for REST-API) -> Codes (openBIS Code for openBIS V3 API)
        String countryCode = OpenBisSchemaConstants.SUPPLIER_COUNTRY_MAPPING_LABEL_TO_CODE.getOrDefault(dto.country(), dto.country());

        // Map Properties
        Map<String, String> props = new HashMap<>();

        // Mandatory fields
        props.put(properties.nameProperty(), dto.name());
        props.put(OpenBisSchemaConstants.CUSTOMER_NUMBER_SUPPLIER_PROPERTY, dto.customerNumber());
        props.put(OpenBisSchemaConstants.STREET_SUPPLIER_PROPERTY, dto.street());
        props.put(OpenBisSchemaConstants.COUNTRY_SUPPLIER_PROPERTY, countryCode);
        props.put(OpenBisSchemaConstants.ZIP_CODE_SUPPLIER_PROPERTY, dto.zipCode());
        props.put(OpenBisSchemaConstants.CITY_SUPPLIER_PROPERTY, dto.city());
        props.put(OpenBisSchemaConstants.VAT_ID_SUPPLIER_PROPERTY, dto.vatId());
        props.put(OpenBisSchemaConstants.CONDITIONS_SUPPLIER_PROPERTY, dto.conditions());

        // Optional fields
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY, dto.addition());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY, dto.poBox());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY, dto.website());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY, dto.email());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY, dto.phoneNumber());
        OpenBisUtils.putIfNotNull(props, OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY, dto.customerInfo());

        return new SampleCreation(
                spaceId,
                projectId,
                experimentId,
                typeId,
                code,
                props,
                null // No parent samples needed for suppliers
        );
    }

    /**
     * Converts a SupplierUpdateDto into an openBIS SampleUpdate object.
     * (WRITE Direction)
     *
     * @param permId The stable identifier (PermID) of the supplier to update.
     * @param dto    The update data containing mutable master data.
     * @return The openBIS update payload.
     */
    public SampleUpdate toOpenBisUpdate(String permId, SupplierUpdateDto dto) {
        // Identifier
        SamplePermId sampleId = new SamplePermId(permId);

        // Mapping Labels (user-friendly term for REST-API) -> Codes (openBIS Code for openBIS V3 API)
        String countryCode = OpenBisSchemaConstants.SUPPLIER_COUNTRY_MAPPING_LABEL_TO_CODE.getOrDefault(dto.country(), dto.country());

        // Map Properties
        Map<String, String> props = new HashMap<>();

        // Mandatory Fields (enforced by @NotBlank in DTO)
        props.put(properties.nameProperty(), dto.name());
        props.put(OpenBisSchemaConstants.CUSTOMER_NUMBER_SUPPLIER_PROPERTY, dto.customerNumber());
        props.put(OpenBisSchemaConstants.STREET_SUPPLIER_PROPERTY, dto.street());
        props.put(OpenBisSchemaConstants.COUNTRY_SUPPLIER_PROPERTY, countryCode);
        props.put(OpenBisSchemaConstants.ZIP_CODE_SUPPLIER_PROPERTY, dto.zipCode());
        props.put(OpenBisSchemaConstants.CITY_SUPPLIER_PROPERTY, dto.city());
        props.put(OpenBisSchemaConstants.VAT_ID_SUPPLIER_PROPERTY, dto.vatId());
        props.put(OpenBisSchemaConstants.CONDITIONS_SUPPLIER_PROPERTY, dto.conditions());

        // Optional Fields
        // Use putForUpdate to allow clearing values via empty strings
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.ADDITION_SUPPLIER_PROPERTY, dto.addition());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.PO_BOX_SUPPLIER_PROPERTY, dto.poBox());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.WEBSITE_SUPPLIER_PROPERTY, dto.website());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.EMAIL_SUPPLIER_PROPERTY, dto.email());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.PHONE_NUMBER_SUPPLIER_PROPERTY, dto.phoneNumber());
        OpenBisUtils.putForUpdate(props, OpenBisSchemaConstants.CUSTOMER_INFO_SUPPLIER_PROPERTY, dto.customerInfo());

        return new SampleUpdate(sampleId, props);
    }
}
