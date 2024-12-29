import React from 'react';
import { useDroppable, type UseDroppableArguments } from '@dnd-kit/core';
import { Badge } from 'antd';

type Props = {
  id: string;
  title: string;
  count: number;
  data?: UseDroppableArguments["data"];
};

export const KanbanColumn: React.FC<React.PropsWithChildren<Props>> = ({
  children,
  id,
  title,
  count,
  data,
}) => {
  const { isOver, setNodeRef, active } = useDroppable({
    id,
    data,
  });

  return (
    <div
      ref={setNodeRef}
      style={{
        flex: 1,
        padding: '1rem',
        backgroundColor: isOver ? '#f0f0f0' : '#fff',
        borderRadius: '6px',
        border: '1px solid #f0f0f0',
        minWidth: '280px',
      }}
    >
      <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <h3 style={{ margin: 0 }}>{title}</h3>
        <Badge count={count} style={{ backgroundColor: '#52c41a' }} />
      </div>
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column', 
        gap: '0.5rem',
        overflowY: active ? "unset" : "auto",
      }}>
        {children}
      </div>
    </div>
  );
}; 