# Gotowe testy

Stan dokumentu: 2026-06-14.

## Uruchamianie

```powershell
.\mvnw.cmd test
```

Ostatnia weryfikacja pelnego zestawu: 41 testow, 0 bledow.

## Podsumowanie

| Obszar | Klasa testowa | Liczba testow | Status |
| --- | --- | ---: | --- |
| Smoke test aplikacji | `EquipmentRentalApplicationTests` | 1 | Gotowe |
| Architektura heksagonalna | `HexagonalArchitectureTest` | 8 | Gotowe |
| PU-09 Rejestracja uzytkownika | `RegisterUserServiceTest` | 4 | Gotowe |
| PU-10 Logowanie uzytkownika | `LoginUserServiceTest` | 4 | Gotowe |
| PU-01 Zlozenie rezerwacji | `RequestReservationServiceTest` | 4 | Gotowe |
| PU-02 Wydanie sprzetu | `CheckoutEquipmentServiceTest` | 5 | Gotowe |
| PU-03 Zwrot sprzetu | `ReturnEquipmentServiceTest` | 4 | Gotowe |
| PU-04 Wyszukanie dostepnego sprzetu | `FindAvailableEquipmentServiceTest` | 3 | Gotowe |
| PU-05 Rozpatrzenie rezerwacji | `ReviewReservationServiceTest` | 3 | Gotowe |
| PU-06 Rejestracja modelu sprzetu | `RegisterEquipmentModelServiceTest` | 2 | Gotowe |
| PU-07 Rejestracja assetu | `RegisterAssetServiceTest` | 3 | Gotowe |

## Pokryte scenariusze

| PU / Obszar | Pokryte scenariusze |
| --- | --- |
| Architektura | Domena bez zaleznosci od frameworkow, domena bez infrastruktury, aplikacja bez infrastruktury, rdzenie kontekstow bez bezposrednich zaleznosci miedzy kontekstami, porty jako interfejsy, kontrolery w infrastrukturze, encje JPA w persistence. |
| PU-09 RegisterUser | Poprawna rejestracja, domyslna rola `BORROWER`, duplikat e-maila, za krotkie haslo, publikacja `UserRegisteredEvent`. |
| PU-10 LoginUser | Poprawny login i utworzenie sesji, bledne haslo, blokada po trzecim bledzie, odrzucenie zablokowanego konta. |
| PU-01 RequestReservation | Poprawna rezerwacja, publikacja `ReservationRequestedEvent`, brak uprawnien uzytkownika, niedostepny sprzet, konflikt terminu. |
| PU-02 CheckoutEquipment | Wydanie zatwierdzonej rezerwacji, utworzenie aktywnego `Rental`, status `FULFILLED`, publikacja `EquipmentCheckedOutEvent`, brak rezerwacji, niedostepny sprzet, blokada statusow `PENDING` i `CANCELLED`. |
| PU-03 ReturnEquipment | Zwrot w terminie, zwrot po terminie z `RentalOverdueEvent`, zwrot uszkodzonego sprzetu z `EquipmentReturnedWithDamageEvent`, walidacja wymaganego opisu usterki, publikacja `EquipmentReturnedEvent`. |
| PU-04 FindAvailableEquipment | Filtrowanie dostepnych assetow po kategorii i okresie, wykluczenie assetow zarezerwowanych w wybranym okresie, pusta lista gdy wszystko jest zarezerwowane, walidacja pustej kategorii. |
| PU-05 ReviewReservation | Zatwierdzenie rezerwacji, odrzucenie z powodem, publikacja `ReservationApprovedEvent`, publikacja `ReservationRejectedEvent`, brak rezerwacji. |
| PU-06 RegisterEquipmentModel | Rejestracja modelu sprzetu, publikacja `ModelRegisteredEvent`, odrzucenie duplikatu nazwy i producenta bez zapisu i bez eventu. |
| PU-07 RegisterAsset | Rejestracja assetu w stanie `OPERATIONAL`, trimowanie numeru inwentarzowego, publikacja `AssetRegisteredEvent`, brak modelu, duplikat inventory tag bez zapisu i bez eventu. |

## Jeszcze niepokryte

| PU / Obszar | Brakujace testy |
| --- | --- |
| PU-08 ChangeAssetCondition / ReportAssetDamage | Zmiana na `DAMAGED`, `IN_REPAIR`, `OPERATIONAL`, eventy `AssetDamagedEvent` i `AssetRepairedEvent`. |
| PU-11 CancelReservation | Anulowanie `PENDING` i `APPROVED`, blokada `FULFILLED`, publikacja `ReservationCancelledEvent`. |
| PU-12 LockUserAccount | Reakcja na `RentalOverdueEvent`, idempotentnosc dla juz zablokowanego konta, publikacja `UserBlockedEvent`. |
| Adaptery REST/JPA | Testy web slice, mapowania DTO, repozytoria i migracje. |
