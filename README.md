# 🛰️ SatOps DSL Simulator  

A **Domain Specific Language (DSL)** for simulating satellite operations, ground station support, and mission control workflows.  
This project provides a way to write `.satops` scripts that define satellite deployments, movements, telemetry, and ground station communication, and then execute them via a Spring Boot backend.  

---

## 🚀 Features  

- **Custom DSL** (`.satops` files) for satellite operations.  
- Commands to:  
  - Deploy satellites into memory.  
  - Move satellites (change latitude/longitude/orbit).  
  - Print status/telemetry.  
  - Add & manage **Ground Stations** (e.g., Pune, Bangkok).  
  - Support communication between satellites and ground stations.  
- Backend built with **Spring Boot** (Java 21 + ANTLR4).  
- **In-memory execution** (with plan for MySQL persistence).  
- Future integration with **CesiumJS** for interactive 3D visualization of satellites.  

---


---
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/311d5700-59d2-4e87-be4b-cc045c198ce0" />


## 📝 Example DSL Script  

```satops
deploy Sat1 at 19.07, 72.87 altitude 500;
deploy Sat2 at 13.73, 100.52 altitude 550;

move Sat1 to 20.00, 73.00 altitude 510;

groundstation Pune at 18.52, 73.85;
groundstation Bangkok at 13.73, 100.52;

print Sat1;
print Pune;

