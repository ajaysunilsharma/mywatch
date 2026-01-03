import { useState, useEffect } from 'react';
import { getWatches } from '../utils/api';
import WatchCard from '../components/WatchCard';
import './Leaderboard.css';

function Leaderboard({ user }) {
  const [watches, setWatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sort, setSort] = useState('top');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadWatches();
  }, [sort, page]);

  const loadWatches = async () => {
    setLoading(true);
    try {
      const response = await getWatches(sort, page, 20);
      setWatches(response.data.watches);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Error loading watches:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleVoteUpdate = (updatedWatch) => {
    setWatches(prevWatches => 
      prevWatches.map(w => w.id === updatedWatch.id ? updatedWatch : w)
    );
  };

  const handleDelete = (deletedWatchId) => {
    setWatches(prevWatches => prevWatches.filter(w => w.id !== deletedWatchId));
  };

  return (
    <div className="leaderboard">
      <div className="hero">
        <div className="container">
          <h1>The Greatest Watches of All Time</h1>
          <p>Community-curated leaderboard of the most iconic timepieces</p>
        </div>
      </div>

      <div className="container">
        <div className="controls">
          <div className="sort-controls">
            <button 
              className={sort === 'top' ? 'sort-btn active' : 'sort-btn'}
              onClick={() => { setSort('top'); setPage(0); }}
            >
              Top Rated
            </button>
            <button 
              className={sort === 'new' ? 'sort-btn active' : 'sort-btn'}
              onClick={() => { setSort('new'); setPage(0); }}
            >
              Newest
            </button>
            <button 
              className={sort === 'reviews' ? 'sort-btn active' : 'sort-btn'}
              onClick={() => { setSort('reviews'); setPage(0); }}
            >
              Most Reviewed
            </button>
          </div>
        </div>

        {loading ? (
          <div className="loading">Loading watches...</div>
        ) : watches.length === 0 ? (
          <div className="empty-state">
            <h2>No watches yet!</h2>
            <p>Be the first to add a watch to the leaderboard.</p>
          </div>
        ) : (
          <>
            <div className="watches-grid">
              {watches.map(watch => (
                <WatchCard 
                  key={watch.id} 
                  watch={watch} 
                  onVoteUpdate={handleVoteUpdate}
                  onDelete={handleDelete}
                  user={user}
                />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="pagination">
                <button 
                  onClick={() => setPage(p => Math.max(0, p - 1))} 
                  disabled={page === 0}
                  className="btn-outline"
                >
                  Previous
                </button>
                <span className="page-info">
                  Page {page + 1} of {totalPages}
                </span>
                <button 
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} 
                  disabled={page >= totalPages - 1}
                  className="btn-outline"
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default Leaderboard;
