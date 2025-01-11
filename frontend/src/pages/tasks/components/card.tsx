import React, { memo, useMemo } from 'react';
import { Card, Tag, Skeleton, Button, Dropdown, Typography, Space, Tooltip, type MenuProps, Checkbox } from 'antd';
import { Task } from '@/restful/types';
import { EyeOutlined, DeleteOutlined, MoreOutlined, MenuOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { useDelete, useUpdate } from '@refinedev/core';
import { ProjectMembers } from './member/index';
import dayjs from 'dayjs';
import { useNavigate, useParams } from 'react-router-dom';

const { Text } = Typography;
interface TaskCardProps extends Task {
  id: number | undefined;
  managerId: number;
  sprintId?: number | null;
}

const BaseTaskCard: React.FC<TaskCardProps> = (props) => {
  const { id: projectId } = useParams();
  const { id, title, description, type, storyPoints, dueDate, managerId, status, sprintId } = props;
  const edit = useNavigate();
  const { mutate: deleteMutate } = useDelete();
  const { mutate: updateMutate } = useUpdate();

  // Convert managerId to users array format
  const users = managerId ? [{
    id: managerId.toString(),
    name: `User ${managerId}`,
    avatarUrl: undefined
  }] : [];

  const formatDate = (dateString: string) => {
    return dayjs(dateString).format('MMM DD');
  };

  const handleStatusChange = (e: React.MouseEvent) => {
    e.stopPropagation();
    const newStatus = status === 'DONE' ? 'TO_DO' : 'DONE';

    updateMutate({
      resource: 'tasks',
      id,
      values: {
        status: newStatus,
        sprintId: sprintId
      },
      meta: {
        operation: 'task',
      },
    });
    window.location.reload();
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
          deleteMutate({
            resource: 'tasks',
            id: id as string | number,
            meta: {
              operation: 'task',
            },
          });
        },
      },
    ];
    return items;
  }, [id, edit, deleteMutate]);

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
          {/* Left section: expand button, date and tags */}
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
            <Tag color="green">{storyPoints} pts</Tag>
          </div>

          {/* Right section: checkbox and avatar */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Checkbox
              checked={status === 'DONE'}
              onClick={handleStatusChange}

              style={{ marginRight: '8px' }}
            />
            {managerId && (
              <ProjectMembers
                projectId={projectId as string}
                displayManagement={false}
                selectedUserId={managerId} 
                render={(members) => {
                  const assignedMember = members.find(member => member.id === managerId);
                  if (assignedMember) {
                    return (
                      <Tooltip title={assignedMember.name}>
                        <div style={{
                          width: '24px',
                          height: '24px',
                          borderRadius: '50%',
                          backgroundColor: '#f0f0f0',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: '12px',
                          color: '#666'
                        }}>
                          {assignedMember.name.charAt(0).toUpperCase()}
                        </div>
                      </Tooltip>
                    );
                  }
                  return null;
                }}
              />
            )}
          </div>
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
    prev.status === next.status &&
    prev.sprintId === next.sprintId
  );
});