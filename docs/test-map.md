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

## Przypadki uzycia ze strategii

| PU                           | Scenariusz testowy | Zakres | Priorytet |
|------------------------------| --- | --- | --- |
| PU-01 RequestReservation     | Sciezka pomyslna: uzytkownik bez blokady rezerwuje dostepny sprzet w wolnym terminie | Rental application, Identity ACL, Inventory port, ReservationRequestedEvent | Wysoki |
| PU-01 RequestReservation     | Sciezka alternatywna: sprzet jest juz zajety w wybranym okresie | Rental application, ReservationRepository | Wysoki |
| PU-02 CheckoutEquipment      | Wydanie zatwierdzonej rezerwacji tworzy aktywne wypozyczenie i oznacza rezerwacje jako zrealizowana | Rental application, EquipmentCheckedOutEvent | Wysoki |
| PU-02 CheckoutEquipment      | Proba wydania rezerwacji niezatwierdzonej albo anulowanej jest blokowana | Rental domain/application | Wysoki |
| PU-03 ReturnEquipment        | Sciezka glowna: zwrot w terminie zamyka wypozyczenie bez zdarzenia opoznienia i bez zdarzenia usterki | Rental application, EquipmentReturnedEvent | Wysoki |
| PU-03 ReturnEquipment        | Sciezka opoznienia: zwrot po terminie zamyka wypozyczenie i publikuje zdarzenie uruchamiajace blokade konta | Rental application, RentalOverdueEvent, Identity handler | Wysoki |
| PU-03 ReturnEquipment        | Sciezka usterki: zwrot uszkodzonego sprzetu zamyka wypozyczenie i zglasza usterke do Kontekstu Katalogu | Rental application, EquipmentReturnedWithDamageEvent, Inventory handler | Wysoki |
| PU-04 FindAvailableEquipment | Wyszukiwanie zwraca tylko sprzet sprawny i niezarezerwowany w wybranym okresie | Rental application, Inventory port, ReservationRepository | Wysoki |
| PU-05 ReviewReservation      | Zatwierdzenie oczekujacej rezerwacji zmienia status i publikuje ReservationApprovedEvent | Rental application/domain | Sredni |
| PU-05 ReviewReservation      | Odrzucenie oczekujacej rezerwacji zapisuje powod i publikuje ReservationRejectedEvent | Rental application/domain | Sredni |
| PU-07 RegisterEquipmentModel | Poprawne dane tworza model sprzetu i publikuja ModelRegisteredEvent | Inventory application/domain | Sredni |
| PU-08 RegisterAsset          | Unikalny numer inwentarzowy tworzy egzemplarz w stanie Operational i publikuje AssetRegisteredEvent | Inventory application/domain | Sredni |
| PU-08 RegisterAsset          | Duplikat numeru inwentarzowego jest odrzucany | Inventory application/domain | Sredni |
| PU-09 ChangeAssetCondition   | Zmiana stanu na Damaged albo InRepair wyklucza sprzet z dostepnosci | Inventory domain/application | Sredni |
| PU-11 ChangeAssetCondition   | Przywrocenie stanu Operational publikuje AssetRepairedEvent | Inventory application/domain | Sredni |
| PU-11 RegisterUser           | Rejestracja unikalnego e-maila tworzy konto i publikuje UserRegisteredEvent | Identity application/domain | Sredni |
| PU-11 RegisterUser           | Rejestracja na istniejacy e-mail jest odrzucana | Identity application/domain | Sredni |
| PU-12 LoginUser              | Poprawne dane logowania tworza sesje/token | Identity application/domain | Sredni |
| PU-12 LoginUser              | Trzy bledne hasla skutkuja czasowa blokada logowania | Identity domain/application | Sredni |
| PU-06 CancelReservation      | Anulowanie rezerwacji Pending albo Approved zmienia status i publikuje ReservationCancelledEvent | Rental application/domain | Sredni |
| PU-06 CancelReservation      | Anulowanie rezerwacji Fulfilled jest blokowane | Rental domain/application | Sredni |
| PU-13 LockUserAccount        | Odebranie RentalOverdueEvent blokuje niezablokowane konto i publikuje UserBlockedEvent | Identity event handler/application | Sredni |
| PU-13 LockUserAccount        | Ponowne odebranie zdarzenia dla juz zablokowanego konta nie powoduje bledu | Identity event handler/application | Niski |
