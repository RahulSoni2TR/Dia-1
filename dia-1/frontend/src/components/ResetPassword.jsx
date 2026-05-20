import { useState } from 'react';
import { apiPostJson } from '../utils.js';

function ResetPassword({ onSwitchPage, onOpenModal }) {
  const [resetNsData, setResetNsData] = useState({
    username: '',
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const handleSubmit = async (event) => {
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
    } catch (error) {
      console.error(error);
      onOpenModal(error.message || 'Something went wrong. Please try again.');
    }
  };

  return (
    <div className="reset-password form">
      <header>Reset Password</header>
      <form id="reset-password-form" onSubmit={handleSubmit}>
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
        <input type="submit" className="button" value="Reset Password" />
      </form>
      <div className="signup">
        <button type="button" className="button" onClick={() => onSwitchPage('login')}>
          Back to Login
        </button>
      </div>
    </div>
  );
}

export default ResetPassword;