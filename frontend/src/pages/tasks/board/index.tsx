import React, { useState, useEffect, Children } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useOne } from "@refinedev/core";
import { DndContext, DragEndEvent, closestCenter } from "@dnd-kit/core";
import { Spin, Alert, Button, Space, Modal } from 'antd';
import { DeleteOutlined, EditOutlined, PlayCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { DeleteSprintButton } from "./deleteSpringButton";
import { KanbanColumn } from '../components/column';
import { KanbanItem } from '../components/item';
import { TaskCardMemo } from '../components/card';
import { Sprint, Task, Project } from '@/restful/types';
import { SprintEditModal } from './edit'
import { SprintCreateModal } from './create'
import axios from 'axios';
import './index.less';
import { log } from 'console';
import dayjs from 'dayjs';

interface TaskBoardPageProps {
  children?: React.ReactNode;
}

export const TaskBoardPage: React.FC<TaskBoardPageProps> = ({ children }) => {
  const { id: projectId } = useParams();
  const navigate = useNavigate();
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [sprintTasks, setSprintTasks] = useState<Record<number, Task[]>>({});
  const [backlogTasks, setBacklogTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [editingSprint, setEditingSprint] = useState<Sprint | null>(null);
  const [isCreateModalVisible, setIsCreateModalVisible] = useState(false);
  const { data: projectData, isLoading: isProjectLoading, isError } = useOne<Project>({
    resource: "projects",
    id: projectId as string,
  });

  const fetchData = async () => {
    try {
      setLoading(true);

      // 获取所有 sprints
      const sprintsResponse = await axios.get(
        `http://localhost:8083/api/projects/${projectId}/sprints`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        }
      );
      const fetchedSprints = sprintsResponse.data;
      setSprints(fetchedSprints);

      // 获取所有任务
      const allTasksResponse = await axios.get(
        `http://localhost:8083/api/projects/${projectId}/tasks`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        }
      );
      const allTasks = allTasksResponse.data;
      console.log(allTasks)
      // 分离 backlog 任务和 sprint 任务
      const backlog = allTasks.filter((task: Task) =>
        task.sprintId === null && task.projectId === Number(projectId)
      );
      setBacklogTasks(backlog);

      // 按 sprint 分组任务
      const tasksBySprint = allTasks.reduce((acc: Record<number, Task[]>, task: Task) => {
        if (task.sprintId) {
          if (!acc[task.sprintId]) {
            acc[task.sprintId] = [];
          }
          acc[task.sprintId].push(task);
        }
        return acc;
      }, {});
      setSprintTasks(tasksBySprint);

    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [projectId]);

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const task = active.data.current as Task;
    const taskId = parseInt(active.id as string);
    const targetId = over.id as string;

    try {
      // let newSprintId: number | null = null;

      // // 确定目标 sprintId
      // if (targetId.startsWith('sprint-')) {
      //   newSprintId = parseInt(targetId.replace('sprint-', ''));
      // } else if (targetId === 'backlog') {
      //   newSprintId = null;
      // }
      const newSprintId = targetId.startsWith('sprint-')
        ? parseInt(targetId.replace('sprint-', ''))
        : null;

      console.log('Moving task:', {
        taskId,
        targetId,
        newSprintId
      });

      if (task.sprintId === newSprintId) {
        return;
      }

      console.log('Updating task:', {
        taskId,
        currentSprintId: task.sprintId,
        newSprintId,
        targetId
      });
      // 更新任务的 sprintId
      await axios.put(
        `http://localhost:8083/api/tasks/${taskId}`,
        { sprintId: newSprintId },
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        }
      );

      // 重新获取数据
      await fetchData();
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        Modal.error({
          title: 'failed',
          content: error.response.data
        });
        console.error('Server error response:', error.response.data);
      }
      console.error('Error updating task:', error);
    }
  };

  const handleStartSprint = async (sprintId: number) => {
    try {
      await axios.put(

        `http://localhost:8083/api/projects/${projectId}/sprints/${sprintId}`,
        {
          status: 'IN_PROGRESS',
          startDate: new Date().toISOString()
        },
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        }
      );
      await fetchData();
    } catch (error) {
      console.error('Error starting sprint:', error);
    }
  };

  if (loading || isProjectLoading) {
    return <Spin spinning={true} />;
  }

  if (isError) {
    return <Alert type="error" message="Error loading project data" />;
  }

  return (
    <div>
      <DndContext onDragEnd={handleDragEnd} collisionDetection={closestCenter}>
        <div className="taskBoard">
          <Space direction="vertical" size="large" className="taskBoard-container">
            <div className="taskBoard-header">
              <h1 className="taskBoard-header-title">
                {projectData?.data?.name} - Sprint Board
              </h1>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  console.log('Navigating to create sprint page...');
                  setIsCreateModalVisible(true)
                }}
                size="small"
              >
                Create Sprint
              </Button>
            </div>

            {[...sprints]
              .sort((a, b) => a.id - b.id) // 按照 sprintId 升序排序
              .map((sprint: Sprint, index: number) => (
                <div key={`sprint-wrapper-${sprint.id}`} className="taskBoard-sprint-wrapper">
                  <div className="taskBoard-sprint-header">
                    <div className="taskBoard-sprint-header-actions">
                      <div style={{ fontSize: '12px', color: '#888' }}>
                        {sprint.startDate ? `Start: ${dayjs(sprint.startDate).format('YYYY-MM-DD')}` : '-'}
                        {' | '}
                        {sprint.endDate ? `Due: ${dayjs(sprint.endDate).format('YYYY-MM-DD')}` : '-'}
                      </div>
                      <Button
                        type="text"
                        icon={<EditOutlined />}
                        onClick={() => {
                          setEditingSprint(sprint);
                          setIsEditModalVisible(true);
                        }}
                      />
                      <Button
                        type="default"
                        size="small"
                        icon={<PlayCircleOutlined />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleStartSprint(sprint.id);
                        }}
                      >
                        Start Sprint
                      </Button>
                      <DeleteSprintButton
                        sprintId={sprint.id}
                        onDeleteSuccess={fetchData}
                      />
                    </div>
                  </div>
                  <KanbanColumn
                    showAddButton={false}
                    key={`sprint-${sprint.id}`}
                    id={`sprint-${sprint.id}`}
                    title={(
                      <div>
                        Sprint {index + 1} - {sprint.name}
                      </div>
                    )}
                    count={sprintTasks[sprint.id]?.length || 0}
                  >
                    {sprintTasks[sprint.id]?.map((task) => (
                      <KanbanItem
                        key={task.id}
                        id={task.id.toString()}
                        data={task}
                      >
                        <TaskCardMemo {...task} />
                      </KanbanItem>
                    ))}
                  </KanbanColumn>
                </div>
              ))}

            <KanbanColumn
              id="backlog"
              title="Backlog"
              count={backlogTasks.length}
              showAddButton={false}
            >
              {backlogTasks.map((task) => (
                <KanbanItem
                  key={task.id}
                  id={task.id.toString()}
                  data={task}
                >
                  <TaskCardMemo {...task} />
                </KanbanItem>
              ))}
            </KanbanColumn>
          </Space>

        </div>
      </DndContext>

      <SprintEditModal
        visible={isEditModalVisible}
        sprint={editingSprint}
        onClose={() => {
          setIsEditModalVisible(false);
          setEditingSprint(null);
        }}
        onSuccess={fetchData}
      />
      <SprintCreateModal
        visible={isCreateModalVisible}
        onClose={() => {
          setIsCreateModalVisible(false);
        }}
        onSuccess={fetchData}
      />
    </div>
  );
};