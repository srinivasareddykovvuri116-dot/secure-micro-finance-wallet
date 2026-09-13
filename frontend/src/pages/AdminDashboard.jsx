import { useEffect, useState } from 'react';
import api from '../services/api';
import '../App.css';

function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);

  const [loading, setLoading] = useState(true);
  const [auditLoading, setAuditLoading] = useState(true);

  const [actionUserId, setActionUserId] = useState(null);

  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const loadUsers = async () => {
    try {
      const response = await api.get('/admin/users');
      setUsers(response.data);
    } catch (error) {
      setError(
        error.response?.data?.message ||
        'Failed to load users.'
      );
    } finally {
      setLoading(false);
    }
  };

  const loadAuditLogs = async () => {
    try {
      const response = await api.get('/admin/audit-logs');
      setAuditLogs(response.data);
    } catch (error) {
      setError(
        error.response?.data?.message ||
        'Failed to load audit logs.'
      );
    } finally {
      setAuditLoading(false);
    }
  };

  useEffect(() => {
    const loadAdminData = async () => {
      await Promise.all([
        loadUsers(),
        loadAuditLogs()
      ]);
    };

    loadAdminData();
  }, []);

  const handleStatusChange = async (user) => {
    setError('');
    setMessage('');
    setActionUserId(user.id);

    try {
      await api.put(
        `/admin/users/${user.id}/status`,
        null,
        {
          params: {
            active: !user.active
          }
        }
      );

      setMessage(
        `User ${user.id} ${
          user.active ? 'suspended' : 'activated'
        } successfully.`
      );

      await loadUsers();
      await loadAuditLogs();
    } catch (error) {
      setError(
        error.response?.data?.message ||
        'Failed to update user status.'
      );
    } finally {
      setActionUserId(null);
    }
  };

  if (loading) {
    return <p>Loading admin dashboard...</p>;
  }

  return (
    <div>
      {/* Header */}
      <div className="dashboard-header">
        <h1 className="page-title">
          Admin Dashboard
        </h1>

        <p className="dashboard-subtitle">
          Manage users and monitor the platform
        </p>
      </div>

      {/* Messages */}
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

      {/* Statistics */}
      <section className="stats-grid">
        <div className="stat-card">
          <p className="card-label">
            Total Users
          </p>

          <h3>{users.length}</h3>

          <p>Registered accounts</p>
        </div>

        <div className="stat-card">
          <p className="card-label">
            Active Users
          </p>

          <h3>
            {users.filter(
              (user) => user.active
            ).length}
          </h3>

          <p>Currently active</p>
        </div>

        <div className="stat-card">
          <p className="card-label">
            Suspended Users
          </p>

          <h3>
            {users.filter(
              (user) => !user.active
            ).length}
          </h3>

          <p>Currently suspended</p>
        </div>

        <div className="stat-card">
          <p className="card-label">
            Administrators
          </p>

          <h3>
            {
              users.filter(
                (user) => user.role === 'ROLE_ADMIN'
              ).length
            }
          </h3>

          <p>Admin accounts</p>
        </div>
      </section>

      {/* User Management */}
      <section className="transaction-card admin-users-card">
        <div className="admin-section-header">
          <div>
            <h2>User Management</h2>

            <p>
              Manage registered wallet users
            </p>
          </div>
        </div>

        {users.length === 0 ? (
          <div className="empty-state">
            <h2>No Users</h2>

            <p>
              No registered users found.
            </p>
          </div>
        ) : (
          <div className="table-container">
            <table className="transaction-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Full Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.id}</td>

                    <td>
                      <span className="transaction-type">
                        {user.fullName}
                      </span>
                    </td>

                    <td>{user.email}</td>

                    <td>
                      <span className="role-badge">
                        {user.role === 'ROLE_ADMIN'
                          ? 'Admin'
                          : 'User'}
                      </span>
                    </td>

                    <td>
                      <span
                        className={`status-badge ${
                          user.active
                            ? 'status-success'
                            : 'status-failed'
                        }`}
                      >
                        {user.active
                          ? 'ACTIVE'
                          : 'SUSPENDED'}
                      </span>
                    </td>

                    <td>
                      <button
                        className="admin-action-button"
                        onClick={() =>
                          handleStatusChange(user)
                        }
                        disabled={
                          actionUserId === user.id
                        }
                      >
                        {actionUserId === user.id
                          ? 'Updating...'
                          : user.active
                            ? 'Suspend'
                            : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Audit Logs */}
      <section className="transaction-card admin-users-card">
        <div className="admin-section-header">
          <div>
            <h2>Audit Logs</h2>

            <p>
              Track important administrative and
              wallet activities
            </p>
          </div>
        </div>

        {auditLoading ? (
          <div className="empty-state">
            <p>Loading audit logs...</p>
          </div>
        ) : auditLogs.length === 0 ? (
          <div className="empty-state">
            <h2>No Audit Logs</h2>

            <p>
              No audit activity has been recorded yet.
            </p>
          </div>
        ) : (
          <div className="table-container">
            <table className="transaction-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User ID</th>
                  <th>Action</th>
                  <th>Details</th>
                  <th>Timestamp</th>
                </tr>
              </thead>

              <tbody>
                {auditLogs.map((log) => (
                  <tr key={log.id}>
                    <td>{log.id}</td>

                    <td>
                      {log.userId ?? '-'}
                    </td>

                    <td>
                      <span className="transaction-type">
                        {log.action}
                      </span>
                    </td>

                    <td>
                      {log.details || '-'}
                    </td>

                    <td className="audit-timestamp">
                      {new Date(
                        log.createdAt
                      ).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default AdminDashboard;