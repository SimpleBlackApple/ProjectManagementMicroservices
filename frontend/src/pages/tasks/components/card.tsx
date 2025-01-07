import React, {memo, useMemo} from 'react';
import { Card, Tag, Skeleton, Button, Dropdown, Typography, Space, Tooltip, type MenuProps } from 'antd';
import { Task } from '@/restful/types';
import { EyeOutlined, DeleteOutlined, MoreOutlined, MenuOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useDelete } from '@refinedev/core';
import { CustomAvatar } from '@/components';
import dayjs from 'dayjs';
import { useNavigate, useParams } from 'react-router-dom';

const { Text } = Typography;
interface TaskCardProps extends Task {
  id: string;
  managerId: number | string;
}

const BaseTaskCard: React.FC<TaskCardProps> = (props) => {
  const { id:projectId } = useParams();
  const { id, title, description, type, storyPoints, dueDate, managerId } = props;
  const  edit  = useNavigate();
  const { mutate } = useDelete();

  // 将 managerId 转换为符合 users 格式的数组
  const users = managerId ? [{
    id: managerId.toString(),
    name: `User ${managerId}`,  // 你可以根据需要修改用户名的显示方式
    avatarUrl: undefined
  }] : [];

  const formatDate = (dateString: string) => {
    return dayjs(dateString).format('MMM DD');
  };

  const dropdownItems = useMemo(() => {
    const items: MenuProps['items'] = [
      {
        label: 'View card',
        key: '1',
        icon: <EyeOutlined />,
        onClick: () => {
          edit(`/projects/${projectId}/backlog/edit/${id}`);
        },
      },
      {
        danger: true,
        label: 'Delete card',
        key: '2',
        icon: <DeleteOutlined />,
        onClick: () => {
          mutate({
            resource: 'tasks',
            id,
            meta: {
              operation: 'task',
            },
          });
        },
      },
    ];
    return items;
  }, [id, edit, mutate]);

  return (
    <Card
      size="small"
      style={{ width: '100%', marginBottom: '0.5rem' }}
      onClick={() => edit(`/projects/${projectId}/backlog/edit/${id}`)}
      title={
        <Text
          ellipsis={{ tooltip: title }}
          style={{
            textAlign: 'left',
            fontSize: '16px',
            lineHeight: '32px',
            display: 'inline-block',
            margin: 0,
            verticalAlign: 'middle'
          }}
        >
          {title}
        </Text>
      }
      extra={
        <Dropdown
          trigger={['click']}
          menu={{
            items: dropdownItems,
            onPointerDown: (e) => {
              e.stopPropagation();
            },
            onClick: (e) => {
              e.domEvent.stopPropagation();
            },
          }}
          placement="bottom"
          arrow={{ pointAtCenter: true }}
        >
          <Button
            type="text"
            shape="circle"
            icon={
              <MoreOutlined
                style={{
                  transform: 'rotate(90deg)',
                }}
              />
            }
            onPointerDown={(e) => {
              e.stopPropagation();
            }}
            onClick={(e) => {
              e.stopPropagation();
            }}
          />
        </Dropdown>
      }
    >
      <div>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}>
          {/* 左侧区域：展开按钮、日期和标签 */}
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <Button
              type="text"
              icon={<MenuOutlined style={{ fontSize: '10px' }} />}
              style={{
                padding: 0,
                width: '10px',
                height: '1px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
              onClick={(e) => {
                e.stopPropagation();
              }}
            />
            {dueDate && (
              <Tag
                icon={<ClockCircleOutlined style={{ fontSize: '12px' }} />}
                color="default"
                style={{
                  padding: '0 8px',
                  borderRadius: '16px',
                  backgroundColor: 'rgb(255, 239, 239)',
                  color: 'rgb(255, 77, 79)',
                  border: 'none'
                }}
              >
                {formatDate(dueDate)}
              </Tag>
            )}
            {/*<Tag color="blue">{type}</Tag>*/}
            <Tag color="green">{storyPoints} pts</Tag>
          </div>

          {/* 右侧头像 */}
          {!!users.length && (
            <Space
              size={4}
              direction="horizontal"
              align="center"
            >
              {users.map((user) => (
                <Tooltip key={user.id} title={user.name}>
                  <CustomAvatar
                    name={user.name}
                    src={user.avatarUrl}
                    size="small"
                  />
                </Tooltip>
              ))}
            </Space>
          )}
        </div>
      </div>
    </Card>
  );
};

const TaskCardSkeleton: React.FC = () => (
  <Card size="small" style={{ width: '100%', marginBottom: '0.5rem' }}>
    <Skeleton active paragraph={{ rows: 2 }} />
  </Card>
);

export const TaskCard = Object.assign(BaseTaskCard, { Skeleton: TaskCardSkeleton });

export const TaskCardMemo = memo(BaseTaskCard, (prev, next) => {
  return (
    prev.id === next.id &&
    prev.title === next.title &&
    prev.dueDate === next.dueDate &&
    prev.managerId === next.managerId &&
    prev.storyPoints === next.storyPoints &&
    prev.status === next.status
  );
});

