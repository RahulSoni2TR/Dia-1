import { useState } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function Login({ onSwitchPage, onOpenModal, onLoginSuccess, licenseStatus, licenseDetails, onLicenseActivated }) {
  const [loginData, setLoginData] = useState({ username: '', password: '' });
  const [activationKey, setActivationKey] = useState('');
  const [activating, setActivating] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      const response = await fetch(`${API_BASE}/process-login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams(loginData).toString(),
        credentials: 'include', // Include cookies
      });

      const contentType = response.headers.get('content-type') || '';
      const isJson = contentType.includes('application/json');
      const body = isJson ? await response.json() : await response.text();

      if (response.ok && isJson && body.success) {
        const userData = { username: body.username, roles: body.roles || [] };
        onLoginSuccess(userData);
        onSwitchPage('home');
      } else {
        if (response.status === 402 || (isJson && (body?.error === 'TRIAL_EXPIRED' || body?.status === 'EXPIRED'))) {
          if (onLicenseActivated) {
            onLicenseActivated();
          }
        } else {
          const errorMessage =
            (isJson && body?.message) ||
            (typeof body === 'string' && body) ||
            'Login failed. Please check your credentials.';
          onOpenModal(errorMessage);
        }
      }
    } catch (error) {
      onOpenModal('Network error. Please try again.');
    }
  };

  const handleActivate = async (e) => {
    e.preventDefault();
    setActivating(true);
    try {
      const response = await fetch(`${API_BASE}/api/license/activate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ licenseKey: activationKey }),
      });
      const data = await response.json();
      if (response.ok && data.success) {
        onOpenModal('License activated successfully! App is now unlocked.');
        if (onLicenseActivated) {
          onLicenseActivated();
        }
      } else {
        onOpenModal(data.message || 'Invalid activation key.');
      }
    } catch (error) {
      onOpenModal('Activation failed. Connection error.');
    } finally {
      setActivating(false);
    }
  };

  const isLocked = licenseStatus === 'EXPIRED' || licenseStatus === 'TAMPERED';

  if (isLocked) {
    return (
      <div className="auth-container" style={{ maxWidth: '500px' }}>
        <h1 style={{ color: '#ff4d4d' }}>Activation Required</h1>
        <div style={{
          backgroundColor: '#ffe6e6',
          color: '#d93838',
          padding: '15px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '1px solid #ffcccc',
          fontSize: '14px',
          lineHeight: '1.5',
          textAlign: 'center',
          fontWeight: '500'
        }}>
          {licenseStatus === 'TAMPERED'
            ? 'System tampering or clock rollback detected. Please reach out to the system administrator to restore access.'
            : 'Your trial has expired. Please reach out to the system administrator to get the full version.'}
        </div>

        <div style={{ marginBottom: '20px', fontSize: '14px' }}>
          <strong>Machine ID:</strong>
          <div style={{
            fontFamily: 'monospace',
            background: '#f4f4f4',
            padding: '8px 12px',
            borderRadius: '4px',
            marginTop: '5px',
            border: '1px solid #ddd',
            userSelect: 'all',
            textAlign: 'center',
            fontSize: '16px',
            fontWeight: 'bold',
            letterSpacing: '1px'
          }}>
            {licenseDetails?.machineId || 'Retrieving Machine ID...'}
          </div>
          <span style={{ fontSize: '11px', color: '#666', display: 'block', marginTop: '5px', textAlign: 'center' }}>
            Provide this Machine ID to the administrator to obtain an activation key.
          </span>
        </div>

        <form className="auth-form" onSubmit={handleActivate}>
          <textarea
            style={{
              width: '100%',
              minHeight: '80px',
              padding: '10px',
              borderRadius: '6px',
              border: '1px solid #ccc',
              fontFamily: 'monospace',
              fontSize: '12px',
              marginBottom: '15px',
              resize: 'vertical',
              boxSizing: 'border-box'
            }}
            placeholder="Paste your activation key here..."
            required
            value={activationKey}
            onChange={(e) => setActivationKey(e.target.value)}
            disabled={activating}
          />
          <input
            type="submit"
            className="action-button"
            value={activating ? "Activating..." : "Activate License"}
            disabled={activating}
          />
        </form>
      </div>
    );
  }

  const showTrialBanner = licenseStatus === 'TRIAL';

  return (
    <div className="auth-container">
      {showTrialBanner && (
        <div style={{
          backgroundColor: '#e6f4ea',
          color: '#137333',
          padding: '10px 15px',
          borderRadius: '6px',
          marginBottom: '20px',
          border: '1px solid #ceead6',
          fontSize: '13px',
          textAlign: 'center',
          fontWeight: '500',
          width: '100%',
          boxSizing: 'border-box'
        }}>
          Running in Trial Mode. <strong>{licenseDetails?.daysRemaining} days remaining</strong>.
        </div>
      )}
      <h1>Login</h1>
      <form className="auth-form" onSubmit={handleSubmit}>
        <input
          type="text"
          name="username"
          placeholder="Enter your email"
          required
          value={loginData.username}
          onChange={(event) => setLoginData({ ...loginData, username: event.target.value })}
        />
        <input
          type="password"
          name="password"
          placeholder="Enter your password"
          required
          value={loginData.password}
          onChange={(event) => setLoginData({ ...loginData, password: event.target.value })}
        />
        <input type="submit" className="action-button" value="Login" />
      </form>
      <div className="auth-link-container">
        <button type="button" className="dropdown-item" onClick={() => onSwitchPage('forgot')}>
          Forgot password?
        </button>
        <button type="button" className="dropdown-item" onClick={() => onSwitchPage('reset')}>
          Reset password?
        </button>
        <button type="button" className="action-button secondary" style={{ marginTop: '10px', width: '100%' }} onClick={() => onSwitchPage('signup')}>
          Signup
        </button>
        <button type="button" className="action-button secondary" style={{ marginTop: '10px', width: '100%' }} onClick={() => onSwitchPage('view-rates')}>
          View Public Rates
        </button>
      </div>
    </div>
  );
}

export default Login;