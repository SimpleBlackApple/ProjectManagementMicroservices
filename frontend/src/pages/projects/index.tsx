import { useList } from "@refinedev/core";
import { Card, Row, Col, ConfigProvider, Space, theme } from "antd";
import { CustomAvatar, Text, TextIcon } from "@/components";
import { Project } from "@/restful/types";
import { useNavigate } from "react-router-dom";

export const ProjectListPage: React.FC = () => {
  const { data, isLoading } = useList<Project>({
    resource: "projects",
  });
  const { token } = theme.useToken();
  const navigate = useNavigate();

  if (isLoading) return <div>Loading...</div>;

  return (
    <div style={{ padding: 24 }}>
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
                title={
                  <Text ellipsis={{ tooltip: project.name }}>
                    {project.name}
                  </Text>
                }
                onClick={() => navigate(`/projects/${project.id}/board`)}
                style={{ cursor: 'pointer' }}
                styles={{
                  header: { background: 'transparent' },
                  body: { padding: '12px' }
                }}
              >
                <div
                  style={{
                    display: "flex",
                    flexWrap: "wrap",
                    alignItems: "center",
                    gap: "8px",
                  }}
                >
                  <TextIcon
                    style={{
                      marginRight: "4px",
                    }}
                  />
                  <Text
                    style={{
                      color: token.colorTextSecondary,
                      fontSize: "12px",
                    }}
                  >
                    {project.description}
                  </Text>
                  
                  {project.members && project.members.length > 0 && (
                    <Space
                      size={4}
                      wrap
                      direction="horizontal"
                      align="center"
                      style={{
                        display: "flex",
                        justifyContent: "flex-end",
                        marginLeft: "auto",
                        marginRight: "0",
                      }}
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
      </Row>
    </div>
  );
}; 