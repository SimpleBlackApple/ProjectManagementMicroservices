import { DataProvider } from "@refinedev/core";
import axios from "axios";

export const API_URLS = {
  users: "http://localhost:8081",
  projects: "http://localhost:8082",
  tasks: "http://localhost:8083"
};

// 创建axios实例
const axiosInstance = axios.create();

// 添加请求拦截器
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 根据resource选择对应的API地址和路径
const getApiConfig = (resource: string, meta?: any) => {
  // 处理项目任务的特殊情况
  if (resource.startsWith('projects/') && resource.endsWith('/tasks')) {
    const projectId = resource.split('/')[1];
    return {
      baseUrl: API_URLS.tasks,
      path: 'tasks',
      projectId // 保存projectId用于构建URL
    };
  }

  switch (resource) {
    case "users":
      return {
        baseUrl: API_URLS.users,
        path: "users"
      };
    case "projects":
      return {
        baseUrl: API_URLS.projects,
        path: "projects"
      };
    case "tasks":
      return {
        baseUrl: API_URLS.tasks,
        path: "tasks",
        projectId: meta?.projectId // 支持传入projectId
      };
    default:
      return {
        baseUrl: API_URLS.projects,
        path: resource
      };
  }
};

export const dataProvider: DataProvider = {
  getList: async ({ resource, meta }) => {
    const { baseUrl, path, projectId } = getApiConfig(resource, meta);
    // 如果有projectId，使用projects/:id/tasks路径
    const url = projectId 
      ? `${baseUrl}/api/projects/${projectId}/tasks`
      : `${baseUrl}/api/${path}`;

    try {
      const { data } = await axiosInstance.get(url);
      return {
        data,
        total: data.length,
      };
    } catch (error) {
      console.error(`Failed to fetch ${resource} list:`, error);
      throw error;
    }
  },

  getOne: async ({ resource, id }) => {
    const { baseUrl, path } = getApiConfig(resource);
    // 特殊处理users资源
    const url = resource === "users" 
      ? `${baseUrl}/api/${path}/me`
      : `${baseUrl}/api/${path}/${id}`;
    
    console.log("URL:", url);
    try {
      const { data } = await axiosInstance.get(url);
      return {
        data,
      };
    } catch (error) {
      console.error(`Failed to fetch ${resource} with id ${id}:`, error);
      throw error;
    }
  },

  create: async ({ resource, variables }) => {
    const { baseUrl, path } = getApiConfig(resource);
    const url = `${baseUrl}/api/${path}`;
    try {
      const { data } = await axiosInstance.post(url, variables);
      return {
        data,
      };
    } catch (error) {
      console.error(`Failed to create ${resource}:`, error);
      throw error;
    }
  },

  update: async ({ resource, id, variables, meta }) => {
    const { baseUrl, path } = getApiConfig(resource, meta);
    const url = resource === "users"
      ? `${baseUrl}/api/${path}`
      : `${baseUrl}/api/${path}/${id}`;
    
    try {
      const { data } = await axiosInstance.put(url, variables);
      return {
        data: {
          ...meta?.previousData,
          ...data,
        },
      };
    } catch (error) {
      console.error(`Failed to update ${resource} with id ${id}:`, error);
      throw error;
    }
  },

  deleteOne: async ({ resource, id }) => {
    const { baseUrl, path } = getApiConfig(resource);
    // 特殊处理users资源
    const url = resource === "users"
      ? `${baseUrl}/api/${path}`  // 删除用户不需要id，因为从token获取
      : `${baseUrl}/api/${path}/${id}`;
    try {
      const { data } = await axiosInstance.delete(url);
      return {
        data,
      };
    } catch (error) {
      console.error(`Failed to delete ${resource} with id ${id}:`, error);
      throw error;
    }
  },

  getApiUrl: () => {
    return API_URLS.users;
  },
};
