import React from 'react';
import { Card, Tag, Skeleton } from 'antd';
import { Task } from '@/restful/types';

export const TaskCard: React.FC<Task> & { Skeleton: React.FC } = (props) => {
  const { title, description, type, storyPoints, dueDate } = props;

  return (
    <Card size="small" style={{ width: '100%', marginBottom: '0.5rem' }}>
      <Card.Meta
        title={title}
        description={
          <div>
            <p>{description}</p>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
              <Tag color="blue">{type}</Tag>
              <Tag color="green">{storyPoints} 点</Tag>
              {dueDate && (
                <Tag color="orange">
                  截止: {new Date(dueDate).toLocaleDateString()}
                </Tag>
              )}
            </div>
          </div>
        }
      />
    </Card>
  );
};

TaskCard.Skeleton = () => (
  <Card size="small" style={{ width: '100%', marginBottom: '0.5rem' }}>
    <Skeleton active paragraph={{ rows: 2 }} />
  </Card>
); 