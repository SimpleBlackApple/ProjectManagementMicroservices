import React from 'react';
import { useParams } from 'react-router';
import { useList } from '@refinedev/core';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import { Card, Space, Tag, Avatar } from 'antd';
import { Task } from '@/restful/types';

const COLUMNS = [
  { id: 'unassigned', title: 'Unassigned' },
  { id: 'todo', title: 'To Do' },
  { id: 'in_progress', title: 'In Progress' },
  { id: 'done', title: 'Done' }
];

export const TaskBoardPage: React.FC = () => {
  const { id: projectId } = useParams();

  const { data, isLoading } = useList<Task>({
    resource: "tasks",
    filters: [
      {
        field: "projectId",
        operator: "eq",
        value: projectId
      }
    ]
  });

  if (isLoading) return <div>Loading...</div>;

  const tasks = data?.data || [];

  const getTasksByStatus = (status: string) => {
    return tasks.filter(task => task.status === status);
  };

  return (
    <div style={{ padding: '24px' }}>
      <div style={{ display: 'flex', gap: '16px' }}>
        {COLUMNS.map(column => (
          <div
            key={column.id}
            style={{
              background: '#f5f5f5',
              padding: '16px',
              borderRadius: '8px',
              width: '300px'
            }}
          >
            <h3>{column.title}</h3>
            <Space direction="vertical" style={{ width: '100%' }}>
              {getTasksByStatus(column.id).map(task => (
                <Card
                  key={task.id}
                  size="small"
                  title={task.title}
                  style={{ width: '100%' }}
                >
                  <p>{task.description}</p>
                  <Space>
                    {task.priority && (
                      <Tag color={
                        task.priority === 'high' ? 'red' :
                        task.priority === 'medium' ? 'orange' :
                        'green'
                      }>
                        {task.priority}
                      </Tag>
                    )}
                    {task.assignee && (
                      <Avatar
                        src={task.assignee.avatarUrl}
                        size="small"
                      >
                        {task.assignee.name[0]}
                      </Avatar>
                    )}
                  </Space>
                </Card>
              ))}
            </Space>
          </div>
        ))}
      </div>
    </div>
  );
};
