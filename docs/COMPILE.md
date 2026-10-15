# Building FastLayout from Source

## Prerequisites

- **JDK 17+** (JDK 21 recommended) — [Adoptium Temurin](https://adoptium.net/)
- **Maven 3.9+** — [Apache Maven](https://maven.apache.org/download.cgi)

FastLayout is a pure, zero-dependency Java module that does not require C++ compilers or native toolchains.

---

## Build Commands

| Command | Purpose |
|:---|:---|
| `mvn clean compile` | Compiles core FastLayout Java sources. |
| `mvn test` | Runs the full JUnit 5 unit test suite. |
| `mvn clean package` | Builds the FastLayout JAR and sources JAR in `target/`. |
| `mvn clean install` | Installs FastLayout to your local `.m2` repository for other projects. |
| `.\run-benchmark.bat` | Compiles and executes the OpenJDK JMH microbenchmark suite. |

---

## Running the JMH Microbenchmark

FastLayout contains a dedicated JMH benchmark suite measuring throughput for 10, 100, and 1,000 cells across `measure`, AoS `Rect[]`, and flat `float[]` buffers:

```powershell
.\run-benchmark.bat
```
