import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { FaEye, FaEyeSlash } from 'react-icons/fa';

const API_URL = import.meta.env.VITE_API_URL;

function Signup() {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const navigate = useNavigate();

  const handleOAuthLogin = (provider) => {
    window.location.href = `${API_URL}/oauth2/authorization/${provider}`;
  };

  const isPasswordStrong = (pwd) => {
    // At least 15 chars OR (at least 8 chars including a number and a lowercase letter)
    const longEnough = pwd.length >= 15;
    const complexEnough = pwd.length >= 8 && /[a-z]/.test(pwd) && /[0-9]/.test(pwd);
    return longEnough || complexEnough;
  };

  const isEmailValid = (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});
    setError('');
    
    const errors = {};
    if (!isEmailValid(email)) {
      errors.email = 'Please enter a valid email address.';
    }
    if (username.length < 3) {
      errors.username = 'Username must be at least 3 characters long.';
    }
    if (!isPasswordStrong(password)) {
      errors.password = 'Password must be at least 15 characters OR at least 8 characters including a number and a lowercase letter.';
    }
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
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
          <div style={{ position: 'relative' }}>
            <input 
              type={showPassword ? "text" : "password"} 
              value={password} 
              onChange={e => setPassword(e.target.value)} 
              required 
              style={{ width: '100%', paddingRight: '40px' }}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              style={{
                position: 'absolute',
                right: '10px',
                top: '50%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#666',
                padding: 0,
                display: 'flex',
                alignItems: 'center'
              }}
              title={showPassword ? "Hide password" : "Show password"}
            >
              {showPassword ? <FaEyeSlash size={20} /> : <FaEye size={20} />}
            </button>
          </div>
          {fieldErrors.password && <div className="error-message" style={{fontSize: '0.85rem', marginTop: '0.25rem'}}>{fieldErrors.password}</div>}
        </div>
        <button type="submit" className="btn-primary" style={{ width: '100%' }}>Create Account</button>
      </form>

      <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
        <p style={{ margin: '0 0 1rem 0', color: '#666' }}>OR</p>
        <button 
          onClick={() => handleOAuthLogin('google')}
          className="btn-primary"
          style={{ width: '100%', marginBottom: '0.5rem', backgroundColor: '#db4437', borderColor: '#db4437' }}
        >
          Sign up with Google
        </button>
      </div>

      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}

export default Signup;