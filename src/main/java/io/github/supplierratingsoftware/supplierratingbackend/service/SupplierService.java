package io.github.supplierratingsoftware.supplierratingbackend.service;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.RatingStatsDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.SupplierUpdateDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.creation.SampleCreation;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.fetchoptions.PropertyFetchOptions;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.fetchoptions.SampleFetchOptions;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.fetchoptions.SampleTypeFetchOptions;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.id.SamplePermId;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.result.OpenBisSample;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.*;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.update.SampleUpdate;
import io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisIntegrationException;
import io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisResourceNotFoundException;
import io.github.supplierratingsoftware.supplierratingbackend.integration.openbis.OpenBisClient;
import io.github.supplierratingsoftware.supplierratingbackend.mapper.OrderMapper;
import io.github.supplierratingsoftware.supplierratingbackend.mapper.SupplierMapper;
import io.github.supplierratingsoftware.supplierratingbackend.util.OpenBisUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service responsible for managing supplier-related business logic.
 * <p>
 * This service communicates with the {@link OpenBisClient} to retrieve raw data
 * and uses the {@link SupplierMapper} to transform it into API-compliant DTOs.
 * It also aggregates statistics (ratings) for suppliers.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

    private final OpenBisClient openBisClient;
    private final SupplierMapper supplierMapper;
    private final OpenBisProperties properties;
    private final OrderMapper orderMapper;

    /**
     * Retrieves all suppliers from OpenBIS.
     * <p>
     * Executes a server-side filtered search for samples located in the configured
     * space, project, and matching the configured sample type.
     * It fetches only the necessary properties and types for the suppliers,
     * including their child orders and ratings to calculate aggregates.
     * </p>
     *
     * @return A list of {@link SupplierDetailDto} objects representing all available suppliers.
     */
    public List<SupplierDetailDto> getAllSuppliers() {
        // 1. Define Hierarchy Fetch Options (Recursive Fetching)

        // Level 2: Ratings (Children of Orders)
        SampleFetchOptions ratingFetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                null
        );

        // Level 1: Orders (Children of Suppliers) -> fetch their children (Ratings)
        SampleFetchOptions orderFetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                ratingFetchOptions
        );

        // Level 0: Suppliers -> fetch their children (Orders)
        SampleFetchOptions supplierFetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                orderFetchOptions
        );

        // 2. Define Search Criteria
        SampleSearchCriteria criteria = SampleSearchCriteria.create()
                .with(SpaceSearchCriteria.withCode(properties.defaultSpace()))
                .with(ProjectSearchCriteria.withCode(properties.supplier().projectCode()))
                .with(SampleTypeSearchCriteria.withCode(properties.supplier().typeCode()));

        // 3. Execute Search
        List<OpenBisSample> rawSamples = openBisClient.searchSamples(criteria, supplierFetchOptions);

        // 4. Map & Aggregate
        return rawSamples.stream()
                .map(supplier -> {
                    RatingStatsDto stats = calculateStats(supplier);
                    return supplierMapper.toApiDto(supplier, stats, null);
                })
                .toList();
    }

    /**
     * Retrieves a single supplier by its PermID from OpenBIS.
     * This includes the full list of orders associated with the supplier.
     *
     * @param permId The PermID of the supplier to retrieve.
     * @return The supplier details including orders.
     * @throws OpenBisResourceNotFoundException If the supplier with the given PermID is not found.
     */
    public SupplierDetailDto getSupplierById(String permId) {
        // Criteria: Search by PermID
        SampleSearchCriteria criteria = SampleSearchCriteria.create()
                .with(PermIdSearchCriteria.withId(permId))
                .with(SampleTypeSearchCriteria.withCode(properties.supplier().typeCode()));

        // Deep fetch options for Detail View:
        // - Grandchildren (Ratings): Needed for Stats calculation and rating status
        SampleFetchOptions ratingFetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                null
        );

        // - Children (Orders): Needed for Order List
        SampleFetchOptions orderFetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                ratingFetchOptions
        );

        // - The supplier itself
        SampleFetchOptions fetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                orderFetchOptions
        );

        List<OpenBisSample> results = openBisClient.searchSamples(criteria, fetchOptions);

        if (results.isEmpty()) {
            throw new OpenBisResourceNotFoundException("Supplier with PermID '" + permId + "' not found in OpenBIS");
        }

        OpenBisSample supplierSample = results.getFirst();

        // Extract Supplier Info for Injection
        if (supplierSample.permId() == null || supplierSample.permId().permId() == null) {
            throw new OpenBisIntegrationException("Critical Error: Supplier with PermID '" + permId + "' has no PermID.");
        }
        String supplierId = supplierSample.permId().permId();
        Map<String, String> supplierProperties = supplierSample.properties();
        String supplierName = supplierProperties != null ? supplierProperties.get(properties.nameProperty()) : null;

        // Calculate Stats
        RatingStatsDto stats = calculateStats(supplierSample);

        // Map Orders
        List<OrderDetailDto> orders = List.of();
        if (supplierSample.children() != null) {
            orders = supplierSample.children().stream()
                    // Ensure we only process Orders
                    .filter(child -> child.type() != null && child.type().code().equals(properties.order().typeCode()))
                    .map(child -> orderMapper.toApiDto(child, supplierId, supplierName))
                    .toList();
        }

        return supplierMapper.toApiDto(supplierSample, stats, orders);
    }

    /**
     * Creates a new supplier in openBIS based on the provided data.
     *
     * @param creationDto The data for the new supplier.
     * @return The DTO of the newly created supplier (fetched fresh from openBIS to ensure consistency).
     */
    public SupplierDetailDto createSupplier(SupplierCreationDto creationDto) {
        log.info("Creating a new supplier {}", creationDto.name());

        // Map API DTO to openBIS Creation DTO
        SampleCreation creation = supplierMapper.toOpenBisCreation(creationDto);

        // Execute Creation via Client
        List<SamplePermId> createdIds = openBisClient.createSamples(List.of(creation));

        if (createdIds.isEmpty()) throw new OpenBisIntegrationException("Creation failed: No PermID returned from openBIS");
        SamplePermId permId = createdIds.getFirst();
        log.info("Successfully created supplier with PermID: {}", permId.permId());

        // Fetch the newly created supplier to return full details
        return fetchSupplierMetadataByPermId(permId.permId());
    }

    /**
     * Updates the master data of an existing supplier.
     *
     * @param permId The openBIS PermID of the supplier.
     * @param updateDto The update data.
     * @return The updated supplier details.
     */
    public SupplierDetailDto updateSupplier(String permId, SupplierUpdateDto updateDto) {
        log.info("Updating supplier with PermID: {}", permId);

        // Validation: Check if the supplier exists AND is actually a supplier
        checkSupplierExists(permId);

        // Map to OpenBIS Update
        SampleUpdate update = supplierMapper.toOpenBisUpdate(permId, updateDto);

        // Execute Update
        openBisClient.updateSamples(List.of(update));

        // Fetch and return the updated supplier (to show the changes in response)
        return fetchSupplierMetadataByPermId(permId);
    }

    /**
     * Checks if a supplier with the given PermID exists in openBIS.
     *
     * @param permId The PermID of the supplier to check.
     * @throws OpenBisResourceNotFoundException if the supplier does not exist.
     */
    private void checkSupplierExists(String permId) {
        SampleSearchCriteria criteria = SampleSearchCriteria.create()
                .with(PermIdSearchCriteria.withId(permId))
                .with(SampleTypeSearchCriteria.withCode(properties.supplier().typeCode()));

        SampleFetchOptions fetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                null);

        List<OpenBisSample> existingSamples = openBisClient.searchSamples(criteria, fetchOptions);
        if (existingSamples.isEmpty()) {
            throw new OpenBisResourceNotFoundException("Supplier with PermID " + permId + " not found.");
        }
    }

    /**
     * Calculates the average ratings for a given supplier based on its child orders and their ratings.
     * <p>
     * Logic:
     * - Null or blank values in openBIS are ignored (they do not pull down the average).
     * - Averages are calculated individually per category (Quality, Cost, etc.).
     * </p>
     *
     * @param supplier The supplier sample.
     * @return The aggregated rating statistics.
     */
    private RatingStatsDto calculateStats(OpenBisSample supplier) {
        List<OpenBisSample> orders = supplier.children();
        if (orders == null || orders.isEmpty()) {
            return emptyStats();
        }

        // Flatten: Supplier -> Orders -> Ratings
        List<OpenBisSample> ratings = orders.stream()
                .map(OpenBisSample::children)           // Get children of Orders
                .filter(Objects::nonNull)               // Ignore Orders without children
                .flatMap(List::stream)                  // Convert Stream<List<Sample>> to Stream<Sample>
                .filter(child -> child.type() != null  // Handle null type gracefully
                        && properties // If the child type is defined
                        .rating()
                        .typeCode()
                        .equals(child.type().code()))   // Ensure strict type safety
                .toList();

        if (ratings.isEmpty()) {
            return emptyStats();
        }

        // Initialize accumulators for each category
        RatingAccumulator qualityAcc = new RatingAccumulator();
        RatingAccumulator costAcc = new RatingAccumulator();
        RatingAccumulator reliabilityAcc = new RatingAccumulator();
        RatingAccumulator availabilityAcc = new RatingAccumulator();
        RatingAccumulator totalAcc = new RatingAccumulator();

        // Iterate and accumulate
        for (OpenBisSample rating : ratings) {
            String code = rating.code();

            qualityAcc.add(OpenBisUtils.parseDoubleOrNull(
                    rating.getProperty(OpenBisSchemaConstants.QUALITY_RATING_PROPERTY),
                    OpenBisSchemaConstants.QUALITY_RATING_PROPERTY, code));

            costAcc.add(OpenBisUtils.parseDoubleOrNull(
                    rating.getProperty(OpenBisSchemaConstants.COST_RATING_PROPERTY),
                    OpenBisSchemaConstants.COST_RATING_PROPERTY, code));

            reliabilityAcc.add(OpenBisUtils.parseDoubleOrNull(
                    rating.getProperty(OpenBisSchemaConstants.RELIABILITY_RATING_PROPERTY),
                    OpenBisSchemaConstants.RELIABILITY_RATING_PROPERTY, code));

            availabilityAcc.add(OpenBisUtils.parseDoubleOrNull(
                    rating.getProperty(OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY),
                    OpenBisSchemaConstants.AVAILABILITY_RATING_PROPERTY, code));

            totalAcc.add(OpenBisUtils.parseDoubleOrNull(
                    rating.getProperty(OpenBisSchemaConstants.TOTAL_SCORE_RATING_PROPERTY),
                    OpenBisSchemaConstants.TOTAL_SCORE_RATING_PROPERTY, code));
        }

        return new RatingStatsDto(
                qualityAcc.getAverage(),
                costAcc.getAverage(),
                reliabilityAcc.getAverage(),
                availabilityAcc.getAverage(),
                totalAcc.getAverage(),
                ratings.size() // Total count of rating objects
        );
    }

    /**
     * Generates an empty stats object.
     * We use nulls for averages to indicate "no ratings yet" to the frontend.
     */
    private RatingStatsDto emptyStats() {
        return new RatingStatsDto(null, null, null, null, null, 0);
    }

    /**
     * Internal helper class to handle summation and averaging of rating values.
     * Encapsulates the logic for ignoring null values and calculating rounded averages.
     */
    private static class RatingAccumulator {
        private double sum = 0.0;
        private int count = 0;

        /**
         * Adds a value to the accumulator if it is not null.
         *
         * @param value The value to add (can be null).
         */
        void add(Double value) {
            if (value != null) {
                sum += value;
                count++;
            }
        }

        /**
         * Calculates the average of the accumulated values.
         *
         * @return The average rounded to 2 decimal places, or null if the count is 0.
         */
        Double getAverage() {
            if (count == 0) {
                return null;
            }
            return Math.round((sum / count) * 100.0) / 100.0;
        }
    }

    /**
     * Optimized fetch for a newly created supplier.
     * <p>
     * This method performs a "Shallow Fetch". It loads properties
     * but explicitly excludes children (Orders/Ratings), as a fresh supplier cannot have history yet.
     * Do NOT use this method for a general "Get By ID" endpoint where statistics are required.
     * </p>
     *
     * @param permId The PermID of the supplier to fetch.
     * @return The mapped SupplierDetailDto with null stats.
     */
    private SupplierDetailDto fetchSupplierMetadataByPermId(String permId) {
        SampleSearchCriteria criteria = SampleSearchCriteria.create().with(PermIdSearchCriteria.withId(permId));

        // Optimized Fetch Options: Properties & Type only. No hierarchy.
        SampleFetchOptions fetchOptions = new SampleFetchOptions(
                new PropertyFetchOptions(),
                new SampleTypeFetchOptions(),
                null,
                null
        );
        List<OpenBisSample> results = openBisClient.searchSamples(criteria, fetchOptions);

        if (results.isEmpty()) {
            throw new OpenBisIntegrationException("Critical Error: Created supplier with PermID " + permId + " could not be retrieved immediately after creation.");
        }

        return supplierMapper.toApiDto(results.getFirst(), null, null);
    }
}
