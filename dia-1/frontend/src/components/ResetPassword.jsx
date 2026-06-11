import { useState } from 'react';
import { apiPostJson } from '../utils.js';

function ResetPassword({ onSwitchPage, onOpenModal }) {
  const [step, setStep] = useState('credentials');
  const [resetNsData, setResetNsData] = useState({
    username: '',
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const handleVerifyCredentials = async (event) => {
    event.preventDefault();
    if (!resetNsData.username || !resetNsData.oldPassword) {
      onOpenModal('Please enter both username and current password.');
      return;
    }
    try {
      const response = await apiPostJson('/verify-credentials', {
        username: resetNsData.username,
        password: resetNsData.oldPassword
      });
      if (!response.ok) {
        const body = await response.json();
        throw new Error(body.message || 'Invalid username or password.');
      }
      setStep('reset');
    } catch (error) {
      console.error(error);
      onOpenModal(error.message || 'Invalid username or password.');
    }
  };

  const handleResetPassword = async (event) => {
    event.preventDefault();
    if (resetNsData.newPassword !== resetNsData.confirmPassword) {
      onOpenModal('Passwords do not match. Please try again.');
      return;
    }
    try {
      const response = await apiPostJson('/reset-ns-password', resetNsData);
      if (!response.ok) {
        const body = await response.json();
        throw new Error(body.error || 'Failed to reset password.');
      }
      const data = await response.json();
      onOpenModal(data.message || 'Password reset successful!');
      setResetNsData({
        username: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setStep('credentials');
      onSwitchPage('login');
    } catch (error) {
      console.error(error);
      onOpenModal(error.message || 'Something went wrong. Please try again.');
    }
  };

  return (
    <div className="reset-password form">
      <header>Reset Password</header>
      {step === 'credentials' ? (
        <form id="reset-password-form" onSubmit={handleVerifyCredentials}>
          <input
            type="text"
            id="usernameNs"
            name="username"
            placeholder="Enter your username"
            required
            value={resetNsData.username}
            onChange={(event) => setResetNsData({ ...resetNsData, username: event.target.value })}
          />
          <input
            type="password"
            name="oldPassword"
            id="oldPassword"
            placeholder="Enter current password"
            required
            value={resetNsData.oldPassword}
            onChange={(event) => setResetNsData({ ...resetNsData, oldPassword: event.target.value })}
          />
          <input type="submit" className="button" value="Verify Credentials" />
        </form>
      ) : (
        <form id="reset-password-form" onSubmit={handleResetPassword}>
          <div style={{ marginBottom: '15px', fontSize: '1rem', color: '#000', fontWeight: 'bold' }}>
            Username: {resetNsData.username}
          </div>
          <input
            type="password"
            id="newPassword"
            name="newPassword"
            placeholder="Enter new password"
            required
            value={resetNsData.newPassword}
            onChange={(event) => setResetNsData({ ...resetNsData, newPassword: event.target.value })}
          />
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            placeholder="Confirm new password"
            required
            value={resetNsData.confirmPassword}
            onChange={(event) => setResetNsData({ ...resetNsData, confirmPassword: event.target.value })}
          />
          <div style={{ display: 'flex', gap: '10px' }}>
            <input type="submit" className="button" value="Reset Password" style={{ flex: 1 }} />
            <button
              type="button"
              className="button"
              style={{ flex: 1, backgroundColor: '#6c757d' }}
              onClick={() => setStep('credentials')}
            >
              Back
            </button>
          </div>
        </form>
      )}
      <div className="signup">
        <button type="button" className="button" onClick={() => onSwitchPage('login')}>
          Back to Login
        </button>
      </div>
    </div>
  );
}

export default ResetPassword;