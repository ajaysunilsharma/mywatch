import { Link } from 'react-router-dom';
import './Header.css';

function Header() {
  return (
    <header className="header">
      <div className="container">
        <div className="header-content">
          <Link to="/" className="logo">
            <h1>⌚ GOAT Watches</h1>
          </Link>
          <nav className="nav">
            <Link to="/" className="nav-link">Home</Link>
            <Link to="/add" className="nav-link btn-primary">Add Watch</Link>
          </nav>
        </div>
      </div>
    </header>
  );
}

export default Header;
