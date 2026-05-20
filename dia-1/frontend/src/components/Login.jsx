import { useState } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function Login({ onSwitchPage, onOpenModal, onLoginSuccess }) {
  const [loginData, setLoginData] = useState({ username: '', password: '' });

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
        const errorMessage =
          (isJson && body?.message) ||
          (typeof body === 'string' && body) ||
          'Login failed. Please check your credentials.';
        onOpenModal(errorMessage);
      }
    } catch (error) {
      onOpenModal('Network error. Please try again.');
    }
  };

  return (
    <div className="auth-container">
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
        <button type="button" className="action-button secondary" style={{marginTop: '10px', width: '100%'}} onClick={() => onSwitchPage('signup')}>
          Signup
        </button>
        <button type="button" className="action-button secondary" style={{marginTop: '10px', width: '100%'}} onClick={() => onSwitchPage('view-rates')}>
          View Public Rates
        </button>
      </div>
    </div>
  );
}

export default Login;