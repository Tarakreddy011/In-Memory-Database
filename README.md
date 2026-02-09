Command-Driven In-Memory Database (Thread-Safe, TTL Enabled)
Overview

This project implements a generic, thread-safe in-memory database in Java.
It stores key-value data using integer keys, accepts commands from standard input, supports TTL expiration, and handles concurrent execution safely using OOP principles and multithreading.

This project demonstrates:

Object-Oriented Design

Exception Handling

Concurrency Control

Synchronization

Volatile lifecycle control

Use of Concurrent Data Structures

Features

Store any type of value (Generic<T>)

Integer-based keys

Command-driven interaction

Optional TTL (time-to-live)

Lazy expiration on access

Background cleanup thread

Thread-safe operations

Start/Stop database lifecycle

Custom exception handling

Scalable concurrent version using ConcurrentHashMap

Command Syntax
PUT <key> <value>
PUT <key> <value> <ttlMillis>
GET <key>
DELETE <key>
STOP
START
EXIT

Example
PUT 3 hello
PUT 5 100 3000
GET 3
DELETE 5
STOP
START
EXIT

Architecture Design
Core Classes
Command

Encapsulates parsed input.

CommandType type
Integer key
String rawValue
Long ttl

Entry<T>

Stores database values.

T value
long expiryTime  (-1 means no expiry)

InMemoryDatabase<T>

Handles storage and operations.

put()

get()

delete()

CommandParser

Reads input

Tokenizes

Validates

Builds Command objects

CommandExecutor

Executes commands in worker threads.

CleanupThread

Removes expired keys periodically.

Thread-Safety Strategy
Phase 1–6

Used HashMap

Protected operations with synchronized

Lock applied on database instance

Tradeoff

Simple

Safe

But blocks parallel reads/writes

Phase 10 Upgrade

Replaced with:

ConcurrentHashMap


Benefits:

Higher throughput

Better scalability

Fine-grained locking

TTL correctness maintained through atomic checks.

Lifecycle Control
volatile boolean running;


Why volatile:

Ensures visibility across threads

No caching of state

Lightweight compared to locking

Behavior:

STOP → PUT/DELETE rejected

START → Resume operations

TTL Strategy

Expiry stored as epoch time

Checked during:

GET (lazy expiration)

Background cleanup

Why epoch time:

Fast comparison

No need to track creation timestamps

Exception Handling

Custom exceptions used:

InvalidCommandException

DatabaseStoppedException

KeyNotFoundException

InvalidTTLException

Purpose:

Fail fast

Clear debugging

Clean API separation

Running the Project
Compile
javac *.java

Run
java Main


Then enter commands in console.

Sample Multi-Thread Test

Multiple executor threads feed commands simultaneously

Observe:

Correct reads

No lost updates

Safe deletions

Key Learning Outcomes

Separation of parsing and execution logic

Importance of synchronization

Race condition observation and fixing

Visibility guarantees using volatile

Concurrent collections vs locks

Safe iteration under modification

Real-world system design thinking
