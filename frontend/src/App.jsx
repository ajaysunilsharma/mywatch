import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Header from './components/Header';
import Leaderboard from './pages/Leaderboard';
import AddWatch from './pages/AddWatch';
import EditWatch from './pages/EditWatch';
import WatchDetail from './pages/WatchDetail';
import Login from './pages/Login';
import Signup from './pages/Signup';
import ResetPassword from './pages/ResetPassword';
import SetUsername from './pages/SetUsername';
import api from './utils/api';
import './App.css';

const API_URL = import.meta.env.VITE_API_URL;

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const res = await api.get('/auth/me');
        setUser(res.data);
      } catch (e) {
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    checkAuth();
  }, []);

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Logout failed', error);
    }
    setUser(null);
    window.location.href = '/login';
  };

  if (loading) {
    return <div className="container">Loading...</div>;
  }

  return (
    <Router>
      <div className="app">
        {user && user.role === 'ROLE_PRE_AUTH' ? (
          <Routes>
            <Route path="/oauth/set-username" element={<SetUsername setUser={setUser} />} />
            <Route path="*" element={<Navigate to="/oauth/set-username" />} />
          </Routes>
        ) : user ? (
          <>
            <Header user={user} onLogout={handleLogout} />
            <Routes>
              <Route path="/" element={<Leaderboard user={user} />} />
              <Route path="/add" element={<AddWatch />} />
              <Route path="/edit/:id" element={<EditWatch />} />
              <Route path="/watch/:id" element={<WatchDetail user={user} />} />
              <Route path="/health" element={<div>OK</div>} />
              <Route path="*" element={<Navigate to="/" />} />
            </Routes>
          </>
        ) : (
          <Routes>
            <Route path="/login" element={<Login setUser={setUser} />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/oauth/set-username" element={<SetUsername setUser={setUser} />} />
            <Route path="*" element={<Navigate to="/login" />} />
          </Routes>
        )}
      </div>
    </Router>
  );
}

export default App;
