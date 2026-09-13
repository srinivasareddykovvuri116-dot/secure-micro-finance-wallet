import { Routes, Route } from 'react-router-dom';

import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Wallet from './pages/Wallet';
import Transfer from './pages/Transfer';
import TransactionHistory from './pages/TransactionHistory';
import AdminDashboard from './pages/AdminDashboard';

import ProtectedLayout from './layouts/ProtectedLayout';

function Home() {
  return (
    <div>
      <h1>Secure Micro-Finance & Wallet</h1>

      <nav>
        <a href="/login">Login</a>
        {' | '}
        <a href="/register">Register</a>
      </nav>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />

      <Route path="/login" element={<Login />} />

      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedLayout />}>
        <Route
          path="/dashboard"
          element={<Dashboard />}
        />

        <Route
          path="/wallet"
          element={<Wallet />}
        />

        <Route
          path="/transfer"
          element={<Transfer />}
        />

        <Route
          path="/transactions"
          element={<TransactionHistory />}
        />

        <Route
          path="/admin"
          element={<AdminDashboard />}
        />
      </Route>
    </Routes>
  );
}

export default App;