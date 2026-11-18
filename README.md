# Conflict-Free Resource Allocation System

A Java-based scheduling and resource allocation application that uses **Graph Coloring algorithms** to identify conflicts and generate conflict-free schedules.

## Overview

Scheduling multiple exams, tasks, or academic activities can lead to conflicts when the same students are assigned to overlapping activities.

This project models these conflicts using a graph and applies graph-coloring algorithms to assign activities to different time slots, ensuring that conflicting activities are not scheduled at the same time.

## Key Features

- **Dashboard:** Overview of students, resources, tasks, and scheduling conflicts.
- **Student Management:** Add and manage student records.
- **Resource Management:** Manage available scheduling resources.
- **Task Management:** Create and manage tasks or examinations.
- **Conflict Detection:** Identify conflicts between tasks based on shared students.
- **Graph Visualization:** View tasks as vertices and conflicts as edges.
- **Graph Coloring:** Generate conflict-free time-slot assignments.
- **Allocation Management:** View and manage generated allocations.
- **Conflict Analysis:** Display vertex degrees and edge weights to understand scheduling constraints.

## Algorithms Used

### 1. Greedy Graph Coloring

Assigns colors to vertices one by one, selecting an available color that is not used by adjacent vertices.

### 2. Welsh-Powell Algorithm

Sorts vertices by descending degree and assigns colors based on the conflict relationships between vertices.

Both algorithms are used to generate valid color assignments. The number of colors used depends on the graph and the algorithm; these heuristics do not guarantee the minimum possible number of colors.

## Understanding the Conflict Graph

| Graph Element | Meaning |
|---|---|
| Vertex | A task or examination |
| Edge | A conflict between two tasks |
| Color | An assigned time slot |
| Degree | Number of conflicts connected to a vertex |
| Edge Weight | Number of students shared by two conflicting tasks |

**Main rule:** Two connected vertices must never receive the same color.

## Technologies Used

- Java
- Java Swing (GUI)
- MySQL
- JDBC
- Graph Theory
- Greedy Graph Coloring
- Welsh-Powell Algorithm

## Project Structure

```text
Conflict-Free-Resource-Allocator/
├── src/
│   └── com/allocator/
│       ├── config/
│       ├── dao/
│       ├── graph/
│       ├── model/
│       ├── service/
│       ├── test/
│       └── ui/
├── sql/
│   └── schema.sql
├── lib/
│   └── mysql-connector-j-8.0.33.jar
├── db.properties.example
├── build_and_run.bat
├── build_and_run.sh
├── .gitignore
└── README.md
```

## Setup and Installation

### Prerequisites

- Java Development Kit (JDK)
- MySQL Server
- MySQL Workbench (optional, for database management)

### 1. Clone the Repository

```bash
git clone https://github.com/shyamswaroopn/Conflict-Free-Resource-Allocator.git
cd Conflict-Free-Resource-Allocator
```

### 2. Set Up the Database

Open MySQL Workbench and execute:

```text
sql/schema.sql
```

This creates the `conflict_free_allocator` database and its required tables.

**Note:** The schema script resets existing project tables. Do not execute it again if you want to preserve existing data.

### 3. Configure Database Credentials

Copy `db.properties.example` and name the copy `db.properties`.

Update the values in `db.properties` with your local MySQL credentials:

```properties
db.host=localhost
db.port=3306
db.name=conflict_free_allocator
db.user=YOUR_MYSQL_USERNAME
db.password=YOUR_MYSQL_PASSWORD
```

Keep your actual `db.properties` file private. It is excluded from Git.

### 4. Run the Application

On Windows, run:

```text
build_and_run.bat
```

On Linux or macOS, run:

```bash
chmod +x build_and_run.sh
./build_and_run.sh
```

## Expected Workflow

1. Add students and create tasks.
2. Assign students to their respective tasks.
3. Add and manage available resources.
4. Detect conflicts between tasks.
5. Generate a conflict graph.
6. Apply Greedy or Welsh-Powell coloring.
7. View the generated time-slot assignments.

## Future Improvements

- Compare algorithm performance across different datasets.
- Explore improved scheduling strategies.
- Add more detailed resource-capacity constraints.
- Export generated schedules and reports.
- Support larger datasets and additional scheduling constraints.

## Author

**Shyam Swaroop N**

GitHub: https://github.com/shyamswaroopn

---

*Developed as an academic project to demonstrate the practical application of Graph Theory, Java, and database-driven scheduling.*
