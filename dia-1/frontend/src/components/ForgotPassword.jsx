import { useState } from 'react';
import { apiPostJson } from '../utils.js';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function ForgotPassword({ onSwitchPage, onOpenModal }) {
  const [forgotState, setForgotState] = useState('username');
  const [forgotUsername, setForgotUsername] = useState('');
  const [securityQuestion, setSecurityQuestion] = useState('');
  const [securityAnswer, setSecurityAnswer] = useState('');
  const [resetPasswordValue, setResetPasswordValue] = useState('');
  const [confirmResetPassword, setConfirmResetPassword] = useState('');

  const handleFetchSecurityQuestion = async () => {
    if (!forgotUsername) {
      onOpenModal('Please enter your username.');
      return;
    }
    try {
      const response = await fetch(`${API_BASE}/get-security-question?username=` + encodeURIComponent(forgotUsername), { credentials: 'include' });
      const data = await response.json();
      if (data.question) {
        setSecurityQuestion(data.question);
        setForgotState('question');
      } else {
        onOpenModal('User not found.');
      }
    } catch (error) {
      console.error(error);
      onOpenModal('Error fetching security question. Please try again.');
    }
  };

  const handleVerifySecurityAnswer = async () => {
    if (!securityAnswer) {
      onOpenModal('Please enter your security answer.');
      return;
    }
    try {
      const response = await apiPostJson('/verify-security-answer', {
        username: forgotUsername,
        securityAnswer,
      });
      const data = await response.json();
      if (data.success) {
        setForgotState('reset');
      } else {
        onOpenModal('Incorrect security answer.');
      }
    } catch (error) {
      console.error(error);
      onOpenModal('Error verifying the security answer.');
    }
  };

  const handleForgotReset = async () => {
    if (resetPasswordValue !== confirmResetPassword) {
      onOpenModal('Passwords do not match!');
      return;
    }
    try {
      const response = await apiPostJson('/reset-password', {
        username: forgotUsername,
        newPassword: resetPasswordValue,
      });
      const data = await response.json();
      onOpenModal(data.message || 'Password reset completed.');
      if (data.success) {
        setForgotState('username');
        setForgotUsername('');
        setSecurityAnswer('');
        setSecurityQuestion('');
        setResetPasswordValue('');
        setConfirmResetPassword('');
        onSwitchPage('login');
      }
    } catch (error) {
      console.error(error);
      onOpenModal('Error resetting password.');
    }
  };

  return (
    <div className="forgot-password form">
      <header>Forgot Password</header>
      <form id="forgot-password-form" onSubmit={(event) => event.preventDefault()}>
        {forgotState === 'username' && (
          <div id="username-container">
            <input
              type="text"
              id="username"
              name="username"
              placeholder="Enter your username"
              required
              value={forgotUsername}
              onChange={(event) => setForgotUsername(event.target.value)}
            />
            <button type="button" className="button" onClick={handleFetchSecurityQuestion}>
              Get Security Question
            </button>
          </div>
        )}
        {forgotState !== 'username' && (
          <>
            <div className="security-label">Security Question</div>
            <input type="text" id="security-question" disabled value={securityQuestion} />
            <input
              type="text"
              id="security-answer"
              name="securityAnswer"
              placeholder="Enter your answer"
              required
              value={securityAnswer}
              onChange={(event) => setSecurityAnswer(event.target.value)}
            />
            {forgotState === 'question' && (
              <button type="button" className="button" onClick={handleVerifySecurityAnswer}>
                Verify Answer
              </button>
            )}
          </>
        )}
        {forgotState === 'reset' && (
          <>
            <input
              type="password"
              id="new-password"
              name="newPassword"
              placeholder="Enter new password"
              required
              value={resetPasswordValue}
              onChange={(event) => setResetPasswordValue(event.target.value)}
            />
            <input
              type="password"
              id="confirm-password"
              placeholder="Confirm new password"
              required
              value={confirmResetPassword}
              onChange={(event) => setConfirmResetPassword(event.target.value)}
            />
            <button type="button" className="button" onClick={handleForgotReset}>
              Reset Password
            </button>
          </>
        )}
      </form>
      <div className="signup">
        <button type="button" className="button" onClick={() => {
          onSwitchPage('login');
          setForgotState('username');
        }}>
          Back to Login
        </button>
      </div>
    </div>
  );
}

export default ForgotPassword;