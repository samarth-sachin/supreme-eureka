# 🛰️ SatOps-DSL

A **Domain-Specific Language (DSL)** and **simulation engine** for satellite operations, mission planning, and resource management.  
Built with **Spring Boot + Java + ANTLR4 + Orekit**, this project enables mission control teams to define and execute satellite operations using simple DSL commands.  

---

## 🚀 Features

- **Custom DSL for Satellite Operations**
  - Deploy satellites & ground stations
  - Link/unlink satellites with stations
  - Send/receive messages
  - Execute mission scripts

- **Celestrak + Orekit Integration**
  - Fetch TLEs (Two-Line Elements) for real satellites
  - Simulate orbits & ground station visibility
  - Propagate satellite positions

- **Mission Control**
  - Run mission scenarios via DSL
  - Validate visibility before sending messages
  - Future support for scheduling & optimization

- **Extensible Backend**
  - Designed with **Spring Boot**
  - Pluggable with **OptaPlanner** for optimization
  - Ready for DBMS persistence & Kafka integration

---
<img width="1719" height="796" alt="image" src="https://github.com/user-attachments/assets/e8f70703-54c5-4fab-b548-d962ea5c44dc" />

## 📖 Example DSL Script

```dsl
deploy satellite Sat1 at Celestrak:STARLINK-3000
deploy groundstation GS1 at "Pune, India"

link Sat1 to GS1
send "Hello Earth!" from Sat1 to GS1
