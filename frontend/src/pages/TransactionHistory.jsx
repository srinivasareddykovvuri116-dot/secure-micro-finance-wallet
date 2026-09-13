import { useEffect, useState } from 'react';
import api from '../services/api';
import '../App.css';

function TransactionHistory() {
  const [transactions, setTransactions] = useState([]);

  const [type, setType] = useState('');
  const [status, setStatus] = useState('');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadTransactions = async () => {
      setLoading(true);
      setError('');

      try {
        const params = {
          page,
          size: 10,
          sort: 'createdAt,desc'
        };

        if (type) {
          params.type = type;
        }

        if (status) {
          params.status = status;
        }

        const response = await api.get('/transactions', {
          params
        });

        setTransactions(response.data.content);
        setTotalPages(response.data.totalPages);
      } catch (error) {
        setError(
          error.response?.data?.message ||
          'Failed to load transactions.'
        );
      } finally {
        setLoading(false);
      }
    };

    loadTransactions();
  }, [page, type, status]);

  const handleTypeChange = (event) => {
    setType(event.target.value);
    setPage(0);
  };

  const handleStatusChange = (event) => {
    setStatus(event.target.value);
    setPage(0);
  };

  if (loading) {
    return <p>Loading transactions...</p>;
  }

  return (
    <div>
      <div className="dashboard-header">
        <h1 className="page-title">Transaction History</h1>

        <p className="dashboard-subtitle">
          View and filter your wallet transactions
        </p>
      </div>

      <section className="transaction-filters">
        <div>
          <label htmlFor="type">Transaction Type</label>

          <select
            id="type"
            value={type}
            onChange={handleTypeChange}
          >
            <option value="">All Types</option>
            <option value="DEPOSIT">Deposit</option>
            <option value="WITHDRAWAL">Withdrawal</option>
            <option value="TRANSFER_SENT">
              Transfer Sent
            </option>
            <option value="TRANSFER_RECEIVED">
              Transfer Received
            </option>
          </select>
        </div>

        <div>
          <label htmlFor="status">Status</label>

          <select
            id="status"
            value={status}
            onChange={handleStatusChange}
          >
            <option value="">All Statuses</option>
            <option value="SUCCESS">Success</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>
      </section>

      {error && (
        <div className="error-message-box">
          {error}
        </div>
      )}

      <section className="transaction-card">
        {transactions.length === 0 ? (
          <div className="empty-state">
            <h2>No Transactions</h2>
            <p>
              No transactions match your selected filters.
            </p>
          </div>
        ) : (
          <div className="table-container">
            <table className="transaction-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Reference ID</th>
                  <th>Date</th>
                </tr>
              </thead>

              <tbody>
                {transactions.map((transaction) => (
                  <tr key={transaction.id}>
                    <td>
                      <span className="transaction-type">
                        {transaction.type.replaceAll('_', ' ')}
                      </span>
                    </td>

                    <td className="transaction-amount">
                      ₹{Number(transaction.amount).toFixed(2)}
                    </td>

                    <td>
                      <span
                        className={`status-badge ${
                          transaction.status === 'SUCCESS'
                            ? 'status-success'
                            : 'status-failed'
                        }`}
                      >
                        {transaction.status}
                      </span>
                    </td>

                    <td>
                      <span className="reference-id">
                        {transaction.referenceId}
                      </span>
                    </td>

                    <td>
                      {new Date(
                        transaction.createdAt
                      ).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {totalPages > 0 && (
        <div className="pagination">
          <button
            className="pagination-button"
            onClick={() => setPage(page - 1)}
            disabled={page === 0}
          >
            Previous
          </button>

          <span>
            Page {page + 1} of {totalPages}
          </span>

          <button
            className="pagination-button"
            onClick={() => setPage(page + 1)}
            disabled={page >= totalPages - 1}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}

export default TransactionHistory;