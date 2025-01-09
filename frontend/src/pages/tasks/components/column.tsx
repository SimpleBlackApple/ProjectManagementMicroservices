import React from 'react';
import { useDroppable, type UseDroppableArguments } from '@dnd-kit/core';
import { Badge, Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';

type Props = {
  id: string;
  title: React.ReactNode;
  count: number;
  data?: UseDroppableArguments["data"];
  onAddClick?: (args: { id: string }) => void;
  style?: React.CSSProperties;
  showAddButton?: boolean; // 新增控制按钮显示的参数
};

export const KanbanColumn: React.FC<React.PropsWithChildren<Props>> = ({
  children,
  id,
  title,
  count,
  data,
  onAddClick,
  showAddButton = true, // 默认为 true，保持向后兼容
}) => {
  const { isOver, setNodeRef, active } = useDroppable({
    id,
    data,
  });

  const onAddClickHandler = () => {
    onAddClick?.({ id });
  };

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
        {showAddButton && (
          <Button
            shape="circle"
            icon={<PlusOutlined />}
            onClick={onAddClickHandler}
          />
        )}
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