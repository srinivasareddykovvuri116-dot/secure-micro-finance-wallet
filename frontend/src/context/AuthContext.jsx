import { useState } from 'react';
import { jwtDecode } from 'jwt-decode';
import { AuthContext } from './AuthContextDefinition';

function getRoleFromToken(token) {
  if (!token) {
    return null;
  }

  try {
    const decodedToken = jwtDecode(token);
    return decodedToken.role || null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => {
    return localStorage.getItem('token');
  });

  const [role, setRole] = useState(() => {
    const savedToken = localStorage.getItem('token');
    return getRoleFromToken(savedToken);
  });

  const login = (jwtToken) => {
    localStorage.setItem('token', jwtToken);
    setToken(jwtToken);
    setRole(getRoleFromToken(jwtToken));
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setRole(null);
  };

  const isAuthenticated = Boolean(token);
  const isAdmin = role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider
      value={{
        token,
        role,
        isAuthenticated,
        isAdmin,
        login,
        logout
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}