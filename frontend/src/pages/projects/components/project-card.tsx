import { Card, Avatar, Space } from "antd";
import { UserOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";

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

  return (
    <Card
      title={title}
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