import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL;

function Signup() {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const navigate = useNavigate();

  const isPasswordStrong = (pwd) => {
    // At least 15 chars OR (at least 8 chars including a number and a lowercase letter)
    const longEnough = pwd.length >= 15;
    const complexEnough = pwd.length >= 8 && /[a-z]/.test(pwd) && /[0-9]/.test(pwd);
    return longEnough || complexEnough;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});
    setError('');
    
    if (!isPasswordStrong(password)) {
      setError('Password must be at least 15 characters OR at least 8 characters including a number and a lowercase letter.');
      return;
    }

    try {
      const response = await fetch(`${API_URL}/api/auth/user/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, username, password }),
      });

      if (response.ok) {
        navigate('/login', { state: { email } });
      } else {
        const data = await response.json();
        if (data.field) {
            setFieldErrors({ [data.field]: data.error });
        } else {
            setError(data.error || 'Signup failed');
        }
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
          <label>Email*</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
          {fieldErrors.email && <div className="error-message" style={{fontSize: '0.85rem', marginTop: '0.25rem'}}>{fieldErrors.email}</div>}
        </div>
        <div className="form-group">
          <label>Username*</label>
          <input type="text" value={username} onChange={e => setUsername(e.target.value)} required />
          {fieldErrors.username && <div className="error-message" style={{fontSize: '0.85rem', marginTop: '0.25rem'}}>{fieldErrors.username}</div>}
        </div>
        <div className="form-group">
          <label>Password*</label>
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