# Conflict-Free Resource Allocation System Using Graph Coloring (Java PBL)

## 1. Project Overview
This project is a complete **Core Java + Java Swing + MySQL JDBC** desktop application designed to solve academic examination and physical resource scheduling conflicts using **Graph Coloring**.

### How Graph Coloring Solves Resource Allocation
We model the scheduling problem as an undirected **Conflict Graph** $G = (V, E)$:
1. **Vertices ($V$)**: Each academic exam or task (e.g., `CS301 Algorithms`, `CS302 Operating Systems`) is represented as a `Vertex`.
2. **Conflict Edges ($E$)**: An undirected `Edge` connects two tasks $u$ and $v$ if and only if:
   - They share one or more enrolled students (`task_enrollments` intersection $\neq \emptyset$), **or**
   - They are supervised/taught by the same instructor.
3. **Graph Coloring ($\chi(G)$)**: Each color $c \in \{0, 1, 2, \dots\}$ represents a distinct **Time Slot**. Proper graph coloring guarantees that for every conflict edge $(u, v) \in E$, $\text{color}(u) \neq \text{color}(v)$.
4. **Capacity-Aware Room Assignment**: Within each color class (tasks scheduled in the same time slot), the `AllocationEngine` assigns distinct physical rooms (`CLASSROOM`, `LAB`, `AUDITORIUM`) sorted by best-fit seating capacity.

---

## 2. Algorithms & Complexity Analysis

### A. Standard Greedy Graph Coloring (`GreedyColoring.java`)
- **Procedure**: Iterates through vertices $v_1, v_2, \dots, v_V$. For each vertex, scans its adjacency list $\text{Adj}[v_i]$ to flag colors already used by neighbors in a `boolean[] availableColors` array, then assigns the lowest available color index $c \ge 0$.
- **Time Complexity**: $\mathcal{O}(V^2 + E)$ in the worst case; neighbor scans across the entire graph inspect $2|E|$ half-edges.
- **Space Complexity**: $\mathcal{O}(V + E)$ to store the adjacency list and color availability array.

### B. Welsh-Powell Degree-Ordered Coloring (`WelshPowellColoring.java`)
- **Procedure**:
  1. Computes the degree $\deg(v)$ for every vertex $v \in V$.
  2. Sorts vertices in descending order of degree: $\deg(v_{(1)}) \ge \deg(v_{(2)}) \ge \dots \ge \deg(v_{(V)})$.
  3. In each pass for color $c$, assigns color $c$ to the first uncolored vertex and greedily adds all subsequent uncolored vertices that are not adjacent to any vertex already colored $c$.
- **Time Complexity**: $\mathcal{O}(V \log V + V^2 + E)$.
- **Space Complexity**: $\mathcal{O}(V + E)$.
- **Advantage**: Prioritizing high-degree conflict bottlenecks first prevents sub-optimal color fragmentation and consistently minimizes the chromatic number $\chi(G)$.

---

## 3. Complete Project Folder Structure

```text
java-project/
├── build_and_run.sh                  # Linux/macOS pure javac compiler & runner
├── build_and_run.bat                 # Windows pure javac compiler & runner
├── db.properties                     # MySQL JDBC host, port, user, and password config
├── README.md                         # Project documentation & complexity analysis
├── sql/
│   └── schema.sql                    # MySQL 8.0+ DDL creation script & university seed data
└── src/
    └── com/
        └── allocator/
            ├── Main.java                             # Swing launcher & JDBC check
            ├── config/
            │   ├── AppConfig.java                    # JDBC URL & time slot constants
            │   └── DatabaseConnection.java           # Strict MySQL JDBC connection factory
            ├── model/
            │   ├── Student.java                      # Student POJO
            │   ├── Resource.java                     # Physical Room/Lab POJO
            │   ├── Task.java                         # Exam/Task Vertex POJO
            │   ├── Allocation.java                   # Final Slot + Room Allocation POJO
            │   └── ConflictReport.java               # Pairwise Conflict Report POJO
            ├── dao/
            │   ├── StudentDAO.java                   # Student CRUD via PreparedStatement
            │   ├── ResourceDAO.java                  # Resource CRUD via PreparedStatement
            │   ├── TaskDAO.java                      # Transactional Task & Enrollment DAO
            │   └── AllocationDAO.java                # Batch schedule save & manual override DAO
            ├── graph/
            │   ├── Vertex.java                       # Graph Vertex wrapper with degree & color
            │   ├── Edge.java                         # Undirected Conflict Edge with weight
            │   ├── ConflictGraph.java                # Adjacency List Graph implementation
            │   ├── GreedyColoring.java               # Standard Greedy Coloring algorithm
            │   └── WelshPowellColoring.java          # Welsh-Powell Degree-Ordered algorithm
            ├── service/
            │   ├── ConflictDetector.java             # Pairwise enrollment/instructor clash detector
            │   └── AllocationEngine.java             # Orchestrates coloring, rooms & manual guard
            ├── ui/
            │   ├── MainFrame.java                    # Main JFrame window with CardLayout
            │   ├── components/
            │   │   ├── SidebarPanel.java             # Navigation sidebar
            │   │   ├── StatCard.java                 # KPI metric card
            │   │   └── ModernTable.java              # Styled dark-slate JTable
            │   └── views/
            │       ├── DashboardView.java            # Admin dashboard & conflict edge table
            │       ├── StudentView.java              # Student CRUD form & table
            │       ├── ResourceView.java             # Resource CRUD form & table
            │       ├── TaskView.java                 # Task CRUD & multi-student enrollment
            │       ├── GraphVisualizerPanel.java     # Custom 2D Swing Graph & Coloring canvas
            │       └── AllocationView.java           # Final schedule & manual override editor
            └── test/
                └── GraphColoringTestSuite.java       # 4 automated sample test cases
```

---

## 4. How to Configure and Run Locally

### Step 1: Set Up the MySQL Database
1. Ensure **MySQL Server 8.0+** is installed and running on `localhost:3306`.
2. Execute the provided SQL creation and seed script:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
3. Update `db.properties` with your MySQL username and password if different from `root` / `root`.

### Step 2: Place MySQL Connector/J JAR in `lib/`
Download `mysql-connector-j-8.4.0.jar` (or any 8.x/9.x version) from Maven Central or MySQL Downloads and place it inside a `lib/` folder inside `java-project/`.

### Step 3: Compile and Run (Without Maven)
- **On Linux / macOS**:
  ```bash
  chmod +x build_and_run.sh
  ./build_and_run.sh
  java -cp "out:lib/*" com.allocator.Main
  ```
- **On Windows**:
  ```cmd
  build_and_run.bat
  ```
