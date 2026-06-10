# OpenBIS V3 API Integration Documentation

## 1. Introduction & General API Concepts

### 1.1. Purpose of this Document

This document serves as the technical reference for the integration between the **Supplier Rating
Backend** (Spring Boot) and the **OpenBIS V3 Application Server**. It details the mechanism used to
translate internal Java Data Transfer Objects (DTOs) into the specific JSON-RPC 2.0 payloads required
by the OpenBIS API.

The primary focus is on the **Search API (`searchSamples`)**, documenting the strict filtering
strategies and recursive fetching logic implemented to retrieve hierarchical data (Suppliers, Orders,
and Ratings) reliably.

### 1.2. OpenBIS V3 JSON-RPC Fundamentals

The integration communicates exclusively via the OpenBIS V3 JSON-RPC endpoint. This API is stateless
and follows the standard JSON-RPC 2.0 specification.

* **Endpoint URL:** `https://<your-server>/openbis/openbis/rmi-application-server-v3.json`
* **HTTP Method:** `POST`
* **Content-Type:** `application/json` (Request), `application/json-rpc` (Response)
* **Serialization:** The API relies heavily on polymorphism. JSON objects must include a `@type`
  discriminator field (e.g., `@type: "as.dto.sample.search.SampleSearchCriteria"`) so the server
  can deserialize them into the correct Java classes.

### 1.3. Standard Request Structure

For data retrieval operations, the application uses the `searchSamples` method. The `params` array
in these requests strictly adheres to a fixed structure containing exactly **three elements**.

**The Three Pillars of a Search Request:**

1.  **Session Token (`String`):**
    The authentication string obtained via the `login` method. It identifies the user context for
    the operation.

2.  **Search Criteria (`SampleSearchCriteria`):**
    Defines **WHICH** entities to find. This utilizes a composite pattern to combine multiple filters
    (Space, Project, Type) using logical operators (AND/OR).

3.  **Fetch Options (`SampleFetchOptions`):**
    Defines **WHAT** data to load for the found entities. This controls the depth of the graph
    traversal (e.g., "load properties", "load parents", "load children").

**Generic JSON-RPC Envelope:**

```json
{
  "jsonrpc": "2.0",
  "method": "searchSamples",
  "id": "unique-request-id",
  "params": [
    "SESSION_TOKEN_STRING",
    {
      "@type": "as.dto.sample.search.SampleSearchCriteria",
      "operator": "AND",
      "criteria": [ ... ]
    },
    {
      "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
      "properties": { ... },
      "type": { ... },
      "parents": { ... },
      "children": { ... }
    }
  ]
}
```

## 2. Search & Filtering Strategy

### 2.1. The "Strict Filtering" Approach

To ensure data integrity and prevent "ghost data" (e.g., test entries, experimental samples, or misclassified
objects) from appearing in the application, the backend implements a **Strict Filtering Strategy**.

Previous implementations relied solely on the "Location" of a sample (Space and Project). However, openBIS
projects often contain heterogeneous data. For example, a project named `BESTELLUNGEN` might contain valid
`BESTELLUNG` objects, but also temporary `ENTRY` objects or experimental data.

**The Rule:**
Every search request must define the **Location** (Space + Project) AND the **Identity** (Sample Type) of the
target entities. This triple-constraint ensures that the API returns only valid business objects.

### 2.2. Search Criteria Composition

The search logic utilizes the openBIS **Composite Pattern**. The root `SampleSearchCriteria` acts as a
container that combines multiple sub-criteria using a logical `AND` operator.

**The Three Required Dimensions:**

1.  **Space Criteria:** Restricts the search to a specific Space (e.g., `LIEFERANTENBEWERTUNG`).
2.  **Project Criteria:** Restricts the search to a specific Project (e.g., `LIEFERANTEN`).
3.  **Type Criteria:** Restricts the search to a specific Sample Type (e.g., `LIEFERANT`).

**Criteria Composition Diagram:**

```mermaid
graph TD
    %% Nodes
    Root[("SampleSearchCriteria<br/>(Operator: AND)")]
    SpaceCrit["SpaceSearchCriteria"]
    ProjectCrit["ProjectSearchCriteria"]
    TypeCrit["SampleTypeSearchCriteria"]

    SpaceCode["CodeSearchCriteria<br/>(Value: 'LIEFERANTENBEWERTUNG')"]
    ProjectCode["CodeSearchCriteria<br/>(Value: 'LIEFERANTEN')"]
    TypeCode["CodeSearchCriteria<br/>(Value: 'LIEFERANT')"]

    %% Edges
    Root -->|1. Filter by Location| SpaceCrit
    Root -->|2. Filter by Location| ProjectCrit
    Root -->|3. Filter by Identity| TypeCrit

    SpaceCrit -->|contains| SpaceCode
    ProjectCrit -->|contains| ProjectCode
    TypeCrit -->|contains| TypeCode

    %% Styling
    style Root fill:#f9f,stroke:#333,stroke-width:2px,color:black
    style TypeCrit fill:#bbf,stroke:#333,stroke-width:2px,color:black
```

### 2.3. Why "Sample Type" is Mandatory

The inclusion of `SampleTypeSearchCriteria` is the result of specific architectural learnings:

* **Polymorphism Safety:** openBIS allows different entity types to coexist in the same collection.
  Filtering by type delegates the validation logic to the database engine, rather than filtering in
  Java memory.
* **Exclusion of Test Data:** Developers often create generic samples (default type `ENTRY`) for testing.
  Without type filtering, these objects appear in the API response, causing mapping errors because they lack
  business properties (e.g., missing `Name` or `Address`).
* **Performance:** Narrowing the search scope on the server side reduces the payload size and serialization overhead.

### 2.4. Data Organization Strategy (Projects vs. Collections)

To ensure long-term scalability (e.g., yearly archiving), the application distinguishes between
the **Read Scope** and the **Write Target**.

* **Collections (Experiments):** Used as containers for organization (e.g., `ORDERS_2024`, `ORDERS_2025`).
* **Projects:** The fixed parent container (e.g., `BESTELLUNGEN`).

**The Strategy:**
1.  **Reading (Search):** We always search at the **Project Level**. This automatically retrieves
    samples from *all* contained Collections/Experiments. This ensures that historical data (e.g.,
    from last year's collection) remains visible without changing the code.
2.  **Writing (Creation):** New entities must be assigned to a specific, **active Collection**
    (configurable via environment variables) to ensure they appear correctly in the openBIS GUI
    inventory tree.

### 2.5. Filtering by Parent (Hierarchy Search)

To efficiently retrieve child entities (e.g., "All Orders for Supplier X"), the backend utilizes
the **`SampleParentsSearchCriteria`**. This delegates the filtering logic to the openBIS database engine,
avoiding inefficient in-memory filtering of large datasets.

**Criteria Composition:**
The search request combines the standard Location/Type filters with a Parent filter:

1. Space: `LIEFERANTENBEWERTUNG`
2. Project: `BESTELLUNGEN`
3. Type: `BESTELLUNG`
4. **Parent:** `SampleParentsSearchCriteria` containing the `PermId` of the parent Supplier.

**Reference JSON Snippet:**

```json
{
  "@type": "as.dto.sample.search.SampleParentsSearchCriteria",
  "operator": "AND",
  "criteria": [
    {
      "@type": "as.dto.common.search.PermIdSearchCriteria",
      "fieldValue": {
        "@type": "as.dto.common.search.StringEqualToValue",
        "value": "TARGET_SUPPLIER_PERMID"
      }
    }
  ]
}
```

## 3. Java DTO Architecture & Request Composition

### 3.1. The Composite Pattern in Search Criteria

The OpenBIS V3 API employs the **Composite Design Pattern** for its search criteria. This design allows
complex, nested queries to be constructed dynamically. Unlike flat DTOs where properties are set directly
(e.g., `setSpace("SPACE")`), criteria objects act as **containers** holding lists of sub-criteria.

**Architectural Rules:**

1.  **Container Classes:** High-level criteria (like `SampleSearchCriteria`, `SpaceSearchCriteria`,
    `ProjectSearchCriteria`) extend `AbstractCompositeSearchCriteria`. They do not hold values directly.
2.  **Leaf Criteria:** Specific value matching is done by leaf criteria (like `CodeSearchCriteria`)
    which are added to the container's list.
3.  **Logical Operator:** Every container defines an operator (default: `AND`) to combine its sub-criteria.

**Java Implementation Strategy:**
The backend mimics this structure using a fluent API.
* **Base Class:** `AbstractCompositeSearchCriteria` provides the `criteria` list and `operator` field.
* **Fluent Method:** The `with(SearchCriteria... criteria)` method allows chaining criteria addition.

### 3.2. Polymorphism Handling (Jackson & `@type`)

A critical aspect of the integration is how Java objects are serialized into the specific JSON format
expected by OpenBIS. The API relies heavily on polymorphism, meaning a list of criteria can contain
objects of many different classes.

To support this, the application uses **Jackson Polymorphic Serialization**:

1.  **Discriminator Field (`@type`):**
    Every DTO sent to OpenBIS must include a meta-property named `@type`. This string tells the OpenBIS
    server which server-side Java class corresponds to the JSON object.

2.  **Interface Configuration (`SearchCriteria.java`):**
    The root interface uses `@JsonTypeInfo` and `@JsonSubTypes` to map Java classes to their OpenBIS
    type names.

    * `SampleSearchCriteria` &rarr; `"as.dto.sample.search.SampleSearchCriteria"`
    * `SampleTypeSearchCriteria` &rarr; `"as.dto.sample.search.SampleTypeSearchCriteria"`
    * `CodeSearchCriteria` &rarr; `"as.dto.common.search.CodeSearchCriteria"`

**Impact of Incorrect Configuration:**
If a subtype is missing from the `@JsonSubTypes` annotation, Jackson will fail to serialize the `@type`
field (or serialize it incorrectly). OpenBIS will subsequently ignore that specific filter, leading to
unfiltered "ghost data" being returned.

### 3.3. DTO Composition Diagram

The following class diagram illustrates how the backend internal DTOs are structured to enforce the
Composite Pattern and ensure correct JSON generation.

```mermaid
classDiagram
    %% Core Interface
    class SearchCriteria {
        <<Interface>>
        +@JsonTypeInfo
        +@JsonSubTypes
    }

    %% Abstract Base
    class AbstractCompositeSearchCriteria {
        <<Abstract>>
        -String operator = "AND"
        -List~SearchCriteria~ criteria
        +with(SearchCriteria...)
    }

    %% Concrete Implementations (Containers)
    class SampleSearchCriteria {
        +static create()
    }

    class SpaceSearchCriteria {
        +static withCode(String)
    }

    class ProjectSearchCriteria {
        +static withCode(String)
    }

    class SampleTypeSearchCriteria {
        +static withCode(String)
    }

    %% Concrete Implementation (Leaf)
    class CodeSearchCriteria {
        -StringEqualToValue fieldValue
        +static withCode(String)
    }

    %% Relationships
    SearchCriteria <|.. AbstractCompositeSearchCriteria : implements
    SearchCriteria <|.. CodeSearchCriteria : implements

    AbstractCompositeSearchCriteria <|-- SampleSearchCriteria : extends
    AbstractCompositeSearchCriteria <|-- SpaceSearchCriteria : extends
    AbstractCompositeSearchCriteria <|-- ProjectSearchCriteria : extends
    AbstractCompositeSearchCriteria <|-- SampleTypeSearchCriteria : extends

    %% Composition
    AbstractCompositeSearchCriteria o-- SearchCriteria : contains list of
```

## 4. Domain Entity Request Specifications

This section defines the precise filtering logic and JSON payloads used to retrieve specific business
entities. It serves as the reference implementation for any new service added to the backend.

### 4.1. Authentication (Login)

**Context:**
Before any search or retrieval operation can be performed, the client must authenticate against the
openBIS Application Server to obtain a **Session Token**. This token identifies the user and must be
included as the first parameter in every subsequent API call.

* **Responsible Component:** `OpenBisClient`
* **Method:** `login`
* **Return Value:** A String representing the Session Token (e.g., `admin-12345...`).

**Logic:**
The `login` method in the V3 API is straightforward. It does not require complex criteria objects,
only the credentials passed as a list of strings.

**Reference JSON Payload:**

```json
{
  "jsonrpc": "2.0",
  "method": "login",
  "id": "req-login-01",
  "params": [
    "username",
    "password"
  ]
}
```

**Response Handling:**
* **Success:** The `result` field contains the session token string.
* **Failure:** The `error` field contains authentication failure details.

**Usage in subsequent requests:**
The token received here is cached by the `OpenBisClient` and inserted into index `0` of the
`params` array for all `searchSamples` calls (see Scenarios A, B, and C).

```java
// Conceptual Usage
String sessionToken = login(); // returns "admin-12345..."

// Next request (e.g. searchSamples)
List<Object> params = new ArrayList<>();
params.add(sessionToken);
params.add(searchCriteria);
params.add(fetchOptions);
```

### 4.2. Retrieving Suppliers (Root Entities & Aggregation)

**Context:**
Suppliers are the root entities. To calculate rating statistics (Quality, Reliability, etc.)
directly in the backend without causing an "N+1 Select" performance issue, the application
utilizes a **Deep Fetch Strategy**. A single API call retrieves the Supplier, all associated
Orders (Children), and all associated Ratings (Grandchildren).

* **Responsible Service:** `SupplierService`
* **Search Scope:** Space + Project + Type
* **Hierarchy Requirement:** Must fetch `children` (Orders) and `children.children` (Ratings) to perform aggregation.

**Filtering Logic:**
1.  **Space:** Configured Default Space (e.g., `LIEFERANTENBEWERTUNG`)
2.  **Project:** Configured Supplier Project (e.g., `LIEFERANTEN`)
3.  **Type:** Configured Supplier Type (e.g., `LIEFERANT`)

**Reference JSON Payload (Recursive / Deep Fetch):**

```json
{
  "jsonrpc": "2.0",
  "method": "searchSamples",
  "id": "req-supplier-agg-01",
  "params": [
    "SESSION_TOKEN",
    {
      "@type": "as.dto.sample.search.SampleSearchCriteria",
      "operator": "AND",
      "criteria": [
        {
          "@type": "as.dto.space.search.SpaceSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "LIEFERANTENBEWERTUNG"
              }
            }
          ]
        },
        {
          "@type": "as.dto.project.search.ProjectSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "LIEFERANTEN"
              }
            }
          ]
        },
        {
          "@type": "as.dto.sample.search.SampleTypeSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "LIEFERANT"
              }
            }
          ]
        }
      ]
    },
    {
      "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
      "properties": {
        "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
      },
      "type": {
        "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
      },
      "parents": null,
      "children": {
        "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
        "properties": {
          "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
        },
        "type": {
          "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
        },
        "parents": null,
        "children": {
          "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
          "properties": {
            "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
          },
          "type": {
            "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
          },
          "parents": null,
          "children": null
        }
      }
    }
  ]
}
```

### 4.3. Retrieving Orders (Child Entities)

**Context:**
Orders represent the second level of the hierarchy. They are children of Suppliers. To reconstruct
this relationship in the application, the request must explicitly fetch the parent objects.

* **Responsible Service:** `OrderService`
* **Search Scope:** Space + Project + Type
* **Hierarchy Requirement:** Must fetch immediate parents to link the Order to a Supplier ID.

**Filtering Logic:**
1.  **Space:** Configured Default Space
2.  **Project:** Configured Order Project (e.g., `BESTELLUNGEN`)
3.  **Type:** Configured Order Type (e.g., `BESTELLUNG`)

**Reference JSON Payload (with Parents Fetch):**

```json
{
  "jsonrpc": "2.0",
  "method": "searchSamples",
  "id": "req-order-01",
  "params": [
    "SESSION_TOKEN",
    {
      "@type": "as.dto.sample.search.SampleSearchCriteria",
      "operator": "AND",
      "criteria": [
        {
          "@type": "as.dto.space.search.SpaceSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "LIEFERANTENBEWERTUNG"
              }
            }
          ]
        },
        {
          "@type": "as.dto.project.search.ProjectSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "BESTELLUNGEN"
              }
            }
          ]
        },
        {
          "@type": "as.dto.sample.search.SampleTypeSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "BESTELLUNG"
              }
            }
          ]
        }
      ]
    },
    {
      "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
      "properties": {
        "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
      },
      "type": {
        "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
      },
      "parents": {
        "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
        "properties": null,
        "type": null,
        "parents": null
      }
    }
  ]
}
```

### 4.3.1. Filtering Orders by Supplier (Server-Side)

**Context:**
To optimize performance and reduce payload size, the API supports filtering orders by their parent
Supplier directly on the server. This avoids fetching all orders when only a specific subset is needed.

**Mechanism:**
We use the `SampleParentsSearchCriteria` composite criteria. This criteria restricts the result set
to samples that are children of a specific parent sample (identified by PermID).

**Structure:**
* **Root:** `SampleSearchCriteria` (AND)
* **Filter 1:** Space/Project/Type (Standard Location Filter)
* **Filter 2:** `SampleParentsSearchCriteria` -> contains `PermIdSearchCriteria` (Target Supplier ID)

**Reference JSON Payload (Filtered by Supplier):**

```json
{
  "jsonrpc": "2.0",
  "method": "searchSamples",
  "id": "req-orders-filtered-01",
  "params": [
    "SESSION_TOKEN",
    {
      "@type": "as.dto.sample.search.SampleSearchCriteria",
      "operator": "AND",
      "criteria": [
        {
          "@type": "as.dto.space.search.SpaceSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "LIEFERANTENBEWERTUNG"
              }
            }
          ]
        },
        {
          "@type": "as.dto.project.search.ProjectSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "BESTELLUNGEN"
              }
            }
          ]
        },
        {
          "@type": "as.dto.sample.search.SampleTypeSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "BESTELLUNG"
              }
            }
          ]
        },
        {
          "@type": "as.dto.sample.search.SampleParentsSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.PermIdSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "TARGET_SUPPLIER_PERMID"
              }
            }
          ]
        }
      ]
    },
    {
      "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
      "properties": {
        "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
      },
      "type": {
        "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
      },
      "parents": {
        "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
        "properties": null,
        "type": null,
        "parents": null
      }
    }
  ]
}
```

### 4.4. Retrieving Ratings (Leaf Entities)

**Context:**
Ratings represent the third level of the hierarchy (Leaf nodes). They are children of Orders.
To link a rating back to the specific order it belongs to, the request must explicitly fetch the
parent objects.

* **Responsible Service:** `RatingService`
* **Search Scope:** Space + Project + Type
* **Hierarchy Requirement:** Must fetch immediate parents to link the Rating to an Order ID.

**Filtering Logic:**
1.  **Space:** Configured Default Space (e.g., `LIEFERANTENBEWERTUNG`)
2.  **Project:** Configured Rating Project (e.g., `BEWERTUNGEN`)
3.  **Type:** Configured Rating Type (e.g., `BESTELLBEWERTUNG`)

**Reference JSON Payload:**

```json
{
  "jsonrpc": "2.0",
  "method": "searchSamples",
  "id": "req-rating-01",
  "params": [
    "SESSION_TOKEN",
    {
      "@type": "as.dto.sample.search.SampleSearchCriteria",
      "operator": "AND",
      "criteria": [
        {
          "@type": "as.dto.space.search.SpaceSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "LIEFERANTENBEWERTUNG"
              }
            }
          ]
        },
        {
          "@type": "as.dto.project.search.ProjectSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "BEWERTUNGEN"
              }
            }
          ]
        },
        {
          "@type": "as.dto.sample.search.SampleTypeSearchCriteria",
          "operator": "AND",
          "criteria": [
            {
              "@type": "as.dto.common.search.CodeSearchCriteria",
              "fieldValue": {
                "@type": "as.dto.common.search.StringEqualToValue",
                "value": "BESTELLBEWERTUNG"
              }
            }
          ]
        }
      ]
    },
    {
      "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
      "properties": {
        "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
      },
      "type": {
        "@type": "as.dto.sample.fetchoptions.SampleTypeFetchOptions"
      },
      "parents": {
        "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
        "properties": null,
        "type": null,
        "parents": null
      }
    }
  ]
}
```

## 5. Fetch Options & Response Handling

### 5.1. Understanding `SampleFetchOptions`

In the OpenBIS V3 API, search criteria determine **which** entities are returned, while
`FetchOptions` determine **what data** is loaded for those entities.

OpenBIS uses a "lazy loading" philosophy by default. If you search for a sample without specifying
fetch options, the server returns a skeleton object containing only basic identifiers (PermId, Code).
To access business data (like `NAME`, `DATE`, or relationships), the client must explicitly request
these details.

**Key Components of `SampleFetchOptions`:**

* **`properties` (`PropertyFetchOptions`):**
  Instructs the server to load the `properties` map (key-value pairs). This is where custom
  metadata (e.g., "LIEFERANTEN_ORT", "BESTELLDATUM") is stored.
* **`type` (`SampleTypeFetchOptions`):**
  Instructs the server to load the `SampleType` definition. Useful for verifying the entity kind.
* **`parents` / `children` (`SampleFetchOptions`):**
  Controls graph traversal to load related entities.

### 5.2. Recursive Fetching (Parents & Hierarchy)

Our data model is hierarchical (`Supplier` &larr; `Order` &larr; `Rating`). To reconstruct these
relationships in the backend without making N+1 API calls, we use **Recursive Fetch Options**.

Since `SampleFetchOptions` contains fields for `parents` (which are themselves `SampleFetchOptions`),
we can nest these options to define the depth of the graph retrieval.

**Example: Fetching a Rating with its Order**

To link a Rating to an Order, we fetch the rating's properties and its immediate parent. Note that
we do *not* need the parent's properties, only its Identity (PermId).

```json
{
  "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
  "properties": {
    "@type": "as.dto.property.fetchoptions.PropertyFetchOptions"
  },
  "parents": {
    "@type": "as.dto.sample.fetchoptions.SampleFetchOptions",
    "properties": null,
    "type": null,
    "parents": null
  }
}
```

**Optimization Strategy:**
We strictly limit the fetch depth. For a Rating, we only fetch `parents` (Order). We do not fetch
`parents.parents` (Supplier), as the API design usually requires only the immediate foreign key
(`orderId`) to link objects.

**Example: Top-Down Aggregation (Supplier -> Order -> Rating)**

For the Supplier list view, we need to show average ratings. Instead of iterating over every
supplier and firing a separate request for its orders, we request the entire tree structure in the
initial fetch options.

* **Level 0 (Root):** Supplier (Load Properties & Type)
* **Level 1 (Children):** Order (Load Properties & Type)
* **Level 2 (Grandchildren):** Rating (Load Properties & Type - essential for calculation)

This approach ensures strict O(1) network performance (one request regardless of the number of suppliers).

### 5.3. Response Structure & Mapping

The JSON-RPC response returns an `OpenBisSearchResult` containing a list of `OpenBisSample` objects.
The structure of these objects mirrors the requested Fetch Options.

**Response Object Structure (Simplified):**

```json
{
  "permId": { "permId": "20231201-123" },
  "code": "RATING_456",
  "properties": {
    "QUALITAET": "5",
    "KOMMENTAR": "Excellent service"
  },
  "parents": [
    {
      "permId": { "permId": "20231130-999" },
      "code": "BESTELLUNG_999",
      "properties": null
    }
  ]
}
```

**Mapping Logic (Java):**

The backend Mappers (`RatingMapper`, `OrderMapper`) are responsible for flattening this structure
into API DTOs.

1.  **Properties:** Accessed via `sample.getProperties().get("KEY")`. Safe handling for null maps
    is required.
2.  **Relationships:** Accessed via `sample.getParents()`.
    * Since our model enforces a strict hierarchy, we typically expect exactly **one** parent for
      Orders and Ratings.
    * The Mapper extracts `parents.getFirst().getPermId().getPermId()` to populate fields like
      `orderId` or `supplierId`.

**Error Handling:**
If a required parent is missing (e.g., an orphan Order), the Mapper should handle this gracefully
(e.g., setting the ID to null) rather than throwing an `IndexOutOfBoundsException`.

**Safe Mapping Example (Java):**

```java
public RatingDto mapToDto(OpenBisSample sample) {
    // 1. Map simple properties (with null checks handled by Map.getOrDefault or similar)
    String quality = sample.getProperties().get("QUALITAET");
    
    // 2. Map Parent ID (Foreign Key) safely
    String orderId = null;
    List<OpenBisSample> parents = sample.getParents();
    
    // Check if parents exist and list is not empty
    if (parents != null && !parents.isEmpty()) {
        OpenBisSample parentOrder = parents.getFirst();
        if (parentOrder.getPermId() != null) {
            orderId = parentOrder.getPermId().getPermId();
        }
    } else {
        // Log warning: Orphan rating found
        log.warn("Rating {} has no parent order linked!", sample.getCode());
    }

    return new RatingDto(..., orderId, ...);
}
```

## 6. Entity Creation

### 6.1. Creating Suppliers

**Context:**
Creating a new supplier involves the `createSamples` JSON-RPC method.
To ensure the new supplier appears in the default openBIS GUI ("Collections" view), it **must** be
assigned to a specific Experiment (Collection).

* **Method:** `createSamples`
* **DTO:** `SampleCreation`
* **Mandatory Identifiers:** Space, Project, Experiment (Collection), Type.
* **Code Generation Strategy:** The backend automatically generates the `code` field using the pattern:
  `LIEFERANT-<UUID>` (e.g., `LIEFERANT-d290f1ee-6c54...`) to ensure global uniqueness.

**Assignment Strategy:**
* **Space:** Configured Default Space (e.g. `LIEFERANTENBEWERTUNG`)
* **Project:** Configured Supplier Project (e.g. `LIEFERANTEN`)
* **Experiment:** **Configurable Collection** (e.g. `/LIEFERANTENBEWERTUNG/LIEFERANTEN/LIEFERANTEN`). This allows the
  customer to change the target collection (e.g. for yearly rollover) via environment variables without code changes.

**Property Mapping & Validation Strategy:**

| API DTO Field    | openBIS Property Code      | Mandatory (Business Rule) |
|:-----------------|:---------------------------|:--------------------------|
| `name`           | `$NAME`                    | **Yes**                   |
| `customerNumber` | `KUNDENNUMMER`             | **Yes**                   |
| `addition`       | `LIEFERANTEN_ZUSATZ`       | No                        |
| `street`         | `LIEFERANTEN_STRASSE`      | **Yes**                   |
| `poBox`          | `LIEFERANTEN_POSTFACH`     | No                        |
| `country`        | `LIEFERANTEN_LAND`         | **Yes**                   |
| `zipCode`        | `LIEFERANTEN_POSTLEITZAHL` | **Yes**                   |
| `city`           | `LIEFERANTEN_ORT`          | **Yes**                   |
| `website`        | `LIEFERANTEN_WEBLINK`      | No                        |
| `email`          | `LIEFERANTEN_EMAIL`        | No                        |
| `phoneNumber`    | `LIEFERANTEN_TELEFON`      | No                        |
| `vatId`          | `MWST`                     | **Yes**                   |
| `conditions`     | `KONDITIONEN`              | **Yes**                   |
| `customerInfo`   | `KUNDENINFORMATION`        | No                        |

**Reference JSON Payload (Full Example):**

```json
{
  "jsonrpc": "2.0",
  "method": "createSamples",
  "id": "req-create-supplier-final-01",
  "params": [
    "SESSION_TOKEN",
    [
      {
        "@type": "as.dto.sample.create.SampleCreation",
        "spaceId": {
          "@type": "as.dto.space.id.SpacePermId",
          "permId": "LIEFERANTENBEWERTUNG"
        },
        "projectId": {
          "@type": "as.dto.project.id.ProjectIdentifier",
          "identifier": "/LIEFERANTENBEWERTUNG/LIEFERANTEN"
        },
        "experimentId": {
          "@type": "as.dto.experiment.id.ExperimentIdentifier",
          "identifier": "/LIEFERANTENBEWERTUNG/LIEFERANTEN/LIEFERANTEN"
        },
        "typeId": {
          "@type": "as.dto.entitytype.id.EntityTypePermId",
          "permId": "LIEFERANT"
        },
        "code": "LIEFERANT-550e8400-e29b-41d4-a716-446655440000",
        "properties": {
          "$NAME": "Acme Corp",
          "KUNDENNUMMER": "100-200",
          "LIEFERANTEN_ZUSATZ": "Building B",
          "LIEFERANTEN_STRASSE": "Industrial Way 1",
          "LIEFERANTEN_POSTFACH": "PO Box 22",
          "LIEFERANTEN_LAND": "CH",
          "LIEFERANTEN_POSTLEITZAHL": "9000",
          "LIEFERANTEN_ORT": "Zurich",
          "LIEFERANTEN_WEBLINK": "https://acme.com",
          "LIEFERANTEN_EMAIL": "contact@acme.com",
          "LIEFERANTEN_TELEFON": "+41 44 123 45 67",
          "MWST": "CHE-123.456.789",
          "KONDITIONEN": "30 days net",
          "KUNDENINFORMATION": "Key supplier for Q1"
        }
      }
    ]
  ]
}
```

**Response Handling:**
On success, the API returns a list of identifiers for the created samples.

```json
{
  "jsonrpc": "2.0",
  "id": "req-create-supplier-final-01",
  "result": [
    {
      "@type": "as.dto.sample.id.SamplePermId",
      "permId": "20251231103114886-315"
    }
  ]
}
```

### 6.2. Creating Orders (With Hierarchy Linking)

**Context:**
Creating an Order differs from creating a Supplier because it involves **Hierarchy Linking**.
Every Order must be assigned to exactly one parent Supplier.

* **Method:** `createSamples`
* **DTO:** `SampleCreation`
* **Mandatory Identifiers:** Space, Project, Experiment, Type.
* **Code Generation Strategy:** `BESTELLUNG-<UUID>` (e.g. `BESTELLUNG-98a76s...`).

**Assignment Strategy:**
* **Space:** Configured Default Space
* **Project:** Configured Order Project (e.g. `BESTELLUNGEN`)
* **Experiment:** **Configurable Collection** (e.g. `/LIEFERANTENBEWERTUNG/BESTELLUNGEN/BESTELLUNGEN`).
  Controlled via `OPENBIS_ORDER_COLLECTION`.

**Parent Linking Strategy (Critical):**
To link the Order to a Supplier, the `parentIds` list in the JSON payload must contain the
`SamplePermId` of the target Supplier.

**Property Mapping:**

| API DTO Field   | openBIS Property Code    | Mandatory |
|:----------------|:-------------------------|:----------|
| `name`          | `$NAME`                  | **Yes**   |
| `mainCategory`  | `BESTELLUNG_HK`          | No        |
| `subCategory`   | `BESTELLUNG_UK`          | No        |
| `details`       | `BEZEICHNUNG`            | No        |
| `frequency`     | `RYTHMUS`                | No        |
| `contactPerson` | `ANSPRECHPERSON`         | No        |
| `contactEmail`  | `ANSPRECHPERSON_EMAIL`   | No        |
| `contactPhone`  | `ANSPRECHPERSON_TELEFON` | No        |
| `reason`        | `BESCHAFFUNGSGRUND`      | No        |
| `orderMethod`   | `BESTELLART`             | No        |
| `orderedBy`     | `BESTELLER`              | No        |
| `orderDate`     | `BESTELLDATUM`           | No        |
| `deliveryDate`  | `LIEFERDATUM`            | No        |
| `orderComment`  | `KOMMENTAR`              | No        |

**Note about the `supplierId` field:**
The `supplierId` field in `OrderCreationDto` is also **mandatory**, but it is not listed in the table above
because it does not map to an openBIS property. Instead, it is used to construct the `parentIds` array in the
`createSamples` request, linking the created order sample to its parent supplier sample (see the `parentIds`
example in the JSON payload below).

**Reference JSON Payload:**

```json
{
  "jsonrpc": "2.0",
  "method": "createSamples",
  "id": "req-create-order-01",
  "params": [
    "SESSION_TOKEN",
    [
      {
        "@type": "as.dto.sample.create.SampleCreation",
        "spaceId": {
          "@type": "as.dto.space.id.SpacePermId",
          "permId": "LIEFERANTENBEWERTUNG"
        },
        "projectId": {
          "@type": "as.dto.project.id.ProjectIdentifier",
          "identifier": "/LIEFERANTENBEWERTUNG/BESTELLUNGEN"
        },
        "experimentId": {
          "@type": "as.dto.experiment.id.ExperimentIdentifier",
          "identifier": "/LIEFERANTENBEWERTUNG/BESTELLUNGEN/BESTELLUNGEN"
        },
        "typeId": {
          "@type": "as.dto.entitytype.id.EntityTypePermId",
          "permId": "BESTELLUNG"
        },
        "code": "BESTELLUNG-a1b2c3d4-e5f6-...",
        "parentIds": [
           {
             "@type": "as.dto.sample.id.SamplePermId",
             "permId": "20251215211254413-254" 
           }
        ],
        "properties": {
          "$NAME": "Lab material Q1",
          "BESTELLUNG_HK": "BESCHAFFUNG",
          "BESTELLUNG_UK": "MESSMITTEL",
          "BEZEICHNUNG": "50x Precision scales",
          "RYTHMUS": "One-time",
          "ANSPRECHPERSON": "John Doe",
          "ANSPRECHPERSON_EMAIL": "john.doe@example.com",
          "ANSPRECHPERSON_TELEFON": "+1234567890",
          "BESCHAFFUNGSGRUND": "New project requirements",
          "BESTELLART": "Online",
          "BESTELLER": "Jane Smith",
          "BESTELLDATUM": "2023-01-01",
          "LIEFERDATUM": "2023-01-15",
          "KOMMENTAR": "Please ship to warehouse A"
        }
      }
    ]
  ]
}
```

### 6.3. Creating Ratings (Grandchild Entities)

**Context:**
Ratings represent the leaf nodes of the hierarchy (`Supplier` &rarr; `Order` &rarr; `Rating`).
Creating a rating involves linking it to a parent **Order**.

* **Method:** `createSamples`
* **DTO:** `SampleCreation`
* **Mandatory Identifiers:** Space, Project, Experiment, Type.
* **Code Generation Strategy:** `BEWERTUNG-<UUID>` (e.g. `BEWERTUNG-7f8e9d...`).

**Assignment Strategy:**
* **Space:** Configured Default Space
* **Project:** Configured Rating Project (e.g. `BEWERTUNGEN`)
* **Experiment:** **Configurable Collection** (e.g. `/LIEFERANTENBEWERTUNG/BEWERTUNGEN/BEWERTUNGEN`).
  Controlled via `OPENBIS_RATING_COLLECTION`.

**Logic & Calculation:**
1.  **Hierarchy:** The `parentIds` list must contain the `SamplePermId` of the target Order.
2.  **Calculated Field:** The `GESAMTBEWERTUNG` (Total Score) is **not** provided by the frontend.
    The Backend calculates the average of the provided scores (Quality, Cost, Reliability, Availability)
    and maps it to this property before sending the request to openBIS.

**Property Mapping:**

| API DTO Field        | openBIS Property Code        | Mandatory | logic/values  |
|:---------------------|:-----------------------------|:----------|:--------------|
| `quality`            | `QUALITAET`                  | **Yes**   | 1-5           |
| `qualityReason`      | `GRUND_QUALITAET`            | **Yes**   |               |
| `cost`               | `KOSTEN`                     | **Yes**   | 1-5           |
| `costReason`         | `KOSTEN_BEGRUENDUNG`         | **Yes**   |               |
| `reliability`        | `EINHALTUNG_TERMINE`         | **Yes**   | 1-5           |
| `reliabilityReason`  | `EINHALTUNG_TERMINE_GRUND`   | **Yes**   |               |
| `availability`       | `VERFUEGBARKEIT`             | No        | 1-5           |
| `availabilityReason` | `VERFUEGBARKEIT_BEGRUENDUNG` | No        |               |
| `ratingComment`      | `KOMMENTAR`                  | No        |               |
| *(Calculated)*       | `GESAMTBEWERTUNG`            | **Yes**   | Avg of scores |

**Reference JSON Payload:**

```json
{
  "jsonrpc": "2.0",
  "method": "createSamples",
  "id": "req-create-rating-01",
  "params": [
    "SESSION_TOKEN",
    [
      {
        "@type": "as.dto.sample.create.SampleCreation",
        "spaceId": {
          "@type": "as.dto.space.id.SpacePermId",
          "permId": "LIEFERANTENBEWERTUNG"
        },
        "projectId": {
          "@type": "as.dto.project.id.ProjectIdentifier",
          "identifier": "/LIEFERANTENBEWERTUNG/BEWERTUNGEN"
        },
        "experimentId": {
          "@type": "as.dto.experiment.id.ExperimentIdentifier",
          "identifier": "/LIEFERANTENBEWERTUNG/BEWERTUNGEN/BEWERTUNGEN"
        },
        "typeId": {
          "@type": "as.dto.entitytype.id.EntityTypePermId",
          "permId": "BESTELLBEWERTUNG"
        },
        "code": "BEWERTUNG-b34c5d6e-...",
        "parentIds": [
          {
            "@type": "as.dto.sample.id.SamplePermId",
            "permId": "20260102115225220-324"
          }
        ],
        "properties": {
          "QUALITAET": "1",
          "GRUND_QUALITAET": "Poor quality",
          "KOSTEN": "5",
          "KOSTEN_BEGRUENDUNG": "Free of charge",
          "EINHALTUNG_TERMINE": "5",
          "EINHALTUNG_TERMINE_GRUND": "On time",
          "VERFUEGBARKEIT": "1",
          "VERFUEGBARKEIT_BEGRUENDUNG": "Contact Person was not available",
          "GESAMTBEWERTUNG": "3.0",
          "KOMMENTAR": "Mixed experience"
        }
      }
    ]
  ]
}
```

## 7. Update Operations

### 7.1. Updating Suppliers

**Context:**
To update existing entities (e.g., when a supplier moves to a new address), the `updateSamples` method is used.
This operation allows modifying specific properties without recreating the sample.

* **Method:** `updateSamples`
* **DTO:** `SampleUpdate` (`as.dto.sample.update.SampleUpdate`)
* **Identifier:** The `sampleId` (PermID) is mandatory to identify the target.

**Behavior:**
* **Partial Update:** Only the properties included in the `properties` map are updated. Omitted properties remain unchanged.

**Reference JSON Payload:**

```json
{
  "jsonrpc": "2.0",
  "method": "updateSamples",
  "id": "req-update-supplier-01",
  "params": [
    "SESSION_TOKEN",
    [
      {
        "@type": "as.dto.sample.update.SampleUpdate",
        "sampleId": {
          "@type": "as.dto.sample.id.SamplePermId",
          "permId": "20251215211254413-199"
        },
        "properties": {
          "LIEFERANTEN_ORT": "Bern",
          "LIEFERANTEN_POSTLEITZAHL": "3000"
        }
      }
    ]
  ]
}
```

### 7.2. Updating Orders

**Context:**
Updates specific properties of an Order (e.g. status, details, dates).
**Crucial:** This operation updates properties *without* modifying the parent-child relationships
(Supplier link). The `supplierId` is implicitly preserved because the `parents` field is omitted in the update payload.

* **Method:** `updateSamples`
* **DTO:** `SampleUpdate`
* **Identifier:** The `sampleId` (PermID) is mandatory.

**Reference JSON Payload:**

```json
{
  "jsonrpc": "2.0",
  "method": "updateSamples",
  "id": "req-update-order-01",
  "params": [
    "SESSION_TOKEN",
    [
      {
        "@type": "as.dto.sample.update.SampleUpdate",
        "sampleId": {
          "@type": "as.dto.sample.id.SamplePermId",
          "permId": "20260102115225220-324"
        },
        "properties": {
          "$NAME": "Updated Order Name",
          "KOMMENTAR": "Updated status via API",
          "LIEFERDATUM": "2026-05-20"
        }
      }
    ]
  ]
}
```
