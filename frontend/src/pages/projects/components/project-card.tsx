import { Card, Avatar, Space, Dropdown, Button, type MenuProps } from "antd";
import { UserOutlined, MoreOutlined, EyeOutlined, DeleteOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { useDelete } from "@refinedev/core";
import { useMemo } from "react";

interface ProjectCardProps {
  id: number;
  title: string;
  description?: string;
  members?: Array<{
    id: number;
    name: string;
  }>;
}

export const ProjectCard: React.FC<ProjectCardProps> = ({
  id,
  title,
  description,
  members,
}) => {
  const navigate = useNavigate(); 
  const { mutate } = useDelete();

  const dropdownItems = useMemo(() => {
    const items: MenuProps['items'] = [
      {
        label: 'View project',
        key: '1',
        icon: <EyeOutlined />,
        onClick: () => {
          navigate(`/projects/${id}/board`);
        },
      },
      {
        danger: true,
        label: 'Delete project',
        key: '2',
        icon: <DeleteOutlined />,
        onClick: () => {
          mutate({
            resource: 'projects',
            id,
            meta: {
              operation: 'project',
            },
          });
        },
      },
    ];
    return items;
  }, [id, navigate, mutate]);

  return (
    <Card
      title={
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          width: '100%' 
        }}>
          <span>{title}</span>
          <Dropdown 
            menu={{ items: dropdownItems }} 
            trigger={['click']}
            placement="bottomRight"
          >
            <Button
              type="text"
              icon={<MoreOutlined />}
              size="small"
              onClick={e => e.stopPropagation()}
            />
          </Dropdown>
        </div>
      }
      style={{ height: "100%", cursor: "pointer" }}
      onClick={() => navigate(`/projects/${id}`)}
    >
      {description && <p>{description}</p>}
      {members && members.length > 0 && (
        <Space>
          {members.map((member) => (
            <Avatar key={member.id} icon={<UserOutlined />} />
          ))}
        </Space>
      )}
    </Card>
  );
};