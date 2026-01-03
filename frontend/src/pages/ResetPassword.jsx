import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import api from '../utils/api';

const API_URL = import.meta.env.VITE_API_URL;

function ResetPassword() {
  const [userInfo, setUserInfo] = useState({ username: '', email: '' });
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  useEffect(() => {
    if (token) {
      api.get(`/auth/reset-password/validate?token=${token}`)
        .then(res => setUserInfo(res.data))
        .catch(() => setError('Invalid or expired reset link.'));
    }
  }, [token]);

  const isPasswordStrong = (pwd) => {
    // At least 15 chars OR (at least 8 chars including a number and a lowercase letter)
    const longEnough = pwd.length >= 15;
    const complexEnough = pwd.length >= 8 && /[a-z]/.test(pwd) && /[0-9]/.test(pwd);
    return longEnough || complexEnough;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!isPasswordStrong(password)) {
      setError('Password must be at least 15 characters OR at least 8 characters including a number and a lowercase letter.');
      return;
    }

    try {
      await api.post('/auth/reset-password', { token, newPassword: password });
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.error || 'Reset failed');
    }
  };

  if (!token) {
    return <div className="container" style={{ marginTop: '2rem' }}>Invalid reset link.</div>;
  }

  return (
    <div className="container" style={{ maxWidth: '400px', marginTop: '2rem' }}>
      <h2>Reset Password</h2>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Username</label>
          <input type="text" value={userInfo.username} readOnly disabled style={{ backgroundColor: '#f0f0f0' }} />
        </div>
        <div className="form-group">
          <label>Email</label>
          <input type="text" value={userInfo.email} readOnly disabled style={{ backgroundColor: '#f0f0f0' }} />
        </div>
        <div className="form-group">
          <label>New Password</label>
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
        <button type="submit" className="btn-primary" style={{ width: '100%' }}>Reset Password</button>
      </form>
      <p style={{ marginTop: '1rem', textAlign: 'center' }}>
        <Link to="/login">Back to Login</Link>
      </p>
    </div>
  );
}

export default ResetPassword;