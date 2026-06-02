package io.github.supplierratingsoftware.supplierratingbackend.service;

import io.github.supplierratingsoftware.supplierratingbackend.config.OpenBisProperties;
import io.github.supplierratingsoftware.supplierratingbackend.constant.openbis.OpenBisSchemaConstants;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderCreationDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderDetailDto;
import io.github.supplierratingsoftware.supplierratingbackend.dto.api.OrderUpdateDto;
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
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.ProjectSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleParentsSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SampleTypeSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.search.SpaceSearchCriteria;
import io.github.supplierratingsoftware.supplierratingbackend.dto.openbis.update.SampleUpdate;
import io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisResourceNotFoundException;
import io.github.supplierratingsoftware.supplierratingbackend.integration.openbis.OpenBisClient;
import io.github.supplierratingsoftware.supplierratingbackend.mapper.OrderMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OpenBisClient openBisClient;

    @Mock
    private OrderMapper orderMapper;

    private OpenBisProperties properties;

    private OrderService orderService;

    @Captor
    private ArgumentCaptor<List<SampleCreation>> creationCaptor;

    @Captor
    private ArgumentCaptor<SampleSearchCriteria> criteriaCaptor;

    @Captor
    private ArgumentCaptor<List<SampleUpdate>> updateCaptor;

    // OpenBis Property Constants
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

    private static final String FULL_PROJECT_CODE_ORDER = "/" + DEFAULT_SPACE_CODE + "/" + ORDER_PROJECT_CODE;
    private static final String FULL_COLLECTION_CODE_ORDER = FULL_PROJECT_CODE_ORDER + "/" + ORDER_COLLECTION_CODE;

    private static final String DUMMY_API_URL = "dummyApiUrl";
    private static final String DUMMY_USER = "dummyUser";
    private static final String DUMMY_PASSWORD = "dummyPassword";

    private static final String DUMMY_UUID = UUID.randomUUID().toString();
    private static final String DUMMY_ORDER_PERM_ID = ORDER_TYPE_CODE + "-" + DUMMY_UUID;
    private static final String DUMMY_SUPPLIER_PERM_ID = SUPPLIER_TYPE_CODE + "-" + DUMMY_UUID;

    // Order Constants
    private static final String DUMMY_ORDER_NAME = "Test Order Name";
    private static final String DUMMY_MAIN_CATEGORY = "Beschaffung";
    private static final String DUMMY_SUB_CATEGORY = "Messmittel";
    private static final String DUMMY_REASON = "Test Reason";
    private static final String DUMMY_ORDERED_BY = "John Doe";
    private static final String DUMMY_ORDER_DATE = "2026-01-01";
    private static final String DUMMY_ORDER_DETAILS = "Test Details";
    private static final String DUMMY_ORDER_FREQUENCY = "Test Frequency";
    private static final String DUMMY_ORDER_CONTACT_NAME = "Test Contact Name";
    private static final String DUMMY_ORDER_CONTACT_EMAIL = "Test Contact Email";
    private static final String DUMMY_ORDER_CONTACT_PHONE = "Test Contact Phone";
    private static final String DUMMY_ORDER_METHOD = "Test Method";
    private static final String DUMMY_ORDER_DELIVERY_DATE = "2026-01-01";
    private static final String DUMMY_ORDER_COMMENT = "Test Comment";

    // Supplier Constants
    private static final String DUMMY_SUPPLIER_NAME = "Test Supplier Name";

    /**
     * Helper method to create an order OpenBis sample with specified properties.
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
     * Helper method to create a minimal supplier sample for parent linking or existence checks.
     */
    private OpenBisSample createOpenBisSupplier(String id) {
        return new OpenBisSample(
                new OpenBisPermId(id),
                new OpenBisEntityType(SUPPLIER_TYPE_CODE),
                SUPPLIER_TYPE_CODE + "-" + UUID.randomUUID(),
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    /**
     * Helper method to create a valid OrderCreationDto.
     */
    private OrderCreationDto getOrderCreationDto() {
        return new OrderCreationDto(
                DUMMY_ORDER_NAME,
                DUMMY_MAIN_CATEGORY,
                DUMMY_SUB_CATEGORY,
                DUMMY_ORDER_DETAILS,
                DUMMY_ORDER_FREQUENCY,
                DUMMY_ORDER_CONTACT_NAME,
                DUMMY_ORDER_CONTACT_EMAIL,
                DUMMY_ORDER_CONTACT_PHONE,
                DUMMY_REASON,
                DUMMY_ORDER_METHOD,
                DUMMY_ORDERED_BY,
                DUMMY_ORDER_DATE,
                DUMMY_ORDER_DELIVERY_DATE,
                DUMMY_ORDER_COMMENT,
                DUMMY_SUPPLIER_PERM_ID
        );
    }

    /**
     * Helper method to create a valid OrderUpdateDto.
     */
    private OrderUpdateDto getOrderUpdateDto() {
        return new OrderUpdateDto(
                DUMMY_ORDER_NAME,
                DUMMY_MAIN_CATEGORY,
                DUMMY_SUB_CATEGORY,
                DUMMY_ORDER_DETAILS,
                null,
                null,
                null,
                null,
                DUMMY_REASON,
                null,
                DUMMY_ORDERED_BY,
                DUMMY_ORDER_DATE,
                null,
                null
        );
    }

    /**
     * Dummy return object for mapper mocks (read).
     */
    private OrderDetailDto getMinimalOrderDetailDto() {
        return new OrderDetailDto(
                DUMMY_ORDER_NAME,
                DUMMY_MAIN_CATEGORY,
                DUMMY_SUB_CATEGORY,
                null,
                null,
                null,
                null,
                null,
                DUMMY_REASON,
                null,
                DUMMY_ORDERED_BY,
                DUMMY_ORDER_DATE,
                null,
                null,
                DUMMY_ORDER_PERM_ID,
                ORDER_TYPE_CODE + "-" + DUMMY_UUID,
                OpenBisSchemaConstants.RATING_STATUS_PENDING_ORDER_PROPERTY,
                DUMMY_SUPPLIER_PERM_ID,
                DUMMY_SUPPLIER_NAME,
                null
        );
    }

    /**
     * Helper method to create a valid OrderSampleCreationDto.
     */
    private SampleCreation getOrderSampleCreationDto() {
        return new SampleCreation(
                new SpacePermId(DEFAULT_SPACE_CODE),
                new ProjectIdentifier(FULL_PROJECT_CODE_ORDER),
                new ExperimentIdentifier(FULL_COLLECTION_CODE_ORDER),
                new EntityTypePermId(ORDER_TYPE_CODE),
                ORDER_TYPE_CODE + "-" + DUMMY_UUID,
                Collections.emptyMap(),
                List.of(new SamplePermId(DUMMY_SUPPLIER_PERM_ID))
        );
    }

    /**
     * Dummy return object for mapper mocks (write - update).
     */
    private SampleUpdate getOrderSampleUpdateDto(String permId) {
        return new SampleUpdate(
                new SamplePermId(permId),
                Collections.emptyMap()
        );
    }

    /**
     * Helper method to create a list of sample permission IDs (returned by createSamples).
     */
    private List<SamplePermId> getOrderSamplePermIds(String permId) {
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

        orderService = new OrderService(openBisClient, orderMapper, properties);
    }

    // Testing

    /**
     * Test method to verify that getAllOrders returns a list of orders when no supplier filter is provided.
     * <p>
     * It ensures that:
     * <ul>
     *      <li>The OpenBIS client is called with search criteria containing Space, Project, and Type.</li>
     *      <li>The search criteria does <b>not</b> contain a parent filter.</li>
     *      <li>The results are correctly mapped to DTOs.</li>
     * </ul>
     * </p>
     */
    @Test
    void getAllOrders_shouldReturnAllOrders_whenNoSupplierFilterProvided() {

        // Arrange
        OpenBisSample orderSample = createOpenBisOrder(DUMMY_ORDER_PERM_ID, DUMMY_SUPPLIER_PERM_ID);
        OrderDetailDto expectedDto = getMinimalOrderDetailDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(orderSample));
        when(orderMapper.toApiDto(any())).thenReturn(expectedDto);
        SampleSearchCriteria expectedCriteria = SampleSearchCriteria.create()
                .with(SpaceSearchCriteria.withCode(DEFAULT_SPACE_CODE))
                .with(ProjectSearchCriteria.withCode(ORDER_PROJECT_CODE))
                .with(SampleTypeSearchCriteria.withCode(ORDER_TYPE_CODE));

        // Act
        List<OrderDetailDto> result = orderService.getAllOrders(null);

        // Assert
        // - Result
        assertThat(result.getFirst()).isEqualTo(expectedDto);

        // - Verify Criteria
        verify(openBisClient).searchSamples(criteriaCaptor.capture(), any());
        SampleSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria)
                .usingRecursiveComparison()
                .isEqualTo(expectedCriteria);
    }

    /**
     * Test method to verify that getAllOrders correctly applies the supplier filter when provided.
     * <p>
     * It ensures that:
     * <ul>
     *      <li>The OpenBIS client is called with an additional {@code SampleParentsSearchCriteria}.</li>
     *      <li>The parent ID in the criteria matches the provided supplier ID.</li>
     * </ul>
     * </p>
     */
    @Test
    void getAllOrders_shouldFilterBySupplier_whenSupplierIdProvided() {

        // Arrange
        OpenBisSample orderSample = createOpenBisOrder(DUMMY_ORDER_PERM_ID, DUMMY_SUPPLIER_PERM_ID);
        OrderDetailDto expectedDto = getMinimalOrderDetailDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(orderSample));
        when(orderMapper.toApiDto(any())).thenReturn(expectedDto);
        SampleSearchCriteria expectedCriteria = SampleSearchCriteria.create()
                .with(SpaceSearchCriteria.withCode(DEFAULT_SPACE_CODE))
                .with(ProjectSearchCriteria.withCode(ORDER_PROJECT_CODE))
                .with(SampleTypeSearchCriteria.withCode(ORDER_TYPE_CODE))
                .with(SampleParentsSearchCriteria.withParentId(DUMMY_SUPPLIER_PERM_ID));

        // Act
        List<OrderDetailDto> result = orderService.getAllOrders(DUMMY_SUPPLIER_PERM_ID);

        // Assert
        // - Result
        assertThat(result.getFirst()).isEqualTo(expectedDto);

        // - Verify Criteria contains Parent Filter
        verify(openBisClient).searchSamples(criteriaCaptor.capture(), any());
        SampleSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria)
                .usingRecursiveComparison()
                .isEqualTo(expectedCriteria);
    }

    /**
     * Test method to verify that getOrderById successfully retrieves and returns an order when it exists.
     * <p>
     * It ensures that:
     * <ul>
     *      <li>The OpenBIS client is called to search for the sample by PermID.</li>
     *      <li>The search criteria specifically targets the correct PermID and Order Type.</li>
     *      <li>The mapper is called to convert the result.</li>
     *      <li>The service returns the expected DTO.</li>
     * </ul>
     * </p>
     */
    @Test
    void getOrderById_shouldReturnOrder_whenFound() {

        // Arrange
        OpenBisSample orderSample = createOpenBisOrder(DUMMY_ORDER_PERM_ID, DUMMY_SUPPLIER_PERM_ID);
        OrderDetailDto expectedDto = getMinimalOrderDetailDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(orderSample));
        when(orderMapper.toApiDto(any())).thenReturn(expectedDto);
        SampleSearchCriteria expectedCriteria = SampleSearchCriteria.create()
                .with(PermIdSearchCriteria.withId(DUMMY_ORDER_PERM_ID))
                .with(SampleTypeSearchCriteria.withCode(ORDER_TYPE_CODE));


        // Act
        OrderDetailDto result = orderService.getOrderById(DUMMY_ORDER_PERM_ID);

        // Assert
        // - Result
        assertThat(result).isEqualTo(expectedDto);

        // - Verify Criteria
        verify(openBisClient).searchSamples(criteriaCaptor.capture(), any());
        SampleSearchCriteria capturedCriteria = criteriaCaptor.getValue();
        assertThat(capturedCriteria)
                .usingRecursiveComparison()
                .isEqualTo(expectedCriteria);
    }

    /**
     * Test method to verify that getOrderById throws an OpenBisResourceNotFoundException
     * when the order is not found.
     * <p>
     * This tests the error handling path ensuring the service fails gracefully
     * when the client returns an empty list.
     * </p>
     */
    @Test
    void getOrderById_shouldThrowException_whenNotFound() {

        // Arrange
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert
        Assertions.assertThrows(
                OpenBisResourceNotFoundException.class,
                () -> orderService.getOrderById(DUMMY_ORDER_PERM_ID)
        );

        // Verify that search was attempted
        verify(openBisClient).searchSamples(any(), any());
    }

    /**
     * Test method to verify the complete creation flow of an order.
     * <p>
     * This test ensures the sequence of operations:
     * <ol>
     *      <li>The service checks if the referenced supplier exists.</li>
     *      <li>The input DTO is mapped to an OpenBIS creation object.</li>
     *      <li>The OpenBIS client's {@code createSamples} method is called.</li>
     *      <li>The newly created order is fetched back using {@code searchSamples}.</li>
     *      <li>The fetched sample is mapped to a DTO and returned.</li>
     * </ol>
     * </p>
     */
    @Test
    void createOrder_shouldCreateAndReturnOrder_whenSupplierExists() {
        // Arrange
        OrderCreationDto creationDto = getOrderCreationDto();
        SampleCreation sampleCreation = getOrderSampleCreationDto();
        OpenBisSample supplierSample = createOpenBisSupplier(DUMMY_SUPPLIER_PERM_ID);
        OpenBisSample createdOrderSample = createOpenBisOrder(DUMMY_ORDER_PERM_ID, DUMMY_SUPPLIER_PERM_ID);
        OrderDetailDto expectedDto = getMinimalOrderDetailDto();
        when(openBisClient.searchSamples(any(), any()))
                .thenReturn(List.of(supplierSample))
                .thenReturn(List.of(createdOrderSample));
        when(orderMapper.toOpenBisCreation(any())).thenReturn(sampleCreation);
        when(openBisClient.createSamples(any())).thenReturn(getOrderSamplePermIds(DUMMY_ORDER_PERM_ID));
        when(orderMapper.toApiDto(any())).thenReturn(expectedDto);

        // Act
        OrderDetailDto result = orderService.createOrder(creationDto);

        // Assert
        // - Result
        assertThat(result).isEqualTo(expectedDto);

        // - Verify Create Call
        verify(openBisClient).createSamples(creationCaptor.capture());
        assertThat(creationCaptor.getValue().getFirst()).isEqualTo(sampleCreation);

        // - Search Call to openBisClient (twice)
        //   - First call: does the supplier exist?
        //   - Second call: refetch the order after creation
        verify(openBisClient, org.mockito.Mockito.atLeast(2)).searchSamples(any(), any());
    }

    /**
     * Test method to verify that createOrder throws an exception when the referenced supplier does not exist.
     * <p>
     * It ensures that:
     * <ul>
     *      <li>The supplier existence check fails (returns empty list).</li>
     *      <li>An {@link OpenBisResourceNotFoundException} is thrown.</li>
     *      <li><b>No</b> attempt is made to create the order in OpenBIS.</li>
     * </ul>
     * </p>
     */
    @Test
    void createOrder_shouldThrowException_whenSupplierNotFound() {

        // Arrange
        OrderCreationDto creationDto = getOrderCreationDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisResourceNotFoundException.class,
                () -> orderService.createOrder(creationDto)
        );

        // Verify: Create was NEVER called
        verify(openBisClient, org.mockito.Mockito.never()).createSamples(any());
    }

    /**
     * Test method to verify the complete update flow of an order.
     * <p>
     * This test ensures the sequence of operations:
     * <ol>
     *      <li>The service checks if the order exists (via {@code searchSamples}).</li>
     *      <li>The input DTO is mapped to an OpenBIS update object.</li>
     *      <li>The OpenBIS client's {@code updateSamples} method is called.</li>
     *      <li>The updated order is fetched back using {@code searchSamples} (refetch).</li>
     *      <li>The fetched sample is mapped to a DTO and returned.</li>
     * </ol>
     * </p>
     */
    @Test
    void updateOrder_shouldUpdateAndReturnOrder_whenOrderExists() {

        // Arrange
        OrderUpdateDto updateDto = getOrderUpdateDto();
        SampleUpdate sampleUpdate = getOrderSampleUpdateDto(DUMMY_ORDER_PERM_ID);
        OpenBisSample existingOrder = createOpenBisOrder(DUMMY_ORDER_PERM_ID, DUMMY_SUPPLIER_PERM_ID);
        OrderDetailDto expectedDto = getMinimalOrderDetailDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(List.of(existingOrder));
        when(orderMapper.toOpenBisUpdate(any(), any())).thenReturn(sampleUpdate);
        when(orderMapper.toApiDto(any())).thenReturn(expectedDto);

        // Act
        OrderDetailDto result = orderService.updateOrder(DUMMY_ORDER_PERM_ID, updateDto);

        // Assert
        // - Result
        assertThat(result).isEqualTo(expectedDto);

        // - Verify Update Call
        verify(openBisClient).updateSamples(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getFirst()).isEqualTo(sampleUpdate);

        // - Search Call to openBisClient (twice)
        //   - First call: does the order exist?
        //   - Second call: refetch the order after update
        verify(openBisClient, org.mockito.Mockito.atLeast(2)).searchSamples(any(), any());
    }

    /**
     * Test method to verify that updateOrder throws an exception when the order to update does not exist.
     * <p>
     * It ensures that:
     * <ul>
     *      <li>The order existence check fails (returns empty list).</li>
     *      <li>An {@link OpenBisResourceNotFoundException} is thrown.</li>
     *      <li><b>No</b> attempt is made to update the order in OpenBIS.</li>
     * </ul>
     * </p>
     */
    @Test
    void updateOrder_shouldThrowException_whenOrderNotFound() {

        // Arrange
        OrderUpdateDto updateDto = getOrderUpdateDto();
        when(openBisClient.searchSamples(any(), any())).thenReturn(Collections.emptyList());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                io.github.supplierratingsoftware.supplierratingbackend.exception.OpenBisResourceNotFoundException.class,
                () -> orderService.updateOrder(DUMMY_ORDER_PERM_ID, updateDto)
        );

        // Verify: Update was NEVER called
        verify(openBisClient, org.mockito.Mockito.never()).updateSamples(any());
    }
}
