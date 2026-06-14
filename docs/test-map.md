# Mapa testow

| Obszar | Zakres testowania | Typ testow | Priorytet |
| --- | --- | --- | --- |
| Rental domain | RentalPeriod, Reservation, Rental, OverduePolicy, RentalEligibilityRule | Jednostkowe domeny | Wysoki |
| Inventory domain | InventoryTag, Asset, AssetConditionRule, DamageReport, EquipmentModel | Jednostkowe domeny | Wysoki |
| Identity domain | Email, AccountLock, User, AuthSession | Jednostkowe domeny | Wysoki |
| Rental application | RequestReservation, ReviewReservation, CheckoutEquipment, ReturnEquipment, CancelReservation | Jednostkowe aplikacyjne z fake portami | Wysoki |
| Inventory application | RegisterEquipmentModel, RegisterAsset, ChangeAssetCondition, ReportAssetDamage | Jednostkowe aplikacyjne z fake repozytoriami | Sredni |
| Identity application | RegisterUser, LoginUser, LockUserAccount | Jednostkowe aplikacyjne z fake repozytoriami | Sredni |
| Domain events | Publikacja zdarzen po kluczowych przypadkach uzycia | Jednostkowe aplikacyjne | Sredni |
| REST adapters | Walidacja requestow, mapowanie DTO, kontrola rol | Integracyjne/web slice | Niski |
| Persistence adapters | Mapowanie JPA, repozytoria, migracje Flyway | Integracyjne | Niski |
| Architecture | Zaleznosci warstw, separacja domeny od infrastruktury | Testy architektury | Niski |
