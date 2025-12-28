import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import validator from 'validator';

const API_URL = import.meta.env.VITE_API_URL;

function Signup() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validator.isStrongPassword(password, {
      minLength: 8, 
      minLowercase: 1, 
      minUppercase: 1, 
      minNumbers: 1, 
      minSymbols: 1 
    })) {
      setError('Password must be at least 8 characters long and include 1 uppercase, 1 lowercase, 1 number, and 1 symbol.');
      return;
    }

    try {
      const response = await fetch(`${API_URL}/api/auth/user/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      if (response.ok) {
        navigate('/login');
      } else {
        const data = await response.json();
        setError(data.error || 'Signup failed');
      }
    } catch (err) {
      setError('Signup failed');
    }
  };

  return (
    <div className="container" style={{ maxWidth: '400px', marginTop: '2rem' }}>
      <h2>Sign Up</h2>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Username</label>
          <input type="text" value={username} onChange={e => setUsername(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <button type="submit" className="btn-primary" style={{ width: '100%' }}>Create Account</button>
      </form>
      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}

export default Signup;