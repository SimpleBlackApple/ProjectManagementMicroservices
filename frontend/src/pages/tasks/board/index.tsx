import React, { useState, useEffect, Children } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useOne } from "@refinedev/core";
import { DndContext, DragEndEvent, closestCenter } from "@dnd-kit/core";
import { Spin, Alert, Button, Space, Modal, Tooltip } from 'antd';
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
import { KanbanBoard, KanbanBoardContainer } from '../components/board';

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
      const newSprintId = targetId.startsWith('sprint-')
        ? parseInt(targetId.replace('sprint-', ''))
        : null;

      // Only show confirmation when moving between sprints
      if (task.sprintId !== newSprintId && task.sprintId !== null && newSprintId !== null) {
        // Get source and target sprint information
        const sourceSprint = sprints.find(sprint => sprint.id === task.sprintId);
        const targetSprint = sprints.find(sprint => sprint.id === newSprintId);

        Modal.confirm({
          title: 'Move Issue',
          content: (
            <div>
              <p>This action will affect the sprint scope</p>
              <p><strong>{task.title}</strong> will be moved from sprint <strong>{sourceSprint?.name}</strong> to sprint <strong>{targetSprint?.name}</strong>.</p>
            </div>
          ),
          onOk: async () => {
            try {
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
              await fetchData();
            } catch (error) {
              if (axios.isAxiosError(error) && error.response) {
                Modal.error({
                  title: 'Failed',
                  content: error.response.data
                });
                console.error('Server error response:', error.response.data);
              }
              console.error('Error updating task:', error);
            }
          },
          okText: 'Confirm',
          cancelText: 'Cancel',
        });
      } else {
        // If not moving between sprints (e.g., moving to backlog), update directly
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
        await fetchData();
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        Modal.error({
          title: 'Failed',
          content: error.response.data
        });
        console.error('Server error response:', error.response.data);
      }
      console.error('Error updating task:', error);
    }
  };

  const handleStartSprint = async (sprint: Sprint) => {
    try {
      let newStatus: 'TO_DO' | 'IN_PROGRESS' | 'DONE';
      let newStartDate = null;

      // Status cycle: TO_DO -> IN_PROGRESS -> DONE -> TO_DO
      switch (sprint.status) {
        case 'TO_DO':
          newStatus = 'IN_PROGRESS';
          newStartDate = new Date().toISOString();
          break;
        case 'IN_PROGRESS':
          newStatus = 'DONE';
          break;
        case 'DONE':
          newStatus = 'TO_DO';
          newStartDate = null; // Reset start date when going back to TO_DO
          break;
        default:
          newStatus = 'TO_DO';
      }

      await axios.put(
        `http://localhost:8083/api/sprints/${sprint.id}`,
        {
          status: newStatus,
          startDate: newStartDate
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
      console.error('Error updating sprint:', error);
      if (axios.isAxiosError(error) && error.response) {
        Modal.error({
          title: 'Failed',
          content: (
            <div>
              <p>Failed to update sprint status</p>
              <p style={{ color: '#ff4d4f' }}>{error.response.data}</p>
            </div>
          )
        });
      } else {
        Modal.error({
          title: 'Failed',
          content: 'Failed to update sprint status'
        });
      }
    }
  };

  // In the render part:
  const getSprintButtonProps = (sprint: Sprint) => {
    switch (sprint.status) {
      case 'TO_DO':
        return {
          type: 'default' as const,
          children: 'Start Sprint',
          icon: <PlayCircleOutlined />,
          title: 'Click to start the sprint'
        };
      case 'IN_PROGRESS':
        return {
          type: 'primary' as const,
          children: 'Started',
          style: { backgroundColor: '#52c41a', borderColor: '#52c41a' },
          icon: <PlayCircleOutlined />,
          title: 'Click to mark as done'
        };
      case 'DONE':
        return {
          type: 'primary' as const,
          children: 'Done',
          style: { backgroundColor: '#1890ff', borderColor: '#1890ff' },
          icon: <PlayCircleOutlined />,
          title: 'Click to restart sprint'
        };
      default:
        return {
          type: 'default' as const,
          children: 'Start Sprint',
          icon: <PlayCircleOutlined />,
          title: 'Click to start the sprint'
        };
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
          {/* <KanbanBoardContainer> */}
          <KanbanBoard onDragEnd={handleDragEnd}>
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
                      <Tooltip title={getSprintButtonProps(sprint).title}>
                        <Button
                          {...getSprintButtonProps(sprint)}
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleStartSprint(sprint);
                          }}
                        />
                      </Tooltip>
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
                        {index + 1} . {sprint.name}
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
                  <br />
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
          </KanbanBoard>
        </Space>
      </div>


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