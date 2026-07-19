\# DECISIONS.md — Scelte progettuali



\## 1. Architettura



Architettura a 3 layer classica:



\- \*\*Presentazione\*\*: controller REST (Spring Web) + Swagger/OpenAPI per la documentazione

\- \*\*Business\*\*: service layer, contiene la logica di dominio (generazione codici, regola di stato, KPI, import seed)

\- \*\*Persistenza\*\*: Spring Data JPA + repository, MySQL come database



Backend Spring Boot 3.4.2 (Java 17), frontend Vue 3 con Composition API, containerizzati con Docker Compose (MySQL + backend + frontend con nginx).



\## 2. Interpretazione della regola di business 



Requisito: "I biglietti in stato SCADUTO non possono passare in stato VENDUTO".



Implementazione: controllo esplicito nel service (`TicketService.updateStatus`), che lancia `InvalidStatusException` (mappata a HTTP 409 Conflict) se lo stato corrente è SCADUTO.



\## 3. Regole di pulizia del dataset seed



Approccio adottato: \*\*validazione rigorosa, scarto totale delle righe non perfettamente valide\*\*, senza correzioni euristiche o normalizzazioni automatiche. Ogni riga scartata viene tracciata in una tabella dedicata (`seed\_import\_error`) con il motivo (o motivi, concatenati per non duplicare righe) dello scarto.



Una riga viene scartata se anche una sola di queste condizioni è vera:



\- `ticket\_id` mancante

\- `private\_code` mancante

\- `ticket\_id` duplicato nel file (tutte le occorrenze vengono scartate, non solo le successive)

\- `private\_code` duplicato nel file

\- `status` mancante o non riconducibile a uno dei 4 valori validi (dopo trim/uppercase)

\- `created\_at`, `extract\_at` o `updated\_at` mancante o non parsabile in formato ISO valido

\- `extract\_at` antecedente a `created\_at`

\- `updated\_at` antecedente a `created\_at`



Motivazione: in assenza di certezza sul dato corretto, ho preferito scartare piuttosto che indovinare (es. non ho provato a "correggere" una data in formato diverso o un duplicato scegliendo quale riga tenere, per evitare di introdurre dati potenzialmente sbagliati con falsa sicurezza).



Risultato sul dataset fornito (505 righe): 466 importate, 39 scartate.



\## 4. Sicurezza



\- \*\*Autenticazione\*\*: Basic Auth su tutti gli endpoint `/api/\*\*`, con un singolo utente amministrativo. 

\- \*\*Credenziali\*\*: mai hardcoded nel codice sorgente. Lette da variabili d'ambiente (`ADMIN\_USERNAME`, `ADMIN\_PASSWORD`, credenziali DB), senza valori di default in chiaro nei file versionati.

\- \*\*Password\*\*: codificata con `BCryptPasswordEncoder`.

\- \*\*CORS\*\*: configurato esplicitamente per accettare richieste solo dall'origine del frontend.



\## 5. Trade-off consapevoli



\- \*\*`ddl-auto: update`\*\* 

\- \*\*Query con metodi derivati Spring Data\*\* (`findByStatus`, `findByCreatedAtBetween`, ecc.) :ogni combinazione di filtro è esplicita e leggibile.



\## 6. Estensioni oltre i requisiti minimi



\- Audit trail completo (`ticket\_audit`) su ogni operazione (create, update, delete, scadenza automatica, import seed), con tracciamento di chi/cosa ha generato il cambiamento (`source`: USER, SCHEDULER, SEED).

\- Tracciamento strutturato degli errori di import del seed (`seed\_import\_error`).

\- Pipeline CI (GitHub Actions) che esegue i test automaticamente ad ogni push (implementati 6 test).

\- Frontend Vue funzionante (non solo mockup): lista biglietti con filtri e CRUD, dashboard KPI con grafico.



\## 7. Cosa avrei fatto con più tempo



\- Gestione dedicata di `MethodArgumentNotValidException` nel `GlobalExceptionHandler`, per messaggi di validazione più chiari (attualmente Spring restituisce comunque 400 ma con formato di default).

\- Autenticazione JWT invece di Basic Auth.

\- Combinazione ricerca aggiungendo `ticketId` lato backend 

\- Login Frontend

\- Pagina di upload file excel lato Frontend, api upload file lato backend

\- Deploy su infrastruttura cloud (es. AWS ).



