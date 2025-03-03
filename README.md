# Scrum Project Management Web Application

## Overview
This is a **Scrum Project Management Web Application** that provides various project management features such as scrum boards, task tracking, and project scheduling. Users can create and manage projects through an intuitive graphical interface, adjust project timelines, monitor progress, and efficiently control project workflows.

## Architecture
This project follows a **distributed microservices architecture**, consisting of the following services:
- **user-service**: Manages user authentication and profiles.
- **project-service**: Handles project creation and management.
- **task-service**: Manages task tracking and assignments.

### Key Technologies
- **Backend:** Apache Dubbo (RPC), Apache Seata (Distributed Transaction Management)
- **Frontend:** React, Refine framework
- **Database:** PostgreSQL
- **Containerization & Orchestration:** Docker, Docker Compose

---

## Running the Project with Docker

### Step 1: Start Nacos for Configuration Management
Navigate to the backend directory and start Nacos:
```sh
cd backend
docker-compose up -d nacos
```

### Step 2: Configure Seata in Nacos
Access Nacos at: [http://localhost:8848/nacos](http://localhost:8848/nacos) and add the following configurations:

#### **Configuration 1**
- **Data ID**: `service.vgroupMapping.default_tx_group`
- **Format**: `TEXT`
- **Group**: `SEATA_GROUP`

#### **Configuration 2**
- **Data ID**: `seataServer.properties`
- **Format**: `Properties`
- **Group**: `SEATA_GROUP`

Add the following content for `seataServer.properties`:
```properties
store.mode=db
store.lock.mode=db
store.session.mode=db

store.db.datasource=druid
store.db.dbType=postgresql
store.db.driverClassName=org.postgresql.Driver
store.db.url=jdbc:postgresql://seata-postgres/pm_assistant?stringtype=unspecified
store.db.user=postgres
store.db.password=123456
store.db.minConn=5
store.db.maxConn=30
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.distributedLockTable=distributed_lock
store.db.queryLimit=100
store.db.lockTable=lock_table
store.db.maxWait=5000
```

### Step 3: Start All Docker Containers
Run the following command to start all required services:
```sh
docker-compose up -d
```

### Step 4: Access the Application
Once the containers are up and running, open the application in your browser:
- **Local deployment:** [http://localhost](http://localhost)
- **Live demo:** [http://www.pmassistant.xyz](http://www.pmassistant.xyz)

---

## License
This project is open-source and available under the MIT License.

---

## Contact
For any inquiries, feel free to reach out or open an issue in the repository.

