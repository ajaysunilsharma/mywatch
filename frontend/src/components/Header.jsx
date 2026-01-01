import { Link } from 'react-router-dom';
import SearchBar from './SearchBar';
import './Header.css';

function Header({ user, onLogout }) {
  return (
    <header className="header">
      <div className="container">
        <div className="header-content">
          <Link to="/" className="logo">
            <h1>⌚ GOAT Watches</h1>
          </Link>
          <div className="header-search">
            <SearchBar />
          </div>
          <nav className="nav">
            <Link to="/" className="nav-link">Home</Link>
            {user && user.role === 'ROLE_ADMIN' && <Link to="/add" className="nav-link btn-primary">Add Watch</Link>}
            <span className="nav-link">{user.username}</span>
            <button onClick={onLogout} className="nav-link" style={{ background: 'transparent', border: 'none', cursor: 'pointer', fontSize: 'inherit', fontFamily: 'inherit', color: 'inherit' }}>Logout</button>
          </nav>
        </div>
      </div>
    </header>
  );
}

export default Header;
