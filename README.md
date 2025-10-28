# GOAT Watches - Community Watch Leaderboard

A community-curated leaderboard of the greatest watches of all time. Users can add watches, vote, and write reviews with image uploads.

## Tech Stack

- **Frontend**: React + Vite, React Router, Axios
- **Backend**: Java Spring Boot
- **Database**: H2 (embedded, file-based)
- **Image Storage**: Local filesystem

## Features

- ✅ Watch leaderboard with sorting (Top, Newest, Most Reviewed)
- ✅ Add watches with images
- ✅ Upvote/downvote system
- ✅ Text reviews with optional images
- ✅ Image upload with validation (max 5MB, JPEG/PNG)
- ✅ Admin moderation endpoints
- ✅ Mobile-responsive design
- ✅ Green & gold theme

## Running the Project

### Backend (Port 8080)
```bash
cd backend
mvn spring-boot:run
```

### Frontend (Port 5000)
```bash
cd frontend
npm install
npm run dev
```

The frontend proxies API requests to the backend automatically.

## Environment Variables

- `ADMIN_TOKEN`: Secret token for admin endpoints (default: "changeme")
- `UPLOAD_DIR`: Directory for uploaded images (default: /tmp/uploads)

## API Endpoints

- `GET /api/watches?sort=top|new|reviews&page=0&size=20` - List watches
- `POST /api/watches` - Create watch (multipart)
- `GET /api/watches/{id}` - Get watch detail
- `POST /api/watches/{id}/reviews` - Add review (multipart)
- `POST /api/watches/{id}/vote` - Vote on watch
- `DELETE /api/admin/watches/{id}?token=ADMIN_TOKEN` - Delete watch (admin)
- `DELETE /api/admin/reviews/{id}?token=ADMIN_TOKEN` - Delete review (admin)

## Database

H2 database file is stored at `backend/data/goatwatches.mv.db` and persists across restarts.

## Images

Images are stored in the `uploads/` directory and served at `/uploads/{filename}`.

## Future Enhancements

- User authentication with OAuth
- S3/CDN for image storage
- Full-text search
- Reputation system
- SEO optimization

---

Built with ⌚ by the GOAT Watches team
