import { Layout as AntdLayout, Menu } from "antd";
import { useLocation, Link } from "react-router";
import { Outlet } from "react-router";

export const MainLayout: React.FC = () => {
  const location = useLocation();
  const projectId = location.pathname.split('/')[2]; // 获取项目ID
  const showSidebar = location.pathname.includes("/projects/") && 
                      (location.pathname.includes("/board") || 
                       location.pathname.includes("/backlog"));

  return (
    <AntdLayout style={{ minHeight: "100vh" }}>
      <AntdLayout.Header style={{ background: "#fff", padding: "0 24px" }}>
        {/* 你可以在这里添加header内容 */}
      </AntdLayout.Header>
      <AntdLayout>
        {showSidebar && (
          <AntdLayout.Sider theme="light" width={200}>
            <Menu
              mode="inline"
              selectedKeys={[location.pathname]}
              style={{ height: '100%', borderRight: 0 }}
            >
              <Menu.Item key={`/projects/${projectId}/board`}>
                <Link to={`/projects/${projectId}/board`}>Board</Link>
              </Menu.Item>
              <Menu.Item key={`/projects/${projectId}/backlog`}>
                <Link to={`/projects/${projectId}/backlog`}>Backlog</Link>
              </Menu.Item>
            </Menu>
          </AntdLayout.Sider>
        )}
        <AntdLayout.Content style={{ padding: 24 }}>
          <Outlet />
        </AntdLayout.Content>
      </AntdLayout>
    </AntdLayout>
  );
}; 