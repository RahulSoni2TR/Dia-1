import { useState } from 'react';
import { apiPostForm } from '../utils.js';

const securityQuestions = [
  'What is the name of your first pet?',
  'What is your maiden name?',
  'In what city were you born?',
  'What was the name of your first school?',
  'What was your childhood nickname?',
  'Anything Else!'
];

function Signup({ onSwitchPage, onOpenModal }) {
  const [registerData, setRegisterData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    securityQuestion: '',
    securityAnswer: '',
  });

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (registerData.password !== registerData.confirmPassword) {
      onOpenModal('Passwords do not match!');
      return;
    }
    const response = await apiPostForm('/register', {
      username: registerData.username,
      password: registerData.password,
      securityQuestion: registerData.securityQuestion,
      securityAnswer: registerData.securityAnswer,
    });
    const text = await response.text();
    if (response.ok) {
      onOpenModal('User registered successfully!');
      setRegisterData({ username: '', password: '', confirmPassword: '', securityQuestion: '', securityAnswer: '' });
      onSwitchPage('login');
      return;
    }
    onOpenModal(text || 'Registration failed.');
  };

  return (
    <div className="registration form">
      <header>Signup</header>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          id="usernameS"
          name="username"
          placeholder="Enter your email"
          required
          value={registerData.username}
          onChange={(event) => setRegisterData({ ...registerData, username: event.target.value })}
        />
        <input
          type="password"
          id="password"
          name="password"
          placeholder="Create a password"
          required
          value={registerData.password}
          onChange={(event) => setRegisterData({ ...registerData, password: event.target.value })}
        />
        <input
          type="password"
          id="confirmSPassword"
          name="confirmPassword"
          placeholder="Confirm your password"
          required
          value={registerData.confirmPassword}
          onChange={(event) => setRegisterData({ ...registerData, confirmPassword: event.target.value })}
        />
        <select
          name="securityQuestion"
          id="securityQuestion"
          required
          value={registerData.securityQuestion}
          onChange={(event) => setRegisterData({ ...registerData, securityQuestion: event.target.value })}
        >
          <option value="" disabled>
            Select a security question
          </option>
          {securityQuestions.map((question) => (
            <option key={question} value={question}>
              {question}
            </option>
          ))}
        </select>
        <input
          type="text"
          id="securityAnswer"
          name="securityAnswer"
          placeholder="Your answer"
          required
          value={registerData.securityAnswer}
          onChange={(event) => setRegisterData({ ...registerData, securityAnswer: event.target.value })}
        />
        <input type="submit" className="button" value="Signup" />
      </form>
      <div className="signup">
        <span className="signup">
          Already have an account?{' '}
          <button type="button" className="button" onClick={() => onSwitchPage('login')}>
            Login
          </button>
        </span>
      </div>
    </div>
  );
}

export default Signup;