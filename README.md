# Veritas — AI Fraud Detection API

> Latin for *Truth* — because every transaction tells a story.

A Spring Boot REST API that runs transactions through a **5-stage fraud detection pipeline** and uses **LLaMA 3.3 AI via Groq** to generate human-readable explanations for each risk assessment.

---

## How It Works
POST /transaction/check
↓
┌─────────────────────────────┐
│     5-Stage Pipeline        │
│                             │
│  Stage 1: Amount Check      │
│  Stage 2: Self Transfer     │
│  Stage 3: Frequency Check   │
│  Stage 4: Time Check        │
│  Stage 5: Round Number      │
└─────────────────────────────┘
↓
Risk Score (0-100)
↓
Groq AI Explanation
↓
Save to MySQL + Return

## Risk Levels

| Score | Risk Level |
|-------|------------|
| 0 – 30 | ✅ SAFE |
| 31 – 60 | ⚠️ SUSPICIOUS |
| 61+ | 🚨 FRAUDULENT |

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transaction/check` | Analyze a transaction |
| GET | `/transaction/history` | Get all past checks |
| GET | `/transaction/history/{id}` | Get one transaction |
| DELETE | `/transaction/history/{id}` | Delete a transaction |

---

## Sample Request

```json
POST /transaction/check

{
  "sender": "Divyansh",
  "receiver": "Offshore Account",
  "amount": 200000,
  "description": "Urgent transfer"
}
```

## Sample Response

```json
{
  "id": 1,
  "sender": "Divyansh",
  "receiver": "Offshore Account",
  "amount": 200000.0,
  "description": "Urgent transfer",
  "riskScore": 50,
  "riskLevel": "SUSPICIOUS",
  "aiReason": "This transaction is flagged as suspicious due to the high amount of ₹2,00,000 being transferred to an offshore account. Large transfers to offshore accounts are commonly associated with money laundering. It is recommended to verify the recipient's identity before proceeding.",
  "checkedAt": "2026-05-11T10:30:00"
}
```

---

## Tech Stack

- **Backend:** Spring Boot, Java 17
- **Database:** MySQL, Spring Data JPA
- **AI:** Groq API (LLaMA 3.3 70B)
- **Architecture:** MVC, Multi-stage Pipeline

---

## Setup

1. Clone the repo
2. Create MySQL database: `CREATE DATABASE veritas;`
3. Copy `application.properties.example` → `application.properties`
4. Fill in your MySQL credentials and Groq API key
5. Run `mvnw spring-boot:run`

---

## Pipeline Stages

| Stage | Check | Risk Points |
|-------|-------|-------------|
| 1 | Amount > ₹1,00,000 | +40 |
| 1 | Amount > ₹50,000 | +20 |
| 2 | Sender == Receiver | +50 |
| 3 | 5+ transactions in 1 hour | +30 |
| 4 | Transaction between 12am–5am | +15 |
| 5 | Suspiciously round amount | +10 |
