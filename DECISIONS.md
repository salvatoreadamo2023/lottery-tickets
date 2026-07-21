# DECISIONS.md — Scelte progettuali



## 1. Architettura



Architettura a 3 layer classica:



**Presentazione**: controller REST (Spring Web) + Swagger/OpenAPI per la documentazione

**Business**: service layer, contiene la logica di dominio (generazione codici, regola di stato, KPI, import seed)

**Persistenza**: Spring Data JPA + repository, MySQL come database



Backend Spring Boot 3.4.2 (Java 17), frontend Vue 3 con Composition API, containerizzati con Docker Compose (MySQL + backend + frontend con nginx).



## 2. Interpretazione della regola di business 



Requisito: "I biglietti in stato SCADUTO non possono passare in stato VENDUTO".



Implementazione: controllo esplicito nel service (`TicketService.updateStatus`), che lancia `InvalidStatusException` (mappata a HTTP 409 Conflict) se lo stato corrente è SCADUTO.



## 3. Regole di pulizia del dataset seed



Approccio adottato: **validazione rigorosa, scarto totale delle righe non perfettamente valide**, senza correzioni euristiche o normalizzazioni automatiche. Ogni riga scartata viene tracciata in una tabella dedicata (`seed_import_error`) con il motivo (o motivi, concatenati per non duplicare righe) dello scarto.



Una riga viene scartata se anche una sola di queste condizioni è vera:



`ticket_id` mancante

`private_code` mancante

`ticket_id` duplicato nel file (tutte le occorrenze vengono scartate, non solo le successive)

`private_code` duplicato nel file

`status` mancante o non riconducibile a uno dei 4 valori validi (dopo trim/uppercase)

`created_at`, `extract_at` o `updated_at` mancante o non parsabile in formato ISO valido

`extract_at` antecedente a `created_at`

`updated_at` antecedente a `created_at`



Motivazione: in assenza di certezza sul dato corretto, ho preferito scartare piuttosto che indovinare (es. non ho provato a "correggere" una data in formato diverso o un duplicato scegliendo quale riga tenere, per evitare di introdurre dati potenzialmente sbagliati con falsa sicurezza).



Risultato sul dataset fornito (505 righe): 466 importate, 39 scartate.



## 4. Sicurezza



**Autenticazione**: Basic Auth su tutti gli endpoint `/api/**`, con un singolo utente amministrativo. 

**Credenziali**: mai hardcoded nel codice sorgente. Lette da variabili d'ambiente (`ADMIN_USERNAME`, `ADMIN_PASSWORD`, credenziali DB), senza valori di default in chiaro nei file versionati.

**Password**: codificata con `BCryptPasswordEncoder`.

**CORS**: configurato esplicitamente per accettare richieste solo dall'origine del frontend.



## 5. Trade-off consapevoli



**`ddl-auto: update`** invece di migrazioni versionate (Flyway/Liquibase): scelta rapida per lo sviluppo della prova, dato che il database parte sempre vuoto ad ogni avvio del container. In un contesto di produzione, con schema che evolve su dati già esistenti, andrebbe sostituito con migrazioni tracciate per maggiore controllo e sicurezza.

**Query con metodi derivati Spring Data** (`findByStatus`, `findByCreatedAtBetween`, ecc.) :ogni combinazione di filtro è esplicita e leggibile.



## 6. Estensioni oltre i requisiti minimi



Audit trail completo (`ticket_audit`) su ogni operazione (create, update, delete, scadenza automatica, import seed), con tracciamento di chi/cosa ha generato il cambiamento (`source`: USER, SCHEDULER, SEED).

Tracciamento strutturato degli errori di import del seed (`seed_import_error`).

- Pipeline CI (GitHub Actions) che esegue i test automaticamente ad ogni push (implementati 8 test):
  - `updateStatus_shouldThrowException_whenTicketIsScaduto` — verifica che un ticket SCADUTO non possa cambiare stato
  - `getTicket_shouldThrowException_whenTicketNotFound` — verifica gestione errore su ticket inesistente
  - `createTicket_shouldReturnBadRequest_whenExtractAtIsInThePast` — verifica validazione data di estrazione nel passato
  - `createTicket_shouldReturnCreated_whenExtractAtIsValid` — verifica creazione corretta con data valida
  - `createTicket_shouldReturnUnauthorized_whenNotAuthenticated` — verifica protezione endpoint senza autenticazione
  - `getAllTickets_shouldReturnOk_whenFilteringByDateRange` — verifica filtro per intervallo di date sulla lista ticket
  - `validateRowCompleto_shouldReturnNoErrors_whenRowIsClean` — verifica che una riga pulita del seed non generi errori
  - `validateRowCompleto_shouldReturnError_whenExtractAtIsBeforeCreatedAt` — verifica scarto di una riga con date incoerenti

Frontend Vue funzionante (non solo mockup): lista biglietti con filtri e CRUD, dashboard KPI con grafico.

## 7. Scelta del frontend: Vue invece di React/Mockup

Ho scelto Vue 3 (Composition API) principalmente per esperienza pregressa diretta con il framework in ambito lavorativo.
Ho ritenuto valida l'applicazione per il contesto della prova.

## 8. Cosa avrei fatto con più tempo



Gestione dedicata di `MethodArgumentNotValidException` nel `GlobalExceptionHandler`, per messaggi di validazione più chiari (attualmente Spring restituisce comunque 400 ma con formato di default).

Autenticazione JWT e/o OAuth2 invece di Basic Auth.

Combinazione ricerca aggiungendo `ticketId` lato backend 

Login Frontend

Pagina di upload file excel lato Frontend, api upload file lato backend

Deploy su infrastruttura cloud (es. AWS ).



