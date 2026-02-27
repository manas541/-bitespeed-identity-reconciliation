# Bitespeed Backend Task — Identity Reconciliation

This project implements the Identity Reconciliation service required by Bitespeed.

The system identifies and links multiple contact records belonging to the same customer using email and phone number matching.


## Live API

POST Endpoint: 


## Problem Statement

Customers may place orders using different emails or phone numbers.

The system must:
- Identify whether contacts belong to the same person
- Maintain a single primary contact
- Link all other contacts as secondary
- Return consolidated identity information


## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA (Hibernate)
- PostgreSQL
- Maven

