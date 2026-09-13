import { useEffect, useState } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';

import api from '../services/api';
import '../App.css';

function Dashboard() {
  const [wallet, setWallet] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [trends, setTrends] = useState([]);

  const [error, setError] = useState('');

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const [
          walletResponse,
          analyticsResponse,
          trendsResponse
        ] = await Promise.all([
          api.get('/wallet'),
          api.get('/analytics'),
          api.get('/analytics/trends')
        ]);

        setWallet(walletResponse.data);
        setAnalytics(analyticsResponse.data);
        setTrends(trendsResponse.data);

        console.log('Trends response:', trendsResponse.data);
      } catch (error) {
        setError(
          error.response?.data?.message ||
          'Failed to load dashboard.'
        );
      }
    };

    loadDashboard();
  }, []);

  if (error) {
    return <p className="error-message">{error}</p>;
  }

  if (!wallet || !analytics) {
    return <p>Loading dashboard...</p>;
  }

  return (
    <div>
      <div className="dashboard-header">
        <div>
          <h1 className="page-title">Dashboard</h1>

          <p className="dashboard-subtitle">
            Overview of your wallet and finances
          </p>
        </div>
      </div>

      <section className="balance-card">
        <p className="card-label">Current Balance</p>

        <h2>
          ₹{Number(wallet.balance).toFixed(2)}
        </h2>

        <p className="balance-description">
          Available wallet balance
        </p>
      </section>

      <section className="stats-grid">
        <div className="stat-card">
          <p className="card-label">Total Deposits</p>

          <h3>
            ₹{Number(analytics.totalDeposits).toFixed(2)}
          </h3>

          <p>{analytics.depositCount} transactions</p>
        </div>

        <div className="stat-card">
          <p className="card-label">Total Withdrawals</p>

          <h3>
            ₹{Number(analytics.totalWithdrawals).toFixed(2)}
          </h3>

          <p>
            {analytics.withdrawalCount} transactions
          </p>
        </div>

        <div className="stat-card">
          <p className="card-label">Total Sent</p>

          <h3>
            ₹{Number(analytics.totalSent).toFixed(2)}
          </h3>

          <p>{analytics.sentCount} transfers</p>
        </div>

        <div className="stat-card">
          <p className="card-label">Total Received</p>

          <h3>
            ₹{Number(analytics.totalReceived).toFixed(2)}
          </h3>

          <p>{analytics.receivedCount} transfers</p>
        </div>
      </section>

      <section className="chart-card">
        <div className="chart-header">
          <h2>Cash Flow</h2>

          <p>
            Daily money coming in and going out
          </p>
        </div>

        {trends.length === 0 ? (
          <div className="empty-state">
            <h2>No Cash Flow Data</h2>

            <p>
              Complete a transaction to see your cash flow.
            </p>
          </div>
        ) : (
          <div
            className="chart-container"
            style={{ width: '100%', height: '350px' }}
            >
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trends}>
                <CartesianGrid strokeDasharray="3 3" />

                <XAxis dataKey="date" />

                <YAxis />

                <Tooltip />

                <Legend />

                <Line
                  type="monotone"
                  dataKey="inflow"
                  name="Inflow"
                  stroke="#16a34a"
                  strokeWidth={2}
                  dot={{ r: 5 }}
                />

                <Line
                  type="monotone"
                  dataKey="outflow"
                  name="Outflow"
                  stroke="#dc2626"
                  strokeWidth={2}
                  dot={{ r: 5 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>
    </div>
  );
}

export default Dashboard;