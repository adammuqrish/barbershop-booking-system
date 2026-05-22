# Hugi Barbershop

Full-stack Spring Boot barbershop booking system with role-based access (Customer, Barber, and Admin).

## Features

### Customer
- Register and login
- Book appointments with date/time/barber selection
- Pay online (bank transfer) or cash
- Earn loyalty points (2 points = 1 free appointment)
- View, edit, or cancel upcoming appointments
- View appointment history
- Submit and manage feedback with star ratings
- Update profile and profile picture

### Barber
- Dashboard with personal sales, customer, and appointment stats
- View assigned customers and appointments
- Manage transactions and feedback
- Update profile and profile picture

### Admin
- Dashboard with total sales, customers, appointment counts, and chart
- Manage customers, staff (register/edit/delete barbers and admins)
- Manage all appointments (update status, reschedule, delete)
- View all transactions and feedback
- Update own profile

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.4.1, Spring MVC, Spring Data JPA, Spring Security
- **Frontend:** Thymeleaf, Tailwind CSS, Flowbite, Chart.js
- **Database:** PostgreSQL
- **Auth:** BCrypt password hashing, role-based access control
- **Migrations:** Flyway
- **Deployment:** Heroku

## Pricing

| Category | Price |
|----------|-------|
| Child    | $10   |
| Teen     | $13   |
| Adult    | $15   |
| Senior   | $12   |

## Running Locally

```text
$ git clone <repo-url>
$ cd hugi-barbershop
$ ./mvnw spring-boot run
```

Requires a PostgreSQL database. Set the `JDBC_DATABASE_URL` environment variable or configure in `application.properties`.

## Deploying to Heroku

```text
$ heroku login
$ heroku create
$ git push heroku main
$ heroku open
```
