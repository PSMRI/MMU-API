# CLAUDE.md - MMU-API

## Project Overview

MMU-API is the backend service for AMRIT's Mobile Medical Unit operations. It supports the full clinical workflow: patient registration, nurse assessments (vitals, history), doctor consultations across multiple visit types (ANC, PNC, NCD Care, NCD Screening, General OPD, Cancer Screening, COVID-19, Quick Consult), lab technician workflows, pharmacist dispensing, teleconsultation, and offline data synchronization.

## Tech Stack

- Java 17, Spring Boot 3.2.2, Spring Data JPA, Hibernate
- MySQL 8.0 (via mysql-connector-j)
- Redis (session management)
- Spring AOP (cross-cutting concerns)
- MapStruct (object mapping), Lombok
- Swagger/OpenAPI (springdoc-openapi)
- WAR packaging for Wildfly deployment
- JaCoCo (coverage), Checkstyle (style)

## Build & Run

```bash
mvn clean install -DENV_VAR=local          # Build
mvn spring-boot:run -DENV_VAR=local        # Run locally
mvn -B package --file pom.xml -P <profile> # Package WAR (dev, local, test, ci, uat)
mvn test                                    # Run tests
```

Environment is set via `-DENV_VAR=<env>` which selects `common_<env>.properties`.

## Package Structure

Base package: `com.iemr.mmu`

| Package | Purpose |
|---------|---------|
| `controller/` | REST controllers organized by clinical role/visit type |
| `service/` | Business logic layer |
| `repo/` | JPA repositories |
| `data/` | JPA entity classes + master data entities |
| `config/` | Spring configuration |
| `utils/` | Cross-cutting: AES encryption, Redis, HTTP clients, file handling, validators |

## Key Domains / Controllers

### Clinical Workflow
- **RegistrarController** - Patient registration and demographic capture
- **ANCController** - Antenatal care visits
- **PostnatalCareController** - Postnatal care visits
- **NCDCareController** - Non-communicable disease care
- **NCDController** - NCD screening
- **GeneralOPDController** - General outpatient visits
- **CancerScreeningController** - Cancer screening visits
- **CovidController** - COVID-19 screening
- **QuickConsultController** - Quick consultations

### Support Services
- **AnthropometryVitalsController** - Vitals and anthropometry data
- **LabTechnicianController** - Lab test orders and results
- **TeleConsultationController** - Teleconsultation requests and responses
- **CommonMasterController** - Shared master data
- **CommonController** / **InsertCommonController** - Common data operations
- **SnomedController** - SNOMED CT terminology lookup
- **LocationController** - Location master data
- **IemrMmuLoginController** - Authentication

### Data Sync
- **FileSyncController** - Offline file synchronization
- `service/dataSyncActivity/` - Offline-to-central data sync
- `service/dataSyncLayerCentral/` - Central-side sync processing

## Architecture Notes

- Standard layered architecture: Controller -> Service -> Repository -> Entity
- Extensive master data model under `data/masterdata/` organized by clinical domain (anc, pnc, nurse, doctor, registrar, ncdcare, ncdscreening)
- Offline sync is a core feature: MMUs operate in low-connectivity areas, with data synced when online
- AES encryption utilities in `utils/AESEncryption/`
- Validator utilities in `utils/validator/` for request validation
- Session management via Redis (`utils/redis/`)
- Beneficiary flow status tracked in `data/benFlowStatus/` and `service/benFlowStatus/`
- Visit-type-specific service classes handle the distinct clinical data models for each visit type
