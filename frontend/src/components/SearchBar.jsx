import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { searchWatches } from '../utils/api';
import './SearchBar.css';

function SearchBar() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [showResults, setShowResults] = useState(false);
  const searchRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowResults(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    const delayDebounceFn = setTimeout(async () => {
      if (query.length >= 3) {
        try {
          const response = await searchWatches(query);
          setResults(response.data);
          setShowResults(true);
        } catch (error) {
          console.error('Error searching watches:', error);
        }
      } else {
        setResults([]);
        setShowResults(false);
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [query]);

  return (
    <div className="search-bar-container" ref={searchRef}>
      <div className="search-input-wrapper">
        <input
          type="text"
          className="search-input"
          placeholder="Search watches..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => query.length >= 3 && setShowResults(true)}
        />
        <span className="search-icon">🔍</span>
      </div>

      {showResults && results.length > 0 && (
        <div className="search-results">
          {results.map((watch) => (
            <Link
              key={watch.id}
              to={`/watch/${watch.id}`}
              className="search-result-item"
              onClick={() => setShowResults(false)}
            >
              <div className="result-image">
                {watch.thumbnailUrl ? (
                  <img src={watch.thumbnailUrl} alt={watch.model} />
                ) : (
                  <span>⌚</span>
                )}
              </div>
              <div className="result-info">
                <div className="result-brand">{watch.brand}</div>
                <div className="result-model">{watch.model}</div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {showResults && results.length === 0 && query.length >= 3 && (
        <div className="search-results">
          <div className="no-results">No watches found</div>
        </div>
      )}
    </div>
  );
}

export default SearchBar;
