import { useEffect, useState } from 'react';
import api from '../services/api';
import '../App.css';

function Wallet() {
  const [balance, setBalance] = useState(null);

  const [depositAmount, setDepositAmount] = useState('');
  const [withdrawAmount, setWithdrawAmount] = useState('');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const [loading, setLoading] = useState(false);

  const loadWallet = async () => {
    try {
      const response = await api.get('/wallet');

      setBalance(response.data.balance);
    } catch (error) {
      setError(
        error.response?.data?.message ||
        'Failed to load wallet.'
      );
    }
  };

  useEffect(() => {
    const loadWalletData = async () => {
      try {
        const response = await api.get('/wallet');

        setBalance(response.data.balance);
      } catch (error) {
        setError(
          error.response?.data?.message ||
          'Failed to load wallet.'
        );
      }
    };

    loadWalletData();
  }, []);

  const handleDeposit = async (event) => {
    event.preventDefault();

    setMessage('');
    setError('');
    setLoading(true);

    try {
      await api.post('/wallet/deposit', {
        amount: Number(depositAmount)
      });

      setMessage('Deposit successful.');
      setDepositAmount('');

      await loadWallet();
    } catch (error) {
      setError(
        error.response?.data?.message ||
        'Deposit failed.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleWithdraw = async (event) => {
    event.preventDefault();

    setMessage('');
    setError('');
    setLoading(true);

    try {
      await api.post('/wallet/withdraw', {
        amount: Number(withdrawAmount)
      });

      setMessage('Withdrawal successful.');
      setWithdrawAmount('');

      await loadWallet();
    } catch (error) {
      setError(
        error.response?.data?.message ||
        'Withdrawal failed.'
      );
    } finally {
      setLoading(false);
    }
  };

  if (balance === null && !error) {
    return <p>Loading wallet...</p>;
  }

  return (
    <div>
      <div className="dashboard-header">
        <h1 className="page-title">Wallet</h1>

        <p className="dashboard-subtitle">
          Manage your wallet balance
        </p>
      </div>

      <section className="balance-card">
        <p className="card-label">Current Balance</p>

        <h2>₹{Number(balance).toFixed(2)}</h2>

        <p className="balance-description">
          Available wallet balance
        </p>
      </section>

      {message && (
        <div className="success-message">
          {message}
        </div>
      )}

      {error && (
        <div className="error-message-box">
          {error}
        </div>
      )}

      <section className="wallet-actions">
        <div className="form-card">
          <h2>Deposit Money</h2>

          <p className="form-description">
            Add money to your wallet.
          </p>

          <form onSubmit={handleDeposit}>
            <label htmlFor="depositAmount">
              Amount
            </label>

            <input
              id="depositAmount"
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Enter amount"
              value={depositAmount}
              onChange={(event) =>
                setDepositAmount(event.target.value)
              }
              required
            />

            <button
              className="primary-button"
              type="submit"
              disabled={loading}
            >
              {loading
                ? 'Processing...'
                : 'Deposit Money'}
            </button>
          </form>
        </div>

        <div className="form-card">
          <h2>Withdraw Money</h2>

          <p className="form-description">
            Withdraw money from your wallet.
          </p>

          <form onSubmit={handleWithdraw}>
            <label htmlFor="withdrawAmount">
              Amount
            </label>

            <input
              id="withdrawAmount"
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Enter amount"
              value={withdrawAmount}
              onChange={(event) =>
                setWithdrawAmount(event.target.value)
              }
              required
            />

            <button
              className="secondary-button"
              type="submit"
              disabled={loading}
            >
              {loading
                ? 'Processing...'
                : 'Withdraw Money'}
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}

export default Wallet;