import React, { useState, useEffect } from 'react';
import './Permissions.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function Permissions({ onSwitchPage, onOpenModal }) {
  const [users, setUsers] = useState([]);
  const [selectedUserIds, setSelectedUserIds] = useState(new Set());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/all`, { credentials: 'include' });
      if (!res.ok) {
        throw new Error('Failed to fetch users. Ensure you have admin permissions.');
      }
      const data = await res.json();
      setUsers(data);
    } catch (err) {
      console.error(err);
      onOpenModal(err.message || 'Error loading users.');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectUser = (userId) => {
    setSelectedUserIds((prev) => {
      const next = new Set(prev);
      if (next.has(userId)) {
        next.delete(userId);
      } else {
        next.add(userId);
      }
      return next;
    });
  };

  const handleRoleToggle = (userId, roleName) => {
    setUsers((prevUsers) =>
      prevUsers.map((user) => {
        if (user.id !== userId) return user;

        const roles = [...user.roles];
        const roleIndex = roles.findIndex((r) => r.roleName === roleName);

        if (roleIndex > -1) {
          roles.splice(roleIndex, 1);
        } else {
          roles.push({ roleName });
        }

        return { ...user, roles };
      })
    );
  };

  const handleDeleteUser = async (userId, username) => {
    if (!window.confirm(`Are you sure you want to delete user "${username}"?`)) {
      return;
    }

    try {
      const res = await fetch(`${API_BASE}/removeUser/${userId}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!res.ok) {
        throw new Error('Failed to delete user.');
      }

      onOpenModal('User deleted successfully.');
      fetchUsers();
    } catch (err) {
      onOpenModal(err.message || 'Error deleting user.');
    }
  };

  const handleSubmit = async () => {
    if (selectedUserIds.size === 0) {
      onOpenModal('Please select at least one user to update.');
      return;
    }

    const updates = [];
    users.forEach((user) => {
      if (selectedUserIds.has(user.id)) {
        const hasRole = (role) => user.roles.some((r) => r.roleName === role);
        ['ROLE_ADMIN', 'ROLE_EDITOR', 'ROLE_VIEWER'].forEach((role) => {
          updates.push({
            userId: user.id,
            role: role,
            assigned: hasRole(role),
          });
        });
      }
    });

    try {
      const res = await fetch(`${API_BASE}/update-permissions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updates),
        credentials: 'include',
      });

      if (!res.ok) {
        throw new Error('Failed to update permissions.');
      }

      onOpenModal('Permissions updated successfully!');
      setSelectedUserIds(new Set());
      fetchUsers();
    } catch (err) {
      onOpenModal(err.message || 'Error updating permissions.');
    }
  };

  return (
    <div className="permissions-page font-sans">
      <header className="permissions-header">
        <h1 className="permissions-title">
          <i className="fas fa-key"></i> User Permissions
        </h1>
        <button onClick={() => onSwitchPage('home')} className="btn-back">
          <i className="fas fa-arrow-left"></i> Back
        </button>
      </header>

      <main className="permissions-container">
        <div className="permissions-card">
          <div className="table-wrapper">
            <table className="permissions-table">
              <thead>
                <tr>
                  <th style={{ width: '60px' }}>Select</th>
                  <th>Username</th>
                  <th>Admin</th>
                  <th>Editor</th>
                  <th>Viewer</th>
                  <th style={{ width: '120px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="6" className="text-center py-10 text-gray-500">
                      Loading users...
                    </td>
                  </tr>
                ) : users.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="text-center py-10 text-gray-500">
                      No users found
                    </td>
                  </tr>
                ) : (
                  users.map((user) => {
                    const hasRole = (role) => user.roles.some((r) => r.roleName === role);
                    return (
                      <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                        <td>
                          <input
                            type="checkbox"
                            className="checkbox-custom"
                            checked={selectedUserIds.has(user.id)}
                            onChange={() => handleSelectUser(user.id)}
                          />
                        </td>
                        <td className="font-semibold">{user.username}</td>
                        <td>
                          <input
                            type="checkbox"
                            checked={hasRole('ROLE_ADMIN')}
                            onChange={() => handleRoleToggle(user.id, 'ROLE_ADMIN')}
                          />
                        </td>
                        <td>
                          <input
                            type="checkbox"
                            checked={hasRole('ROLE_EDITOR')}
                            onChange={() => handleRoleToggle(user.id, 'ROLE_EDITOR')}
                          />
                        </td>
                        <td>
                          <input
                            type="checkbox"
                            checked={hasRole('ROLE_VIEWER')}
                            onChange={() => handleRoleToggle(user.id, 'ROLE_VIEWER')}
                          />
                        </td>
                        <td>
                          <button
                            onClick={() => handleDeleteUser(user.id, user.username)}
                            className="btn-delete"
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          <div className="submit-container">
            <button onClick={handleSubmit} className="btn-submit" disabled={loading}>
              Submit Changes
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default Permissions;
