import type { IResourceItem } from "@refinedev/core";

import {
  DashboardOutlined,
  ProjectOutlined,
  AppstoreOutlined,
  ClockCircleOutlined
} from "@ant-design/icons";

export const resources: IResourceItem[] = [
  {
    name: "projects",
    list: "/projects",
    show: "/projects/:id",
    meta: {
      icon: <ProjectOutlined />
    }
  },
  {
    name: "board",
    list: "/projects/:id/board",
    meta: {
      parent: "projects",
      icon: <DashboardOutlined />
    }
  },
  {
    name: "timeline",
    list: "/projects/:id/timeline", // 定义 timeline 的路由
    meta: {
      parent: "projects", // 指定为 projects 的子模块
      icon: <ClockCircleOutlined /> // 使用一个时间相关的图标
    }
  },
  {
    name: "backlog",
    list: "/projects/:id/backlog",
    meta: {
      parent: "projects",
      icon: <AppstoreOutlined />
    }
  }
];
