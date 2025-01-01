import React, { useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { useOne, useList, useUpdate } from "@refinedev/core";
import { DndContext, DragEndEvent } from "@dnd-kit/core";
import { Task } from '@/restful/types';

import { KanbanBoard, KanbanBoardContainer } from './components/board';
import { KanbanColumn } from './components/column';
import { KanbanItem } from './components/item';
import { TaskCard } from './components/card';

const TASK_STATUSES = ['TO_DO', 'IN_PROGRESS', 'DONE'] as const;

export const TaskBacklogPage: React.FC = () => {
  const { id } = useParams();

  const { data: projectData, isLoading: isProjectLoading } = useOne({
    resource: "projects",
    id: id as string,
  });

  const { data: tasksData, isLoading: isTasksLoading } = useList<Task>({
    resource: "tasks",
    meta: {
      projectId: id
    },
    queryOptions: {
      enabled: true,
    },
    pagination: {
      mode: "off"
    }
  });

  const { mutate: updateTask } = useUpdate({
    resource: "tasks",
    mutationMode: "optimistic",
    successNotification: false
  });

  // 按状态分组任务并生成列数据
  const { columns, tasks } = useMemo(() => {
    if (!tasksData?.data) {
      return { columns: [], tasks: [] };
    }

    const tasks = tasksData.data as Task[];
    
    // 创建状态分组
    const groupedTasks = TASK_STATUSES.reduce((acc, status) => {
      acc[status] = tasks.filter(task => task.status === status);
      return acc;
    }, {} as Record<string, Task[]>);

    // 生成列数据
    const columns = TASK_STATUSES.map(status => ({
      id: status,
      title: status,
      tasks: groupedTasks[status] || [],
    }));

    return { columns, tasks };
  }, [tasksData]);

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (!over) return;

    const taskId = active.id as string;
    const newStatus = over.id as typeof TASK_STATUSES[number];
    const task = active.data.current as Task;

    if (task.status === newStatus) return;

    updateTask({
      id: taskId,
      values: {
        status: newStatus,
      },
    });
  };

  if (isProjectLoading || isTasksLoading) {
    return <KanbanBoardSkeleton />;
  }

  return (
    <div style={{ padding: "24px" }}>
      <h1>{projectData?.data.name} - 看板</h1>
      <KanbanBoardContainer>
        <KanbanBoard onDragEnd={handleDragEnd}>
          {columns.map((column) => (
            <KanbanColumn
              key={column.id}
              id={column.id}
              title={column.title}
              count={column.tasks.length}
              data={column}
            >
              {column.tasks.map((task) => (
                <KanbanItem
                  key={task.id}
                  id={task.id.toString()}
                  data={{
                    ...task
                  }}
                >
                  <TaskCard {...task} />
                </KanbanItem>
              ))}
            </KanbanColumn>
          ))}
        </KanbanBoard>
      </KanbanBoardContainer>
    </div>
  );
};

const KanbanBoardSkeleton = () => {
  return (
    <KanbanBoardContainer>
      {TASK_STATUSES.map((status) => (
        <KanbanColumn
          key={status}
          id={status}
          title={status}
          count={0}
        >
          {[1, 2, 3].map((index) => (
            <TaskCard.Skeleton key={index} />
          ))}
        </KanbanColumn>
      ))}
    </KanbanBoardContainer>
  );
};

export default TaskBacklogPage;
