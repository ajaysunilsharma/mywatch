import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL;

function SetUsername({ setUser }) {
  const [username, setUsername] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await fetch(`${API_URL}/api/auth/oauth/complete-signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ username }),
      });

      if (response.ok) {
        // Refresh user state (which will now fetch the full user from /me)
        window.location.href = '/';
      } else {
        const data = await response.json();
        setError(data.error || 'Failed to set username so far');
      }
    } catch (err) {
      setError('An error occurred');
    }
  };

  return (
    <div className="container" style={{ maxWidth: '400px', marginTop: '2rem' }}>
      <h2>Finish Signup</h2>
      <p style={{ color: '#666', marginBottom: '1rem' }}>Please choose a username for your account.</p>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Username*</label>
          <input type="text" value={username} onChange={e => setUsername(e.target.value)} required />
        </div>
        <button type="submit" className="btn-primary" style={{ width: '100%' }}>Complete Signup</button>
      </form>
    </div>
  );
}

export default SetUsername;