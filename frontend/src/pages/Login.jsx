import { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import api from '../utils/api';

const API_URL = import.meta.env.VITE_API_URL;

function Login({ setUser }) {
  const location = useLocation();
  const [username, setUsername] = useState(location.state?.email || '');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [isResetMode, setIsResetMode] = useState(false);
  const navigate = useNavigate();

  const handleOAuthLogin = (provider) => {
    window.location.href = `${API_URL}/oauth2/authorization/${provider}`;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/auth/login', { username, password });
      // Trigger a reload or fetch user info in App
      window.location.href = '/'; 
    } catch (err) {
      setError('Incorrect username or password');
    }
  };

  const handleResetSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    try {
      await api.post('/auth/forgot-password', { email: username });
      setMessage('A reset link has been sent to your registered email address');
    } catch (err) {
      setError('Request failed. Please try again.');
    }
  };

  return (
    <div className="container" style={{ maxWidth: '400px', marginTop: '2rem' }}>
      <h2>{isResetMode ? 'Reset Password' : 'Login'}</h2>
      {message && <div style={{ color: 'green', marginBottom: '1rem' }}>{message}</div>}
      {error && <div className="error-message">{error}</div>}
      
      <form onSubmit={isResetMode ? handleResetSubmit : handleSubmit}>
        {!isResetMode && (
          <div className="form-group">
            <label>Email or username*</label>
            <input 
              type="text" 
              value={username} 
              onChange={e => setUsername(e.target.value)} 
              required 
              readOnly={!!location.state?.email}
              style={location.state?.email ? { backgroundColor: '#f0f0f0', color: '#666' } : {}}
            />
          </div>
        )}
        
        {!isResetMode && (
          <>
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
            </div>
            <div style={{ textAlign: 'right', marginBottom: '1rem' }}>
              <button 
                type="button" 
                onClick={() => { 
                  if (!username) {
                    setError('Please enter your email or username first');
                    return;
                  }
                  setIsResetMode(true); 
                  setError(''); 
                  setMessage(''); 
                }}
                style={{ background: 'none', border: 'none', padding: 0, fontSize: '0.9rem', color: '#666', cursor: 'pointer', textDecoration: 'underline' }}
              >
                Forgot Password?
              </button>
            </div>
          </>
        )}

        <button type="submit" className="btn-primary" style={{ width: '100%' }}>
          {isResetMode ? 'Send Reset Link' : 'Sign In'}
        </button>
      </form>
      
      {!isResetMode && (
        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
          <p style={{ margin: '0 0 1rem 0', color: '#666' }}>OR</p>
          <button 
            onClick={() => handleOAuthLogin('google')}
            className="btn-primary"
            style={{ width: '100%', marginBottom: '0.5rem', backgroundColor: '#db4437', borderColor: '#db4437' }}
          >
            Sign in with Google
          </button>
        </div>
      )}

      {isResetMode && (
        <p style={{ marginTop: '1rem', textAlign: 'center' }}>
          <button 
            onClick={() => { setIsResetMode(false); setError(''); setMessage(''); }}
            style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}
          >
            Back to Login
          </button>
        </p>
      )}

      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        Don't have an account? <Link to="/signup">Sign up</Link>
      </p>
    </div>
  );
}

export default Login;