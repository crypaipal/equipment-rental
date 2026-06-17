# Gotowe testy

Stan dokumentu: 2026-06-15.

## Uruchamianie

```powershell
.\mvnw.cmd test
```

Ostatnia weryfikacja pelnego zestawu: 68 testow, 0 bledow.

## Podsumowanie

| Obszar                              | Klasa testowa | Liczba testow | Status |
|-------------------------------------| --- | ---: | --- |
| Smoke test aplikacji                | `EquipmentRentalApplicationTests` | 1 | Gotowe |
| Architektura heksagonalna           | `HexagonalArchitectureTest` | 8 | Gotowe |
| PU-11 Rejestracja uzytkownika       | `RegisterUserServiceTest` | 4 | Gotowe |
| PU-12 Logowanie uzytkownika         | `LoginUserServiceTest` | 4 | Gotowe |
| PU-01 Zlozenie rezerwacji           | `RequestReservationServiceTest` | 4 | Gotowe |
| PU-02 Wydanie sprzetu               | `CheckoutEquipmentServiceTest` | 5 | Gotowe |
| PU-03 Zwrot sprzetu                 | `ReturnEquipmentServiceTest` | 4 | Gotowe |
| PU-04 Wyszukanie dostepnego sprzetu | `FindAvailableEquipmentServiceTest` | 3 | Gotowe |
| PU-05 Rozpatrzenie rezerwacji       | `ReviewReservationServiceTest` | 3 | Gotowe |
| PU-07 Rejestracja modelu sprzetu    | `RegisterEquipmentModelServiceTest` | 2 | Gotowe |
| PU-08 Rejestracja assetu            | `RegisterAssetServiceTest` | 3 | Gotowe |
| PU-09 Zmiana stanu technicznego     | `ChangeAssetConditionServiceTest` | 5 | Gotowe |
| PU-10 Zgloszenie uszkodzenia        | `ReportAssetDamageServiceTest` | 2 | Gotowe |
| PU-06 Anulowanie rezerwacji         | `CancelReservationServiceTest` | 4 | Gotowe |
| PU-13 Blokada konta                 | `LockUserAccountServiceTest` | 4 | Gotowe |
| PU-13 Obsluga opoznionego zwrotu    | `RentalOverdueEventHandlerTest` | 2 | Gotowe |
| Adapter REST Identity               | `IdentityControllerTest` | 2 | Gotowe |
| Adapter REST Inventory              | `AssetControllerTest` | 3 | Gotowe |
| Adapter REST Rental                 | `ReservationControllerTest` | 3 | Gotowe |
| Adapter JPA Identity                | `UserRepositoryAdapterTest` | 2 | Gotowe |

## Pokryte scenariusze

| PU / Obszar                                    | Pokryte scenariusze |
|------------------------------------------------| --- |
| Architektura                                   | Domena bez zaleznosci od frameworkow, domena bez infrastruktury, aplikacja bez infrastruktury, rdzenie kontekstow bez bezposrednich zaleznosci miedzy kontekstami, porty jako interfejsy, kontrolery w infrastrukturze, encje JPA w persistence. |
| PU-11 RegisterUser                             | Poprawna rejestracja, domyslna rola `BORROWER`, duplikat e-maila, za krotkie haslo, publikacja `UserRegisteredEvent`. |
| PU-12 LoginUser                                | Poprawny login i utworzenie sesji, bledne haslo, blokada po trzecim bledzie, odrzucenie zablokowanego konta. |
| PU-01 RequestReservation                       | Poprawna rezerwacja, publikacja `ReservationRequestedEvent`, brak uprawnien uzytkownika, niedostepny sprzet, konflikt terminu. |
| PU-02 CheckoutEquipment                        | Wydanie zatwierdzonej rezerwacji, utworzenie aktywnego `Rental`, status `FULFILLED`, publikacja `EquipmentCheckedOutEvent`, brak rezerwacji, niedostepny sprzet, blokada statusow `PENDING` i `CANCELLED`. |
| PU-03 ReturnEquipment                          | Zwrot w terminie, zwrot po terminie z `RentalOverdueEvent`, zwrot uszkodzonego sprzetu z `EquipmentReturnedWithDamageEvent`, walidacja wymaganego opisu usterki, publikacja `EquipmentReturnedEvent`. |
| PU-04 FindAvailableEquipment                   | Filtrowanie dostepnych assetow po kategorii i okresie, wykluczenie assetow zarezerwowanych w wybranym okresie, pusta lista gdy wszystko jest zarezerwowane, walidacja pustej kategorii. |
| PU-05 ReviewReservation                        | Zatwierdzenie rezerwacji, odrzucenie z powodem, publikacja `ReservationApprovedEvent`, publikacja `ReservationRejectedEvent`, brak rezerwacji. |
| PU-07 RegisterEquipmentModel                   | Rejestracja modelu sprzetu, publikacja `ModelRegisteredEvent`, odrzucenie duplikatu nazwy i producenta bez zapisu i bez eventu. |
| PU-08 RegisterAsset                            | Rejestracja assetu w stanie `OPERATIONAL`, trimowanie numeru inwentarzowego, publikacja `AssetRegisteredEvent`, brak modelu, duplikat inventory tag bez zapisu i bez eventu. |
| PU-09 ChangeAssetCondition / ReportAssetDamage | Zmiana stanu na `DAMAGED`, zmiana stanu na `IN_REPAIR`, przywrocenie `OPERATIONAL`, publikacja `AssetDamagedEvent`, publikacja `AssetRepairedEvent`, zgloszenie uszkodzenia, brak assetu, walidacja pustego stanu. |
| PU-06 CancelReservation                        | Anulowanie rezerwacji `PENDING`, anulowanie rezerwacji `APPROVED`, publikacja `ReservationCancelledEvent`, blokada anulowania `FULFILLED`, brak rezerwacji. |
| PU-13 LockUserAccount                          | Blokada aktywnego konta, powod podany w commandzie, domyslny powod dla pustego commandu, publikacja `UserBlockedEvent`, idempotentnosc dla juz zablokowanego konta, brak uzytkownika. |
| PU-13 RentalOverdueEventHandler                | Reakcja na `RentalOverdueEvent`, przekazanie `userId` do `LockUserAccountUseCase`, staly powod blokady dla opoznionego zwrotu, deklaracja obslugiwanego typu eventu. |
| Adaptery REST                                  | Mapowanie requestow i odpowiedzi dla rejestracji/logowania, rejestracji assetu, zmiany stanu assetu, zlozenia rezerwacji i zatwierdzenia rezerwacji; sprawdzenie guardow roli i blokady rezerwacji na cudze konto. |
| Adaptery JPA                                   | Podstawowe mapowanie `UserRepositoryAdapter`: zapis domeny do encji JPA i powrot do domeny, wyszukiwanie po znormalizowanym e-mailu. |

## Zakres zamkniety w tym etapie

Pokryto wszystkie PU z mapy oraz podstawowe adaptery REST/JPA. Glebsze testy web slice z `MockMvc` i integracyjne testy repozytoriow z migracjami Flyway wymagaja osobnej konfiguracji bazy testowej.
