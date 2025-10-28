# GOAT Watches - Project Documentation

## Overview
GOAT Watches is a community-curated leaderboard of the greatest watches of all time. Users can add watches with images, vote on them, and write text reviews.

## Architecture
- **Frontend**: React + Vite, React Router, Axios
- **Backend**: Java Spring Boot (port 8080)
- **Database**: H2 (embedded file-based)
- **Image Storage**: Local filesystem (`/uploads`)

## Features
1. Watch leaderboard with sorting (Top, Newest, Most Reviewed)
2. Add watches with image upload
3. Upvote/downvote system with cookie-based tracking
4. Text reviews with optional images (max 1000 chars)
5. Admin moderation endpoints
6. Mobile-responsive design with green (#0B6B3A) and gold (#C6A25E) theme

## Project Structure
```
/
├── backend/               Java Spring Boot backend
│   ├── src/main/java/com/goatwatches/
│   │   ├── entity/       Database entities
│   │   ├── repository/   JPA repositories
│   │   ├── service/      Business logic
│   │   ├── controller/   REST API endpoints
│   │   └── config/       Configuration
│   ├── pom.xml           Maven dependencies
│   └── data/             H2 database storage
├── frontend/             React Vite frontend
│   ├── src/
│   │   ├── components/   Reusable components
│   │   ├── pages/        Page components
│   │   └── utils/        API utilities
│   └── package.json
├── uploads/              Uploaded images
└── README.md

```

## Running the Project
Both workflows are configured:
- **Backend**: Runs on port 8080 (console output)
- **Frontend**: Runs on port 5000 (webview - user-facing)

## Environment Variables
- `ADMIN_TOKEN`: Secret for admin endpoints (default: "changeme")
- `UPLOAD_DIR`: Image upload directory (default: /tmp/uploads)

## API Endpoints
- `GET /api/watches?sort=top|new|reviews&page=0&size=20`
- `POST /api/watches` (multipart: brand, model, year, description, createdBy, image)
- `GET /api/watches/{id}`
- `GET /api/watches/{id}/reviews?page=0&size=10`
- `POST /api/watches/{id}/reviews` (multipart: authorName, content, image)
- `POST /api/watches/{id}/vote` (JSON: {vote: 1|-1, voterToken: string})
- `DELETE /api/admin/watches/{id}?token=ADMIN_TOKEN`
- `DELETE /api/admin/reviews/{id}?token=ADMIN_TOKEN`

## Database Schema
- **watches**: id, brand, model, watch_year, description, created_at, created_by, thumbnail_url, net_votes
- **reviews**: id, watch_id, author_name, content, image_url, created_at
- **votes**: id, watch_id, voter_token, vote_value, created_at

## Recent Changes
- 2025-10-28: Initial implementation of GOAT Watches MVP
  - Complete backend with Spring Boot, H2 database, REST APIs
  - Complete frontend with React, routing, and responsive design
  - Image upload with local storage and validation
  - Voting and review system
  - Green and gold theme implemented

## Future Enhancements
- User authentication with OAuth
- S3/CDN for image storage
- Full-text search
- Reputation system and fraud detection
- SEO optimization with SSR
