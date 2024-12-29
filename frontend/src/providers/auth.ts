import type { AuthProvider } from "@refinedev/core";
import { API_URLS } from "./data";

export const authProvider: AuthProvider = {
  login: async ({ email, password }) => {
    try {
      const response = await fetch(`${API_URLS.users}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (response.ok) {
        // 存储token和过期时间
        localStorage.setItem("token", data.token);
        localStorage.setItem("tokenExpires", data.expiresIn);
        
        // 获取用户信息
        const userResponse = await fetch(`${API_URLS.users}/api/users/me`, {
          headers: {
            'Authorization': `Bearer ${data.token}`
          }
        });
        const userData = await userResponse.json();
        localStorage.setItem("user", JSON.stringify(userData));

        return {
          success: true,
          redirectTo: "/projects",
        };
      }

      return {
        success: false,
        error: {
          message: data.message,
          name: "Invalid credentials",
        },
      };
    } catch (error) {
      return {
        success: false,
        error: {
          message: "Login failed",
          name: "Invalid credentials",
        },
      };
    }
  },

  logout: async () => {
    localStorage.removeItem("token");
    localStorage.removeItem("tokenExpires");
    localStorage.removeItem("user");
    return {
      success: true,
      redirectTo: "/login",
    };
  },

  check: async () => {
    const token = localStorage.getItem("token");
    if (token) {
      return {
        authenticated: true,
      };
    }

    return {
      authenticated: false,
      redirectTo: "/login",
    };
  },

  getIdentity: async () => {
    const user = localStorage.getItem("user");
    if (user) {
      return JSON.parse(user);
    }
    return null;
  },

  onError: async (error) => {
    if (error.status === 401 || error.status === 403) {
      return {
        logout: true,
        redirectTo: "/login",
      };
    }

    return { error };
  },
}; 