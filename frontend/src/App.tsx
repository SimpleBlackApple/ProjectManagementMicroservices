import { BrowserRouter, Outlet, Route, Routes, Navigate } from "react-router-dom";
import { Refine } from "@refinedev/core";
import { useNotificationProvider } from "@refinedev/antd";
import { MainLayout } from "@/components/layouts/main-layout";
import { Authenticated, ErrorComponent } from "@refinedev/core";
import { ConfigProvider, List } from "antd";
import "@refinedev/antd/dist/reset.css";

import { resources } from "@/config/resources";
import { authProvider, dataProvider } from "@/providers";
import { LoginPage } from "./pages/login";
import { RegisterPage } from "./pages/register";
import { ProjectListPage } from "./pages/projects";
import { TaskBoardPage } from "./pages/tasks/board/index";
import { TaskBacklogPage } from "./pages/tasks/backlog";
import { ProjectLayout, TaskLayout } from "@/components/layout";
import { ProjectsCreatePage } from "./pages/projects/create";
import { ProjectsEditPage } from "./pages/projects/edit";
import { CatchAllNavigate } from "@refinedev/react-router";
import { TasksCreatePage } from "@/pages/tasks/create";
import { TasksEditPage } from "@/pages/tasks/edit";
import GanttChart from "./pages/tasks/components/timeline/timeline";


function App() {
  return (
    <BrowserRouter>
      <ConfigProvider>
        <Refine
          authProvider={authProvider}
          dataProvider={dataProvider}
          notificationProvider={useNotificationProvider}
          resources={resources}
          options={{
            syncWithLocation: true,
            warnWhenUnsavedChanges: true,
          }}
        >
          <Routes>
            <Route
              element={
                <Authenticated
                  key="authenticated-layout"
                  fallback={<CatchAllNavigate to="/login" />}
                >
                  <ProjectLayout>
                    <Outlet />
                  </ProjectLayout>
                </Authenticated>
              }
            >

              <Route index element={<Navigate to="/projects" replace />} />

              <Route path="projects" element={<ProjectListPage />}>
                <Route index element={null} />
                <Route path="new" element={<ProjectsCreatePage />} />
                <Route path=":id/edit" element={<ProjectsEditPage />} />
              </Route>
            </Route>

            <Route path="/projects/:id"
              element={
                <Authenticated key="authenticated-task" fallback={<CatchAllNavigate to="/login" />}>
                  <TaskLayout>
                    <Outlet />
                  </TaskLayout>
                </Authenticated>
              }
            >
              <Route index element={<TaskBoardPage />} />
              <Route path="board">
                <Route index element={<TaskBoardPage />} />
              </Route>
              <Route path="timeline" element={<GanttChart />} />
              <Route path="backlog">
                <Route index element={<TaskBacklogPage />} />
                <Route path="new" element={
                  <TaskBacklogPage>
                    <TasksCreatePage />
                  </TaskBacklogPage>
                } />
                <Route path="edit/:taskId" element={
                  <TaskBacklogPage>
                    <TasksEditPage />
                  </TaskBacklogPage>
                } />
              </Route>
            </Route>



            <Route
              element={
                <Authenticated key="authenticated-auth" fallback={<Outlet />}>
                  <Navigate to="/projects" replace />
                </Authenticated>
              }
            >
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Route>
          </Routes>
        </Refine>
      </ConfigProvider>
    </BrowserRouter>
  );
}

export default App;
