import React from 'react';

export default function Dashboard({ user, onLogout }) {
  return (
    <div className="dashboard-container glass-panel">
      <div className="dashboard-header">
        <div className="dashboard-title">
          <h2>Secure Portal</h2>
          <p>Welcome back, {user.username}</p>
        </div>
        <button onClick={onLogout} className="btn-logout">
          Sign Out
        </button>
      </div>

      <div className="info-cards">
        <div className="card">
          <h3>Authentication Status</h3>
          <p style={{ color: '#34d399' }}>Verified ✓</p>
        </div>
        
        <div className="card">
          <h3>Active Role</h3>
          <p><span className="role-badge">{user.role}</span></p>
        </div>

        <div className="card" style={{ gridColumn: '1 / -1' }}>
          <h3>Session Token (JWT)</h3>
          <p style={{ fontSize: '0.875rem', color: '#94a3b8', fontFamily: 'monospace' }}>
            {user.token.substring(0, 40)}...{user.token.substring(user.token.length - 20)}
          </p>
        </div>
      </div>
    </div>
  );
}
