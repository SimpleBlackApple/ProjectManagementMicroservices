import React from 'react';
import { useParams } from 'react-router';
import { useList } from '@refinedev/core';
import { Table, Tag, Avatar } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { Task } from '@/restful/types';

export const TaskBacklogPage: React.FC = () => {
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

  const columns: ColumnsType<Task> = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={
          status === 'done' ? 'green' : 
          status === 'in_progress' ? 'blue' : 
          'default'
        }>
          {status.replace('_', ' ').toUpperCase()}
        </Tag>
      ),
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      render: (priority: string) => (
        <Tag color={
          priority === 'high' ? 'red' : 
          priority === 'medium' ? 'orange' : 
          'green'
        }>
          {priority.toUpperCase()}
        </Tag>
      ),
    },
    {
      title: 'Assignee',
      dataIndex: 'assignee',
      key: 'assignee',
      render: (assignee: Task['assignee']) => 
        assignee ? (
          <Avatar src={assignee.avatarUrl}>
            {assignee.name[0]}
          </Avatar>
        ) : null,
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Table 
        columns={columns} 
        dataSource={data?.data} 
        loading={isLoading}
        rowKey="id"
      />
    </div>
  );
}; 