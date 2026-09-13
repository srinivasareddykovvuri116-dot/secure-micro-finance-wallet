import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import '../App.css';

function Navbar() {
  const { logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <Link to="/dashboard" className="navbar-brand">
        💰 WalletApp
      </Link>

      <div className="navbar-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/wallet">Wallet</Link>
        <Link to="/transfer">Transfer</Link>
        <Link to="/transactions">Transactions</Link>

        {isAdmin && (
          <Link to="/admin">Admin</Link>
        )}

        <button
          className="logout-button"
          onClick={handleLogout}
        >
          Logout
        </button>
      </div>
    </nav>
  );
}

export default Navbar;