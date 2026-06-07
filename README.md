# CommerceCraft

Portfolio-ready ecommerce application built with Spring Boot and React.

## Features

- Product catalog with search, category filtering, featured products, ratings, and inventory.
- In-memory cart workflow backed by REST APIs.
- Checkout API that persists orders with customer and line-item details.
- Admin-style dashboard endpoint with revenue, order, customer, and low-stock metrics.
- H2 database seeded with realistic demo data for local development.
- React storefront with cart, checkout drawer, product discovery, and dashboard metrics.

## Tech Stack

- Backend: Java 17, Spring Boot, Spring Web, Spring Data JPA, Validation, Security, H2.
- Frontend: React, Vite, Lucide icons.
- Tests: JUnit 5, Spring Boot Test, MockMvc.

## Run Backend

```bash
./gradlew bootRun
```

Backend runs at `http://localhost:8080`.

Useful endpoints:

- `GET /api/products`
- `GET /api/products/featured`
- `GET /api/categories`
- `GET /api/carts/{cartId}`
- `POST /api/carts/{cartId}/items`
- `POST /api/orders`
- `GET /api/dashboard/summary`
- `GET /h2-console`

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

## Demo Checkout Payload

```json
{
  "cartId": "demo-cart",
  "customer": {
    "fullName": "Priya Sharma",
    "email": "priya@example.com",
    "phone": "+91 9876543210",
    "addressLine": "12 MG Road",
    "city": "Bengaluru",
    "state": "Karnataka",
    "postalCode": "560001"
  },
  "paymentMethod": "CARD"
}
```
