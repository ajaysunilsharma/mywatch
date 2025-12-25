import { useState } from 'react';
import { Link } from 'react-router-dom';
import { voteWatch, deleteWatch } from '../utils/api';
import './WatchCard.css';

function WatchCard({ watch, onVoteUpdate, onDelete }) {
  const [voting, setVoting] = useState(false);
  const [currentVotes, setCurrentVotes] = useState(watch.netVotes || 0);

  const handleVote = async (voteValue) => {
    if (voting) return;
    setVoting(true);
    try {
      const response = await voteWatch(watch.id, voteValue);
      setCurrentVotes(response.data.netVotes);
      if (onVoteUpdate) {
        onVoteUpdate(response.data);
      }
    } catch (error) {
      console.error('Error voting:', error);
    } finally {
      setVoting(false);
    }
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to delete this watch?')) {
      try {
        // In a real app, you'd get this from a secure place or prompt
        const token = prompt('Enter admin token to delete:');
        if (token) {
          await deleteWatch(watch.id, token);
          if (onDelete) {
            onDelete(watch.id);
          }
        }
      } catch (error) {
        console.error('Error deleting watch:', error);
        alert('Failed to delete watch. Check your token.');
      }
    }
  };

  return (
    <div className="watch-card">
      <Link to={`/watch/${watch.id}`} className="watch-card-link">
        <div className="watch-image-container">
          {watch.thumbnailUrl ? (
            <img 
              src={watch.thumbnailUrl} 
              alt={`${watch.brand} ${watch.model}`}
              className="watch-image"
            />
          ) : (
            <div className="watch-image-placeholder">
              <span>⌚</span>
            </div>
          )}
        </div>
        
        <div className="watch-card-content">
          <h3 className="watch-title">{watch.brand} {watch.model}</h3>
          {watch.year && <p className="watch-year">{watch.year}</p>}
          <p className="watch-description">{watch.description}</p>
        </div>
      </Link>
      
      <div className="watch-card-footer">
        <div className="vote-controls">
          <button 
            onClick={() => handleVote(1)} 
            disabled={voting}
            className="vote-btn vote-up"
            title="Upvote"
          >
            ▲
          </button>
          <span className="vote-count">{currentVotes}</span>
          <button 
            onClick={() => handleVote(-1)} 
            disabled={voting}
            className="vote-btn vote-down"
            title="Downvote"
          >
            ▼
          </button>
        </div>
        <button
          onClick={handleDelete}
          className="delete-btn"
          title="Delete Watch"
        >
          🗑️
        </button>
      </div>
    </div>
  );
}

export default WatchCard;
