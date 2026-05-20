const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

export const apiPostForm = async (url, data) => {
  const response = await fetch(`${API_BASE}${url}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    credentials: 'include',
    body: new URLSearchParams(data),
  });
  return response;
};

export const apiPostJson = async (url, data) => {
  const response = await fetch(`${API_BASE}${url}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data),
  });
  return response;
};