import { useState } from 'react';
import api from '../services/api';
import '../App.css';

function Transfer() {
  const [receiverEmail, setReceiverEmail] = useState('');
  const [amount, setAmount] = useState('');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();

    setMessage('');
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/transfers', {
        receiverEmail,
        amount: Number(amount)
      });

      setMessage(
        `Transfer successful. Reference ID: ${response.data}`
      );

      setReceiverEmail('');
      setAmount('');
    } catch (error) {
      setError(
        error.response?.data?.message ||
        'Transfer failed.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="dashboard-header">
        <h1 className="page-title">Transfer Money</h1>

        <p className="dashboard-subtitle">
          Send money securely to another wallet user
        </p>
      </div>

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

      <section className="transfer-container">
        <div className="form-card transfer-card">
          <h2>Send Money</h2>

          <p className="form-description">
            Enter the receiver's registered email and the
            amount you want to transfer.
          </p>

          <form onSubmit={handleSubmit}>
            <label htmlFor="receiverEmail">
              Receiver Email
            </label>

            <input
              id="receiverEmail"
              type="email"
              placeholder="receiver@example.com"
              value={receiverEmail}
              onChange={(event) =>
                setReceiverEmail(event.target.value)
              }
              required
            />

            <label htmlFor="transferAmount">
              Amount
            </label>

            <input
              id="transferAmount"
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Enter amount"
              value={amount}
              onChange={(event) =>
                setAmount(event.target.value)
              }
              required
            />

            <button
              className="primary-button"
              type="submit"
              disabled={loading}
            >
              {loading ? 'Processing...' : 'Send Money'}
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}

export default Transfer;