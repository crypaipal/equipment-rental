# Testing Map

This document tracks test coverage against the use cases and architectural requirements.

## Current Scope

The current implementation covers part of the Inventory Context:

- PU-06 Register equipment model
- PU-07 Register asset
- Part of PU-08 Change asset condition, currently only domain behavior

## Test Levels

- Domain unit tests: pure domain rules, no Spring, no database.
- Application unit tests: use case services with fake ports/adapters.
- Adapter integration tests: REST/JPA behavior with Spring and PostgreSQL.
- Architecture tests: dependency direction and hexagonal boundaries.

## Use Case Coverage

| Use Case | Requirement / Scenario | Test Level | Status | Test Class |
| --- | --- | --- | --- | --- |
| PU-06 | Register a new equipment model with valid data | Application unit | Done | `RegisterEquipmentModelServiceTest` |
| PU-06 | Reject duplicate equipment model name and manufacturer | Application unit | Done | `RegisterEquipmentModelServiceTest` |
| PU-07 | Register a new asset for an existing equipment model | Application unit | Done | `RegisterAssetServiceTest` |
| PU-07 | Reject asset registration when equipment model does not exist | Application unit | Done | `RegisterAssetServiceTest` |
| PU-07 | Reject duplicate inventory tag | Application unit | Done | `RegisterAssetServiceTest` |
| PU-07 | Inventory tag must be valid | Domain unit | Done | `InventoryTagTest` |
| PU-08 | Damaged asset is excluded from rental availability | Domain unit | Done | `AssetTest` |

## Next Testing Steps

1. Add REST adapter tests for `POST /api/inventory/models`.
2. Add REST adapter tests for `POST /api/inventory/assets`.
3. Add persistence adapter tests for unique constraints and mappings.
4. Add ArchUnit tests for hexagonal architecture boundaries.
5. Expand this map when Rental Context and Identity Context are implemented.
