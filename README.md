# AMRIT - Mobile Medical Unit (MMU) Service

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) ![branch parameter](https://github.com/PSMRI/MMU-API/actions/workflows/sast-and-package.yml/badge.svg)

The AMRIT Mobile Medical Unit (MMU) service provides essential medical assistance to individuals without requiring them to be admitted to a hospital. This service operates through a mobile unit equipped with a laboratory for conducting medical tests and the capability to dispense medicines. The MMU employs an internet-connected device to collect and transmit medical data to the AMRIT application. It supports various medical standards and incorporates a feature that allows data capture and synchronization even when an internet connection is unavailable.

## Features

* **MMU - Mobile Medical Unit**: The MMU is a specialized vehicle or facility that delivers medical advice and services to individuals. It serves as a mobile clinic, capable of reaching diverse locations to provide medical support.

* **Outpatient Service**: The MMU offers medical care to individuals who visit the unit but do not require overnight hospitalization. It caters to those who need medical attention but can be treated without being admitted to a hospital.

* **Drug Dispensing**: The MMU can provide prescribed medications to patients. If a doctor prescribes medication, the MMU has the capability to dispense it to the patients.

* **Laboratory Facility**: The MMU is equipped with a laboratory where various medical tests can be conducted. These tests aid in diagnosing diseases or monitoring the health of patients.

* **IoT Device**: The MMU utilizes a specialized internet-connected device to collect data from different medical tests. This device supports around 20 lab tests, and the collected data is directly transmitted to the AMRIT application.

* **AMRIT Application**: The AMRIT application receives and manages the data obtained from lab tests conducted by the MMU. It serves as a central repository for storing and organizing patients' medical information.

* **SnomedCT, LOINC, ICD - 10, FHIR Compatible**: The MMU is compatible with various medical standards and systems used for classifying and organizing medical information. It can understand and utilize medical codes and terms employed in systems such as SnomedCT, LOINC, ICD - 10, and FHIR.

* **Offline Data Capture and Sync Feature**: The MMU includes a feature that enables the collection of medical information even when an internet connection is unavailable. This offline data capture is later synchronized and transferred to the AMRIT application when an internet connection becomes available.

## Building From Source

This microservice is developed using Java and the Spring Boot framework, with MySQL as the database.

### Prerequisites

Ensure that the following prerequisites are met before building the MMU service:

* JDK 1.8
* Maven
* Redis
* Spring Boot v2
* MySQL

### Installation

To install the MMU module, please follow these steps:

1. Clone the repository to your local machine.
2. Install the dependencies and build the module:
   - Run the command `mvn clean install`. 
3. You can copy `common_example.properties` to `common_local.properties` and edit the file accordingly. The file is under `src/main/environment` folder.
4. Run the development server:
   - Start the Redis server.
   - Run the command `mvn spring-boot:run -DENV_VAR=local`.
5. Open your browser and access `http://localhost:8080/swagger-ui.html#!/` to view the Swagger API documentation.

## Usage

All the features of the MMU service are exposed as REST endpoints. Please refer to the Swagger API specification for detailed information on how to utilize each feature.

The MMU module offers comprehensive management capabilities for your application.

## Filing Issues

If you encounter any issues, bugs, or have feature requests, please file them in the [main AMRIT repository](https://github.com/PSMRI/AMRIT/issues). Centralizing all feedback helps us streamline improvements and address concerns efficiently.  

## Join Our Community

We’d love to have you join our community discussions and get real-time support!  
Join our [Discord server](https://discord.gg/FVQWsf5ENS) to connect with contributors, ask questions, and stay updated.  
