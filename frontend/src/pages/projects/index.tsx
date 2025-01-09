import { useList, useDelete } from "@refinedev/core";
import { Card, Row, Col, ConfigProvider, Space, theme, Button, Dropdown, type MenuProps } from "antd";
import { CustomAvatar, Text, TextIcon } from "@/components";
import { Project } from "@/restful/types";
import { Outlet, useNavigate } from "react-router-dom";
import { PlusOutlined, MoreOutlined, EyeOutlined, DeleteOutlined, EditOutlined } from "@ant-design/icons";
import { useMemo } from "react";
import './index.less';

export const ProjectListPage: React.FC = () => {
  const { data, isLoading } = useList<Project>({
    resource: "projects",
  });
  const { token } = theme.useToken();
  const navigate = useNavigate();
  const { mutate } = useDelete();

  const handleAddProject = () => {
    navigate("/projects/new");
  };

  const handleViewProject = (projectId: number) => {
    navigate(`/projects/${projectId}/board`);
  };

  const handleEditProject = (projectId: number) => {
    navigate(`/projects/${projectId}/edit`);
  };

  const handleDeleteProject = (projectId: number) => {
    mutate({
      resource: "projects",
      id: projectId,
      meta: {
        operation: "project",
      },
    });
  };

  const getDropdownItems = (projectId: number) => {
    const items: MenuProps['items'] = [
      {
        label: 'View project',
        key: '1',
        icon: <EyeOutlined />,
        onClick: () => handleViewProject(projectId),
      },
      {
        label: 'Edit project',
        key: '2',
        icon: <EditOutlined />,
        onClick: () => handleEditProject(projectId),
      },
      {
        danger: true,
        label: 'Delete project',
        key: '3',
        icon: <DeleteOutlined />,
        onClick: () => handleDeleteProject(projectId),
      },
    ];
    return items;
  };

  if (isLoading) return <div>Loading...</div>;

  return (
    <div className="project-list-container">
      <div className="header">
        <h1>Projects</h1>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleAddProject}
        >
          Add Project
        </Button>
      </div>

      <Row gutter={[16, 16]}>
        {data?.data.map((project) => (
          <Col key={project.id} xs={24} sm={12} md={8} lg={6}>
            <ConfigProvider
              theme={{
                components: {
                  Card: {
                    headerBg: "transparent",
                  },
                },
              }}
            >
              <Card
                size="small"
                className="project-card"
                title={
                  <div className="card-title">
                    <Text ellipsis={{ tooltip: project.name }}>
                      {project.name}
                    </Text>
                    <Dropdown
                      menu={{ 
                        items: getDropdownItems(project.id),
                        onClick: (e) => {
                          e.domEvent.stopPropagation();
                        },
                      }}
                      trigger={['click']}
                      placement="bottomRight"
                    >
                      <Button
                        type="text"
                        size="small"
                        icon={<MoreOutlined />}
                        onClick={(e) => {
                          e.stopPropagation();
                        }}
                        className="more-button"
                      />
                    </Dropdown>
                  </div>
                }
                onClick={() => handleViewProject(project.id)}
                styles={{
                  header: { background: 'transparent' },
                  body: { padding: '12px' }
                }}
              >
                <div className="card-content">
                  <TextIcon className="icon" />
                  <Text className="description">
                    {project.description}
                  </Text>

                  {project.members && project.members.length > 0 && (
                    <Space
                      size={4}
                      wrap
                      direction="horizontal"
                      align="center"
                      className="members"
                    >
                      {project.members.map((member) => (
                        <CustomAvatar
                          key={member.id}
                          name={member.name}
                          src={member.avatarUrl}
                        />
                      ))}
                    </Space>
                  )}
                </div>
              </Card>
            </ConfigProvider>
          </Col>
        ))}
        <Outlet />
      </Row>
    </div>
  );
};