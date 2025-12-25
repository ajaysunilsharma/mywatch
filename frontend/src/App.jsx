import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Leaderboard from './pages/Leaderboard';
import AddWatch from './pages/AddWatch';
import EditWatch from './pages/EditWatch';
import WatchDetail from './pages/WatchDetail';
import './App.css';

function App() {
  return (
    <Router>
      <div className="app">
        <Header />
        <Routes>
          <Route path="/" element={<Leaderboard />} />
          <Route path="/add" element={<AddWatch />} />
          <Route path="/edit/:id" element={<EditWatch />} />
          <Route path="/watch/:id" element={<WatchDetail />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
