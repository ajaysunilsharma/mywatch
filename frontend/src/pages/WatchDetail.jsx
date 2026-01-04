import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getWatch, getReviews, createReview, voteWatch } from '../utils/api';
import { loadModel, validateWatchImage } from '../utils/imageValidator';
import './WatchDetail.css';

function WatchDetail({ user }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [watch, setWatch] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [reviewCount, setReviewCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [reviewPage, setReviewPage] = useState(0);
  const [totalReviewPages, setTotalReviewPages] = useState(0);
  
  const [reviewForm, setReviewForm] = useState({
    content: ''
  });
  const [reviewImage, setReviewImage] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [voting, setVoting] = useState(false);
  const fileInputRef = useRef(null);
  const [modelReady, setModelReady] = useState(false);
  const [isValidating, setIsValidating] = useState(false);

  useEffect(() => {
    loadWatch();
    loadReviews();
    loadModel().then(() => setModelReady(true));
  }, [id, reviewPage]);

  const loadWatch = async () => {
    try {
      const response = await getWatch(id);
      setWatch(response.data.watch);
      setReviewCount(response.data.reviewCount);
    } catch (error) {
      console.error('Error loading watch:', error);
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const loadReviews = async () => {
    try {
      const response = await getReviews(id, reviewPage, 10);
      setReviews(response.data.reviews);
      setTotalReviewPages(response.data.totalPages);
    } catch (error) {
      console.error('Error loading reviews:', error);
    }
  };

  const handleVote = async (voteValue) => {
    if (voting) return;
    setVoting(true);
    try {
      const response = await voteWatch(id, voteValue);
      setWatch(response.data);
    } catch (error) {
      console.error('Error voting:', error);
    } finally {
      setVoting(false);
    }
  };

  const handleReviewChange = (e) => {
    setReviewForm({
      ...reviewForm,
      [e.target.name]: e.target.value
    });
  };

  const handleReviewImageChange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      setIsValidating(true);
      setError(''); // Clear previous errors

      const result = await validateWatchImage(file);

      if (result.isValid) {
        setReviewImage(file);
        setError('');
      } else {
        setError(result.error);
        setReviewImage(null);
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
      }
      setIsValidating(false);
    }
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    
    if (!reviewForm.content || reviewForm.content.length > 1000) {
      setError('Review must be between 1 and 1000 characters');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const data = new FormData();
      data.append('content', reviewForm.content);
      if (reviewImage) data.append('image', reviewImage);

      await createReview(id, data);
      
      setReviewForm({ content: '' });
      setReviewImage(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      setReviewPage(0);
      loadReviews();
      loadWatch();
    } catch (err) {
      console.error('Review submission error:', err);
      setError(err.response?.data?.error || 'Failed to submit review');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="loading-page">Loading...</div>;
  }

  if (!watch) {
    return <div className="loading-page">Watch not found</div>;
  }

  return (
    <div className="watch-detail">
      <div className="container">
        <div className="watch-detail-nav">
          <button onClick={() => navigate('/')} className="back-btn">
            ← Back to Leaderboard
          </button>
          {user && user.role === 'ROLE_ADMIN' && (
            <Link to={`/edit/${id}`} className="edit-btn">
              Edit Watch
            </Link>
          )}
        </div>

        <div className="watch-detail-header">
          <div className="watch-detail-image">
            {watch.thumbnailUrl ? (
              <img src={watch.thumbnailUrl} alt={`${watch.brand} ${watch.model}`} />
            ) : (
              <div className="watch-detail-placeholder">⌚</div>
            )}
          </div>

          <div className="watch-detail-info">
            <h1>{watch.brand} {watch.model}</h1>
            {watch.year && <p className="watch-detail-year">{watch.year}</p>}
            <p className="watch-detail-description">{watch.description}</p>
            <p className="watch-detail-meta">
              Added by <strong>{watch.createdBy || 'Anonymous'}</strong>
            </p>

            <div className="vote-section">
              <div className="vote-pill">
                <button
                  onClick={() => handleVote(1)}
                  disabled={voting}
                  className="vote-btn vote-up"
                >
                  ▲
                </button>
                <span className="vote-count up">{watch.upvotes || 0}</span>
              </div>
              <div className="vote-pill">
                <button
                  onClick={() => handleVote(-1)}
                  disabled={voting}
                  className="vote-btn vote-down"
                >
                  ▼
                </button>
                <span className="vote-count down">{watch.downvotes || 0}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="reviews-section">
          <h2>Reviews ({reviewCount})</h2>

          <div className="add-review-form">
            <h3>Add Your Review</h3>
            {error && <div className="error-message">{error}</div>}
            
            <form onSubmit={handleReviewSubmit}>
              <div className="form-group">
                <textarea
                  name="content"
                  value={reviewForm.content}
                  onChange={handleReviewChange}
                  placeholder="Share your thoughts... (max 1000 characters)"
                  rows="4"
                  required
                />
                <div className="char-count">
                  {reviewForm.content.length}/1000
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="review-image">
                  Attach Image (Optional)
                  {!modelReady && <span style={{ marginLeft: '8px', fontSize: '0.85em', color: '#666' }}>(Initializing AI...)</span>}
                </label>
                <input
                  type="file"
                  id="review-image"
                  accept="image/jpeg,image/jpg,image/png"
                  onChange={handleReviewImageChange}
                  ref={fileInputRef}
                  disabled={!modelReady || isValidating}
                />
              </div>

              <button type="submit" disabled={submitting || isValidating} className="btn-primary">
                {submitting ? 'Submitting...' : 'Submit Review'}
              </button>
            </form>
          </div>

          <div className="reviews-list">
            {reviews.length === 0 ? (
              <p className="no-reviews">No reviews yet. Be the first to review!</p>
            ) : (
              reviews.map(review => (
                <div key={review.id} className="review-card">
                  <div className="review-header">
                    <strong>{review.authorName || 'Anonymous'}</strong>
                    <span className="review-date">
                      {new Date(review.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <p className="review-content">{review.content}</p>
                  {review.imageUrl && (
                    <div className="review-image">
                      <img src={review.imageUrl} alt="Review" />
                    </div>
                  )}
                </div>
              ))
            )}

            {totalReviewPages > 1 && (
              <div className="pagination">
                <button 
                  onClick={() => setReviewPage(p => Math.max(0, p - 1))} 
                  disabled={reviewPage === 0}
                  className="btn-outline"
                >
                  Previous
                </button>
                <span>Page {reviewPage + 1} of {totalReviewPages}</span>
                <button 
                  onClick={() => setReviewPage(p => Math.min(totalReviewPages - 1, p + 1))} 
                  disabled={reviewPage >= totalReviewPages - 1}
                  className="btn-outline"
                >
                  Next
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default WatchDetail;
