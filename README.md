<div align="center">

# 📅 StworzGrafik

### Automatyczne generowanie grafików pracy dla sieci sklepów

**Backend scheduling engine built with Java 21 & Spring Boot**

[![Live Demo](https://img.shields.io/badge/Live%20Demo-stworzgrafik.online-brightgreen)](https://stworzgrafik.online)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![Kafka](https://img.shields.io/badge/Apache-Kafka-black)

🇵🇱 [Polski](#-opis-pl) • 🇬🇧 [English](#-description-en)

</div>

---

# 🇵🇱 Opis PL

## O projekcie

**StworzGrafik** to aplikacja webowa do automatycznego generowania miesięcznych grafików pracy dla sklepów oraz większych struktur organizacyjnych.

Backend został napisany w **Java 21 + Spring Boot** i udostępnia REST API obsługujące cały proces planowania pracy:

**zapotrzebowanie sklepu → dostępność pracowników → ograniczenia → generowanie → analiza → eksport grafiku**

Najważniejszym elementem projektu jest rozwijany przeze mnie **autorski algorytm generowania grafików**, który analizuje zapotrzebowanie godzinowe sklepu i przypisuje pracowników do zmian z uwzględnieniem m.in. ich ról, wymiaru czasu pracy, dostępności, urlopów, preferencji oraz wymaganych okresów odpoczynku.

Aplikacja jest dostępna online:

👉 **https://stworzgrafik.online**

Repozytorium zawiera backend aplikacji. Frontend rozwijany jest w osobnym repozytorium przy użyciu **React + TypeScript + Vite**.

---

## 🧠 Generator grafików

Generator tworzy miesięczny grafik na podstawie danych zgromadzonych w systemie.

Algorytm uwzględnia m.in.:

* godzinowe zapotrzebowanie sklepu,
* miesięczny wymiar czasu pracy pracowników,
* pracowników zatrudnionych w niepełnym wymiarze,
* role i kompetencje pracowników,
* możliwość otwierania i zamykania sklepu,
* obsługę kasy,
* obsługę finansowania,
* urlopy,
* proponowane dni wolne,
* preferowane godziny pracy,
* delegacje pomiędzy sklepami,
* dni dostaw,
* godziny otwarcia sklepu,
* minimalne okresy odpoczynku,
* ograniczenia dotyczące liczby godzin pracy.

Przed rozpoczęciem generowania dane wymagane dla danego miesiąca są pobierane z bazy i przygotowywane w strukturach pamięciowych, ograniczając liczbę zapytań wykonywanych podczas pracy algorytmu.

Po wygenerowaniu harmonogramu system zapisuje również komunikaty dotyczące potencjalnych problemów, np.:

* niedoboru pracowników,
* braku osoby mogącej otworzyć lub zamknąć sklep,
* problemów z obsadą określonych kompetencji,
* konfliktów wynikających z dostępności,
* problemów z realizacją zapotrzebowania.

---

## ✨ Najważniejsze funkcje

### 📊 Zarządzanie zapotrzebowaniem

Dla każdego dnia można określić godzinowe zapotrzebowanie na pracowników.

Dane te stanowią podstawę działania algorytmu generującego grafik.

### 👥 Zarządzanie pracownikami

System przechowuje informacje dotyczące m.in.:

* stanowiska,
* wymiaru czasu pracy,
* kompetencji,
* dostępności,
* dni wolnych,
* urlopów,
* delegacji,
* preferowanych zmian.

### 🏢 Struktura organizacyjna

System obsługuje hierarchię:

```text
Network
   └── Region
        └── Branch
             └── Store
                  └── Employee
```

Pozwala to na zarządzanie wieloma sklepami i różnymi poziomami dostępu.

### 🔐 Role i uprawnienia

Obsługiwane role użytkowników:

| Rola            | Dostęp                                           |
| --------------- | ------------------------------------------------ |
| `ADMIN`         | pełny dostęp do systemu                          |
| `DIRECTOR`      | dostęp do przypisanego Branch / Region / Network |
| `STORE_MANAGER` | dostęp do własnego sklepu                        |

Autoryzacja realizowana jest przy pomocy **Spring Security + JWT**.

Dostęp do zasobów jest dodatkowo kontrolowany na poziomie metod przy wykorzystaniu `@PreAuthorize`.

### 📤 Eksport

Wygenerowany grafik może zostać wyeksportowany do:

* Excel,
* PDF.

Pliki przechowywane są w **Cloudflare R2**, a backend generuje czasowe **presigned URLs** pozwalające na ich pobranie.

### 🚀 Tryb demo

Aplikację można przetestować bez zakładania własnego konta.

Backend może automatycznie utworzyć tymczasowe środowisko demonstracyjne wraz z danymi potrzebnymi do przetestowania najważniejszych funkcji systemu.

---

# 🛠 Stack technologiczny

| Obszar                | Technologia                 |
| --------------------- | --------------------------- |
| Język                 | **Java 21**                 |
| Backend               | **Spring Boot 3.5**         |
| REST API              | Spring Web                  |
| Security              | Spring Security + JWT       |
| ORM                   | Spring Data JPA / Hibernate |
| Baza danych           | **MySQL 8**                 |
| Migracje DB           | **Flyway**                  |
| Mapowanie DTO         | MapStruct                   |
| Messaging             | **Apache Kafka**            |
| Testy                 | JUnit 5, Mockito            |
| Excel                 | Apache POI                  |
| PDF                   | OpenPDF                     |
| Object Storage        | Cloudflare R2 / S3 API      |
| Build                 | Maven                       |
| Konteneryzacja        | Docker + Docker Compose     |
| CI/CD                 | GitHub Actions              |
| Reverse proxy / HTTPS | Caddy                       |
| Hosting               | Hetzner VPS                 |
| Code Quality          | JetBrains Qodana            |

Frontend:

```text
React
TypeScript
Vite
Tailwind CSS
```

---

# 🏗 Architektura

Backend został podzielony na moduły odpowiadające poszczególnym obszarom domenowym.

Przykładowe moduły:

```text
auth
user
region
branch
store
employee
shift
demand
schedule
billing
holiday
fileExport
demo
```

Typowy przepływ:

```text
HTTP Request
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
JPA / Hibernate
      ↓
MySQL 8
```

DTO są oddzielone od encji domenowych, a mapowanie pomiędzy nimi realizowane jest przy pomocy **MapStruct**.

---

## 🌐 Infrastruktura

Produkcja działa na VPS w **Hetzner**.

```text
                         Internet
                            │
                            ▼
                      stworzgrafik.online
                            │
                            ▼
                    ┌───────────────┐
                    │     Caddy     │
                    │     HTTPS     │
                    └───────┬───────┘
                            │
                ┌───────────┴───────────┐
                ▼                       ▼
           React frontend          Spring Boot
                                    REST API
                                       │
                        ┌──────────────┼──────────────┐
                        ▼              ▼              ▼
                     MySQL 8        Kafka       Cloudflare R2
```

Aplikacja uruchamiana jest za pomocą **Docker Compose**.

Deployment backendu jest automatyzowany przez **GitHub Actions**.

Typowy flow deploymentu:

```text
git push
   ↓
GitHub
   ↓
GitHub Actions
   ↓
tests / build
   ↓
Docker image
   ↓
deployment
   ↓
Hetzner VPS
   ↓
Docker Compose
```

---

# 🗄 Baza danych

Aplikacja wykorzystuje:

**MySQL 8**

Zmiany schematu bazy danych są wersjonowane przy pomocy:

**Flyway**

Migracje znajdują się w:

```text
src/main/resources/db/migration
```

Dzięki temu struktura bazy danych jest tworzona i aktualizowana automatycznie podczas uruchamiania aplikacji.

---

# 📨 Apache Kafka

Projekt wykorzystuje również **Apache Kafka** do obsługi zdarzeń aplikacyjnych.

Pozwala to oddzielać operacje wykonywane bezpośrednio podczas requestu HTTP od logiki reagującej na zdarzenia występujące w systemie.

Przykładowy przepływ:

```text
Spring Boot
    │
    │ publish event
    ▼
Kafka Producer
    │
    ▼
Kafka Topic
    │
    ▼
Kafka Consumer
    │
    ▼
Event Handler
```

Integracja z Kafką jest rozwijana wraz z kolejnymi funkcjami aplikacji.

---

# 🔐 Security

API wykorzystuje stateless authentication opartą o JWT.

```text
Login
  ↓
credentials validation
  ↓
JWT generation
  ↓
client
  ↓
Authorization: Bearer <token>
  ↓
JwtAuthenticationFilter
  ↓
Spring Security
  ↓
secured endpoint
```

Backend stosuje również autoryzację zależną od:

* roli użytkownika,
* przypisanego sklepu,
* zakresu organizacyjnego użytkownika.

---

# 🧪 Testy

Projekt wykorzystuje:

* **JUnit 5**
* **Mockito**
* testy warstwy serwisowej,
* testy logiki biznesowej.

Testy uruchamiane są również podczas procesu CI/CD.

```bash
./mvnw test
```

---

# 🚀 Uruchomienie lokalne

## Wymagania

* Java 21
* Docker + Docker Compose

lub:

* Java 21
* MySQL 8
* Maven

---

## Opcja 1 — Docker Compose

```bash
git clone https://github.com/makcc93/stworz-grafik-online.git

cd stworz-grafik-online

docker compose up -d --build
```

---

## Opcja 2 — uruchomienie przez Maven

```bash
git clone https://github.com/makcc93/stworz-grafik-online.git

cd stworz-grafik-online

./mvnw spring-boot:run
```

API dostępne jest domyślnie pod:

```text
http://localhost:8080
```

---

# ⚙️ Konfiguracja

Najważniejsze zmienne środowiskowe:

| Zmienna                               | Opis                   |
| ------------------------------------- | ---------------------- |
| `SPRING_DATASOURCE_URL`               | adres MySQL            |
| `SPRING_DATASOURCE_USERNAME`          | użytkownik MySQL       |
| `SPRING_DATASOURCE_PASSWORD`          | hasło MySQL            |
| `APPLICATION_SECURITY_JWT_SECRET_KEY` | klucz podpisujący JWT  |
| `APPLICATION_SECURITY_JWT_EXPIRATION` | czas ważności JWT      |
| `APP_ADMIN_LOGIN`                     | login administratora   |
| `APP_ADMIN_PASSWORD`                  | hasło administratora   |
| `APP_CORS_ALLOWED_ORIGINS`            | dozwolone originy      |
| `CLOUDFLARE_R2_ENDPOINT`              | endpoint Cloudflare R2 |
| `CLOUDFLARE_R2_ACCESS_KEY`            | access key R2          |
| `CLOUDFLARE_R2_SECRET_KEY`            | secret key R2          |
| `CLOUDFLARE_R2_BUCKET`                | bucket R2              |

Sekrety nie są przechowywane w repozytorium.

---

# 📡 API

Przykładowe obszary API:

| Obszar         | Endpoint                             |
| -------------- | ------------------------------------ |
| Authentication | `/api/auth/**`                       |
| Demo           | `/api/demo/**`                       |
| Users          | `/api/users/**`                      |
| Regions        | `/api/regions/**`                    |
| Branches       | `/api/branches/**`                   |
| Stores         | `/api/stores/**`                     |
| Employees      | `/api/stores/{storeId}/employees/**` |
| Demand         | `/api/stores/{storeId}/drafts/**`    |
| Schedules      | `/api/stores/{storeId}/schedules/**` |

Pełny kontrakt API nie jest publikowany w README.

---

# 🌐 Demo

Aplikacja działa online:

### 👉 https://stworzgrafik.online

Tryb demonstracyjny pozwala przetestować aplikację bez konfiguracji własnego środowiska.

---

# 📦 Frontend

Frontend aplikacji utrzymywany jest w oddzielnym repozytorium.

Technologie:

```text
React
TypeScript
Vite
Tailwind CSS
Framer Motion
```

Frontend komunikuje się z backendem poprzez REST API zabezpieczone JWT.

---

# 📜 Licencja

Kod źródłowy tego repozytorium jest publicznie dostępny przede wszystkim w celach **portfolio oraz demonstracji projektu**.

Projekt nie jest obecnie udostępniany na licencji open-source pozwalającej na jego swobodne kopiowanie, modyfikowanie lub wykorzystanie komercyjne.

**All rights reserved.**

---

# 👨‍💻 Autor

Projekt zaprojektowany i rozwijany przez:

**Mateusz Kruk**

GitHub: [@makcc93](https://github.com/makcc93)

---

# 🇬🇧 Description EN

## About

**StworzGrafik** is a web application for automatically generating monthly employee work schedules for retail stores and larger organizational structures.

The backend is built with **Java 21 and Spring Boot** and exposes a REST API responsible for the entire scheduling workflow:

```text
staffing demand
      ↓
employee availability
      ↓
business constraints
      ↓
schedule generation
      ↓
validation
      ↓
export
```

The core of the project is a custom scheduling algorithm that assigns employees to shifts based on hourly staffing requirements while taking into account:

* employee working-hour limits,
* part-time contracts,
* employee roles and skills,
* store opening and closing requirements,
* checkout availability,
* vacations,
* requested days off,
* preferred shifts,
* inter-store delegations,
* delivery days,
* store opening hours,
* required rest periods.

Live application:

👉 **https://stworzgrafik.online**

The repository contains the application backend.

The frontend is developed separately using **React, TypeScript and Vite**.

---

## ✨ Key Features

* 🧠 custom automatic scheduling engine
* 📊 hourly staffing-demand configuration
* 👥 employee management
* 🙋 vacations, requested days off and shift preferences
* 🔄 inter-store employee delegations
* 🏢 multi-level organizational structure
* 🔐 JWT authentication and role-based authorization
* 📊 schedule analysis and warning generation
* 📤 Excel and PDF export
* ☁️ Cloudflare R2 object storage
* 📨 Apache Kafka event processing
* 🚀 temporary demo environments
* 🐳 Docker-based deployment
* 🔄 automated CI/CD

---

# 🛠 Tech Stack

| Area                | Technology                  |
| ------------------- | --------------------------- |
| Language            | **Java 21**                 |
| Backend             | **Spring Boot 3.5**         |
| Web                 | Spring Web                  |
| Security            | Spring Security + JWT       |
| Persistence         | Spring Data JPA / Hibernate |
| Database            | **MySQL 8**                 |
| Database migrations | **Flyway**                  |
| DTO mapping         | MapStruct                   |
| Messaging           | **Apache Kafka**            |
| Testing             | JUnit 5, Mockito            |
| Excel               | Apache POI                  |
| PDF                 | OpenPDF                     |
| Object storage      | Cloudflare R2               |
| Build               | Maven                       |
| Containers          | Docker / Docker Compose     |
| CI/CD               | GitHub Actions              |
| Reverse proxy       | Caddy                       |
| Hosting             | Hetzner VPS                 |
| Code quality        | JetBrains Qodana            |

Frontend:

```text
React + TypeScript + Vite + Tailwind CSS
```

---

# 🏗 Architecture

Typical backend request flow:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Repository
     ↓
Hibernate / JPA
     ↓
MySQL 8
```

DTO mapping is handled using **MapStruct**, while database schema evolution is managed using **Flyway migrations**.

---

# 🌐 Infrastructure

Production environment:

```text
Internet
   ↓
stworzgrafik.online
   ↓
Caddy / HTTPS
   ↓
Docker Compose
   ├── Frontend
   ├── Spring Boot Backend
   ├── MySQL 8
   └── Apache Kafka

Backend
   ↓
Cloudflare R2
```

Deployment is automated using **GitHub Actions**.

---

# 🚀 Running locally

## Requirements

* Java 21
* Docker
* Docker Compose

Clone the repository:

```bash
git clone https://github.com/makcc93/stworz-grafik-online.git

cd stworz-grafik-online
```

Start using Docker:

```bash
docker compose up -d --build
```

or Maven:

```bash
./mvnw spring-boot:run
```

---

# 🧪 Tests

Run tests using:

```bash
./mvnw test
```

The project uses **JUnit 5 and Mockito**.

Tests are also executed as part of the CI/CD pipeline.

---

# 🌐 Live Demo

### 👉 https://stworzgrafik.online

A temporary demo environment can be created without registering a permanent account.

---

# 📜 License

This repository is publicly accessible primarily for **portfolio and project demonstration purposes**.

The source code is not currently distributed under an open-source license permitting unrestricted copying, modification, deployment or commercial use.

**All rights reserved.**

---

# 👨‍💻 Author

Designed and developed by **Mateusz Kruk**

GitHub: [@makcc93](https://github.com/makcc93)
