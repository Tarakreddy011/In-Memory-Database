# In-Memory Database (Thread-Safe with TTL)

A high-performance, command-driven in-memory key-value database written in Java. Engineered with custom thread-safety primitives, lazy/proactive Time-To-Live (TTL) expiration algorithms, and a detached command parser architecture.

## Features

* **Command-Driven CLI Interface:** Parses and executes `PUT`, `GET`, `DELETE`, `START`, `STOP`, and `EXIT` commands.
* **TTL Entry Auto-Expiration:** Dual-strategy expiration using **Lazy Eviction** (during `GET` lookups) and a **Proactive Background Daemon** (periodic sweeps).
* **Fine-Grained Concurrency:** Transitioned from coarse `synchronized` methods to `ConcurrentHashMap` for high-throughput multi-threaded operations.
* **Volatile Lifecycle Controls:** Instant thread-wide visibility for database `START`/`STOP` states using thread safety primitives without locking overhead.
* **Fail-Fast Error Handling:** Custom exceptions for syntax errors, invalid TTL constraints, missing keys, and illegal operations when stopped.

## Tech Stack

* **Language:** Java 8+
* **Concurrency Tools:** `ConcurrentHashMap`, `ScheduledExecutorService`, `volatile` flags
* **Design Patterns:** Command Pattern, Separation of Concerns

## Command Reference

| Command | Syntax | Description |
| --- | --- | --- |
| **PUT** | `PUT <key> <value> [ttl_ms]` | Inserts or updates an entry (optional TTL in milliseconds). |
| **GET** | `GET <key>` | Retrieves active entry value; purges key if expired. |
| **DELETE** | `DELETE <key>` | Explicitly removes a key-value entry. |
| **START** | `START` | Sets volatile state to active and enables writes. |
| **STOP** | `STOP` | Suspends write operations and rejects new mutations. |
| **EXIT** | `EXIT` | Terminates background cleanup threads and stops process. |

## How It Works

1. **Input Parsing:** The command parser tokenizes raw input into structured execution objects before reaching the database engine.
2. **Concurrent Storage:** Integer keys route to entry objects containing data values alongside epoch millisecond expiration timestamps.
3. **Hybrid Expiration Sweeps:**
* **Lazy Check:** Checks current epoch time against key metadata on `GET`.
* **Proactive Cleanup:** A dedicated thread sweeps the database periodically to reclaim memory from abandoned keys.



## Getting Started

### 1. Build

```bash
javac Main.java

```

### 2. Run

```bash
java Main

```
