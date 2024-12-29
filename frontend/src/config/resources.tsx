import type { IResourceItem } from "@refinedev/core";

import {
  DashboardOutlined,
  ProjectOutlined,
  AppstoreOutlined
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
    name: "backlog",
    list: "/projects/:id/backlog",
    meta: {
      parent: "projects",
      icon: <AppstoreOutlined />
    }
  }
];
