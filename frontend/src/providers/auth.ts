import type { AuthProvider } from "@refinedev/core";
import { API_URLS } from "./data";

export const authProvider: AuthProvider = {
  login: async ({ email, password }) => {
    try {
      const response = await fetch(`/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
        credentials: 'include'
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem("token", data.token);
        
        if (data.expiresIn) {
          const expirationTime = Date.now() + data.expiresIn * 1000;
          localStorage.setItem("tokenExpires", expirationTime.toString());
        }

        const userResponse = await fetch(`/api/users/me`, {
          headers: {
            'Authorization': `Bearer ${data.token}`
          }
        });
        
        if (userResponse.ok) {
          const userData = await userResponse.json();
          localStorage.setItem("user", JSON.stringify(userData));
          
          return {
            success: true,
            redirectTo: "/projects",
          };
        }
      }

      return {
        success: false,
        error: {
          message: data.message || "Invalid credentials",
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
    const tokenExpires = localStorage.getItem("tokenExpires");

    if (!token) {
      return {
        authenticated: false,
        error: {
          message: "Please login to continue",
          name: "not authenticated",
        },
        redirectTo: "/login",
      };
    }

    // Check token expiration
    if (tokenExpires && Number(tokenExpires) < Date.now()) {
      localStorage.removeItem("token");
      localStorage.removeItem("tokenExpires");
      localStorage.removeItem("user");
      
      return {
        authenticated: false,
        error: {
          message: "Session expired, please login again",
          name: "session expired",
        },
        redirectTo: "/login",
      };
    }

    return {
      authenticated: true,
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
    const status = error?.status;
    
    if (status === 401 || status === 403) {
      localStorage.removeItem("token");
      localStorage.removeItem("tokenExpires");
      localStorage.removeItem("user");
      
      return {
        error: {
          message: "Session expired, please login again",
          name: "session expired",
        },
        logout: true,
        redirectTo: "/login",
      };
    }

    return { error };
  },
};

export default authProvider;