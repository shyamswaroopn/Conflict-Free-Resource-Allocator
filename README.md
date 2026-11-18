Conflict-Free Resource Allocation System
A Java desktop application for generating conflict-free schedules using graph-colouring algorithms.
The system models scheduling conflicts as a graph and assigns activities to time slots so that activities sharing students are not scheduled at the same time.
Overview
Scheduling exams, tasks, and other academic activities can become difficult when the same students are assigned to overlapping activities.
This project represents each activity as a vertex in a graph. An edge connects two vertices when the corresponding activities share students and therefore conflict. Graph-colouring algorithms are then used to assign time slots while ensuring that connected vertices receive different colours.
Features
Dashboard --- Overview of students, resources, tasks, and scheduling conflicts.
Student management --- Add and manage student records.
Resource management --- Manage available scheduling resources.
Task management --- Create and manage tasks or examinations.
Conflict detection --- Identify conflicts between tasks based on shared students.
Graph visualization --- View tasks as vertices and conflicts as edges.
Schedule generation --- Generate time-slot assignments using graph-colouring algorithms.
Allocation management --- View and manage generated allocations.
Conflict analysis --- Inspect vertex degrees and edge weights to understand scheduling constraints.
How the Scheduling Model Works
The application represents the scheduling problem as a conflict graph:
Graph concept   Meaning in this project
Vertex          A task or examination Edge            A conflict between two tasks Colour          An assigned time slot Degree          Number of conflicts connected to a vertex Edge weight     Number of students shared by two conflicting tasks
Scheduling rule: Two connected vertices must not receive the same colour, because the corresponding tasks share students.
Algorithms
Greedy Graph Coloring
Processes vertices one at a time and assigns each vertex an available colour that is not already used by its adjacent vertices.
Welsh--Powell Algorithm
Orders vertices by descending degree and assigns colours according to the conflict relationships between vertices.
Both algorithms produce valid colour assignments for the represented conflict graph. The number of colours used depends on the graph and the algorithm; these heuristics do not guarantee the minimum possible number of time slots.
Technology Stack
Technology                Purpose
Java                      Application logic Java Swing                Desktop graphical user interface MySQL                     Database JDBC                      Java-to-MySQL connectivity Graph Theory              Conflict modelling Greedy Graph Coloring     Schedule generation Welsh--Powell Algorithm   Schedule generation
Project Structure
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
Getting Started
Prerequisites
Before running the application, make sure you have:
Java Development Kit (JDK)
MySQL Server
MySQL Workbench (optional, for database management)
1. Clone the repository
git clone https://github.com/shyamswaroopn/Conflict-Free-Resource-Allocator.git
cd Conflict-Free-Resource-Allocator
2. Set up the database
Open MySQL Workbench and execute the SQL script:
sql/schema.sql
This creates the conflict_free_allocator database and its required tables.
Important: The schema script resets existing project tables. Do not execute it again if you need to preserve existing data.
3. Configure database credentials
Copy db.properties.example.
Rename the copy to db.properties.
Open db.properties and update the values with your local MySQL credentials.
db.host=localhost
db.port=3306
db.name=conflict_free_allocator
db.user=YOUR_MYSQL_USERNAME
db.password=YOUR_MYSQL_PASSWORD
Keep your actual db.properties file private. It is excluded from Git.
4. Build and run the application
Windows
Run:
build_and_run.bat
Linux / macOS
Run:
chmod +x build_and_run.sh
./build_and_run.sh
Typical Workflow
Add students and create tasks.
Assign students to their respective tasks.
Add and manage available resources.
Detect conflicts between tasks.
Generate the conflict graph.
Apply Greedy Graph Coloring or the Welsh--Powell algorithm.
View the generated time-slot assignments.
Possible Future Improvements
Compare algorithm performance across different datasets.
Explore alternative scheduling strategies.
Add more detailed resource-capacity constraints.
Export generated schedules and reports.
Support larger datasets and additional scheduling constraints.

