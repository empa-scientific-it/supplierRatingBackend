package io.github.supplierratingsoftware.supplierratingbackend.constant.openbis;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains business schema constants specific to the Supplier Rating configuration in OpenBIS.
 * Includes property codes and identifiers defined in the OpenBIS instance.
 */
public final class OpenBisSchemaConstants {

    // Property Codes
    // - Supplier
    public static final String CUSTOMER_NUMBER_SUPPLIER_PROPERTY = "KUNDENNUMMER";
    public static final String ADDITION_SUPPLIER_PROPERTY = "LIEFERANTEN_ZUSATZ";
    public static final String STREET_SUPPLIER_PROPERTY = "LIEFERANTEN_STRASSE";
    public static final String PO_BOX_SUPPLIER_PROPERTY = "LIEFERANTEN_POSTFACH";
    public static final String COUNTRY_SUPPLIER_PROPERTY = "LIEFERANTEN_LAND";
    public static final String ZIP_CODE_SUPPLIER_PROPERTY = "LIEFERANTEN_POSTLEITZAHL";
    public static final String CITY_SUPPLIER_PROPERTY = "LIEFERANTEN_ORT";
    public static final String WEBSITE_SUPPLIER_PROPERTY = "LIEFERANTEN_WEBLINK";
    public static final String EMAIL_SUPPLIER_PROPERTY = "LIEFERANTEN_EMAIL";
    public static final String PHONE_NUMBER_SUPPLIER_PROPERTY = "LIEFERANTEN_TELEFON";
    public static final String VAT_ID_SUPPLIER_PROPERTY = "MWST";
    public static final String CONDITIONS_SUPPLIER_PROPERTY = "KONDITIONEN";
    public static final String CUSTOMER_INFO_SUPPLIER_PROPERTY = "KUNDENINFORMATION";

    // - Order
    public static final String MAIN_CATEGORY_ORDER_PROPERTY = "BESTELLUNG_HK";
    public static final String SUB_CATEGORY_ORDER_PROPERTY = "BESTELLUNG_UK";
    public static final String DESCRIPTION_ORDER_PROPERTY = "BEZEICHNUNG";
    public static final String FREQUENCY_ORDER_PROPERTY = "RYTHMUS"; // Mapped to misspelled "RYTHMUS" [sic!] in openBIS
    public static final String CONTACT_NAME_ORDER_PROPERTY = "ANSPRECHPERSON";
    public static final String CONTACT_EMAIL_ORDER_PROPERTY = "ANSPRECHPERSON_EMAIL";
    public static final String CONTACT_PHONE_ORDER_PROPERTY = "ANSPRECHPERSON_TELEFON";
    public static final String ORDER_REASON_ORDER_PROPERTY = "BESCHAFFUNGSGRUND";
    public static final String ORDER_METHOD_ORDER_PROPERTY = "BESTELLART";
    public static final String PURCHASER_ORDER_PROPERTY = "BESTELLER";
    public static final String ORDER_DATE_ORDER_PROPERTY = "BESTELLDATUM";
    public static final String DELIVERY_DATE_ORDER_PROPERTY = "LIEFERDATUM";
    public static final String ORDER_COMMENT_ORDER_PROPERTY = "KOMMENTAR";

    // - Rating Status of Orders
    public static final String RATING_STATUS_RATED_ORDER_PROPERTY = "RATED";
    public static final String RATING_STATUS_PENDING_ORDER_PROPERTY = "PENDING";

    // - Rating
    public static final String QUALITY_RATING_PROPERTY = "QUALITAET";
    public static final String QUALITY_REASON_RATING_PROPERTY = "GRUND_QUALITAET";
    public static final String COST_RATING_PROPERTY = "KOSTEN";
    public static final String COST_REASON_RATING_PROPERTY = "KOSTEN_BEGRUENDUNG";
    public static final String RELIABILITY_RATING_PROPERTY = "EINHALTUNG_TERMINE";
    public static final String RELIABILITY_REASON_RATING_PROPERTY = "EINHALTUNG_TERMINE_GRUND";
    public static final String AVAILABILITY_RATING_PROPERTY = "VERFUEGBARKEIT";
    public static final String AVAILABILITY_REASON_RATING_PROPERTY = "VERFUEGBARKEIT_BEGRUENDUNG";
    public static final String TOTAL_SCORE_RATING_PROPERTY = "GESAMTBEWERTUNG";
    public static final String RATING_COMMENT_RATING_PROPERTY = "KOMMENTAR";

    // Vocabulary Mappings
    // NOTE: If you extend the vocabularies for
    //       - Order Main Category,
    //       - Order Sub Category or
    //       - Country
    //       in openBIS, you need to update these mappings in this class accordingly.
    //       You will find key and value pairs for the mappings in this class.
    //       These Mappings are used to map between user-friendly terms for the REST-API
    //       and OpenBIS codes (vocabulary) for the openBIS V3 API.
    //       For each mapping, there are two directions: Label -> Code and Code -> Label.
    //       You only need to update the direction Label -> Code.
    //       The Code -> Label direction is derived programmatically from the Label -> Code map.

    /**
     * Order Main Category Mapping: <strong>Label [key]</strong> -> <strong>Code [value]</strong>
     * <ul>
     *     <li><strong>Label [key]:</strong> API: user-friendly term such as "Beschaffung" for REST-API</li>
     *     <li><strong>Code [value]:</strong> OpenBIS: openBIS Code such as "BESCHAFFUNG" for openBIS V3 API</li>
     * </ul>
     * @see #ORDER_MAIN_CATEGORY_MAPPING_CODE_TO_LABEL
     */
    public static final Map<String, String> ORDER_MAIN_CATEGORY_MAPPING_LABEL_TO_CODE = Map.of(
            "Beschaffung", "BESCHAFFUNG",
            "Dienstleistung", "DIENSTLEISTUNG"
    );

    /**
     * Order Main Category Mapping: <strong>Code [key]</strong> -> <strong>Label [value]</strong>
     * <ul>
     *   <li><strong>Code [key]:</strong> openBIS Code such as "BESCHAFFUNG" for openBIS V3 API</li>
     *   <li><strong>Label [value]:</strong> user-friendly term such as "Beschaffung" for REST-API</li>
     * </ul>
     * @see #ORDER_MAIN_CATEGORY_MAPPING_LABEL_TO_CODE
     */
    public static final Map<String, String> ORDER_MAIN_CATEGORY_MAPPING_CODE_TO_LABEL = ORDER_MAIN_CATEGORY_MAPPING_LABEL_TO_CODE.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (existing, replacement) -> existing));

    /**
     * Order Sub Category Mapping: <strong>Label [key]</strong> -> <strong>Code [value]</strong>
     * <ul>
     *     <li><strong>Label [key]:</strong> API: user-friendly name such as "PC Hardware" for REST-API</li>
     *     <li><strong>Code [value]:</strong> OpenBIS: openBIS Code such as "PC_HW" for openBIS V3 API</li>
     * </ul>
     *
     * @see #ORDER_SUB_CATEGORY_MAPPING_CODE_TO_LABEL
     */
    public static final Map<String, String> ORDER_SUB_CATEGORY_MAPPING_LABEL_TO_CODE = Map.of(
            "Beratung", "BERATUNG",
            "Dienstleistung", "DIENSTLEISTUNG",
            "Gerät/Werkzeug", "GERAET_WERKZEUG",
            "Maschine", "MASCHINE",
            "Messgeräte", "MESSGERAETE",
            "Messmittel", "MESSMITTEL",
            "PC Hardware", "PC_HW",
            "PC Software", "PC_SF",
            "Prüfmaschine", "PRUEFMASCHINE"
    );

    /**
     * Order Sub Category Mapping: <strong>Code [key]</strong> -> <strong>Label [value]</strong>
     * <ul>
     *     <li><strong>Code [key]:</strong> OpenBIS: openBIS Code such as "PC_HW" for openBIS V3 API</li>
     *     <li><strong>Label [value]:</strong> API: user-friendly name such as "PC Hardware" for REST-API</li>
     * </ul>
     *
     * @see #ORDER_SUB_CATEGORY_MAPPING_LABEL_TO_CODE
     */
    public static final Map<String, String> ORDER_SUB_CATEGORY_MAPPING_CODE_TO_LABEL = ORDER_SUB_CATEGORY_MAPPING_LABEL_TO_CODE.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (existing, replacement) -> existing));

    /**
     * Supplier Country Mapping: <strong>Label [key]</strong> -> <strong>Code [value]</strong>
     * <ul>
     *     <li><strong>Label [key]:</strong> API: user-friendly name such as "Switzerland", or for now just also "CH" for REST-API</li>
     *     <li><strong>Code [value]:</strong> OpenBIS: openBIS Code such as "CH" for openBIS V3 API</li>
     * </ul>
     * <p>
     * <strong>Note:</strong> This identity mapping is currently kept for consistency and future extensibility.
     * Even if the current keys and values are identical, this mapping allows for future changes without
     * affecting the API and also for consistency.
     * </p>
     * <ul>
     *     <li>If the country codes are extended in the future, the API will still work correctly.</li>
     *     <li>If the user facing country names (keys) are going to be changed to e.g., full country names ("Switzerland", "Germany", etc.),
     *         the API will still work correctly (but that case might need clarification in the documentation).</li>
     * </ul>
     *
     * @see #SUPPLIER_COUNTRY_MAPPING_CODE_TO_LABEL
     */
    public static final Map<String, String> SUPPLIER_COUNTRY_MAPPING_LABEL_TO_CODE = Map.of(
            "CH", "CH",
            "D", "D",
            "F", "F",
            "FL", "FL",
            "NL", "NL"
    );

    /**
     * Supplier Country Mapping: <strong>Code [key]</strong> -> <strong>Label [value]</strong>
     * <ul>
     *     <li><strong>Code [key]:</strong> OpenBIS: openBIS Code such as "CH" for openBIS V3 API</li>
     *     <li><strong>Label [value]:</strong> API: user-friendly name such as "Switzerland", or for now just also "CH" for REST-API</li>
     * </ul>
     * <p>
     * <strong>Note:</strong> See {@link #SUPPLIER_COUNTRY_MAPPING_LABEL_TO_CODE} for explanation of the identity mapping.
     * </p>
     *
     * @see #SUPPLIER_COUNTRY_MAPPING_LABEL_TO_CODE
     */
    public static final Map<String, String> SUPPLIER_COUNTRY_MAPPING_CODE_TO_LABEL = SUPPLIER_COUNTRY_MAPPING_LABEL_TO_CODE.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (existing, replacement) -> existing));

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private OpenBisSchemaConstants() {
    }
}
