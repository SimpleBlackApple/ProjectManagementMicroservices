import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useOne } from "@refinedev/core";
import { DndContext, DragEndEvent, closestCenter } from "@dnd-kit/core";
import { Spin, Alert, Button, Space, Modal, Tooltip, Tag } from 'antd';
import { DeleteOutlined, EditOutlined, PlayCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { DeleteSprintButton } from "./deleteSpringButton";
import { KanbanColumn } from '../components/column';
import { KanbanItem } from '../components/item';
import { TaskCardMemo } from '../components/card';
import { Sprint, Task, Project } from '@/restful/types';
import { ProjectMembers } from '../components/member/index';
import { SprintEditModal } from './edit'
import { SprintCreateModal } from './create'
import axios from 'axios';
import './index.less';
import dayjs from 'dayjs';
import { KanbanBoard, KanbanBoardContainer } from '../components/board';

interface TaskBoardPageProps {
  children?: React.ReactNode;
}

export const TaskBoardPage: React.FC<TaskBoardPageProps> = ({ children }) => {
  const {id: projectId} = useParams();
  const navigate = useNavigate();
  const [sprints, setSprints] = useState<Sprint[]>([]);
  const [sprintTasks, setSprintTasks] = useState<Record<number, Task[]>>({});
  const [backlogTasks, setBacklogTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [isEditModalVisible, setIsEditModalVisible] = useState(false);
  const [editingSprint, setEditingSprint] = useState<Sprint | null>(null);
  const [isCreateModalVisible, setIsCreateModalVisible] = useState(false);
  const {data: projectData, isLoading: isProjectLoading, isError} = useOne<Project>({
    resource: "projects",
    id: projectId as string,
  });

  const fetchData = async () => {
    try {
      setLoading(true);
      console.log('Fetching data for project:', projectId);

      // 获取所有 sprints
      const sprintsResponse = await axios.get(
        `/api/projects/${projectId}/sprints`,
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
        `/api/projects/${projectId}/tasks`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        }
      );
      const allTasks = allTasksResponse.data;
      console.log('All tasks fetched:', allTasks)

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
      if (axios.isAxiosError(error)) {
        Modal.error({
          title: 'Failed to load data',
          content: error.response?.data || 'Unknown error occurred'
        });
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (projectId) {
      fetchData();
    }
  }, [projectId]);

  const handleDragEnd = async (event: DragEndEvent) => {
    const {active, over} = event;
    if (!over || active.id === over.id) return;

    const task = active.data.current as Task;
    const taskId = parseInt(active.id as string);
    const targetId = over.id as string;

    try {
      const newSprintId = targetId.startsWith('sprint-')
        ? parseInt(targetId.replace('sprint-', ''))
        : null;

      // 当移动到某个 sprint 时
      if (newSprintId !== null) {
        const targetSprint = sprints.find(sprint => sprint.id === newSprintId);
        if (!targetSprint) return;

        // 检查任务的时间范围并调整
        const taskStartDate = dayjs(task.startDate);
        const taskDueDate = dayjs(task.dueDate);
        const sprintStartDate = dayjs(targetSprint.startDate);
        const sprintEndDate = dayjs(targetSprint.endDate);

        let newStartDate = task.startDate;
        let newDueDate = task.dueDate;

        // 如果任务的开始或结束时间超出 sprint 范围，则将整个任务时间调整为 sprint 的时间范围
        if (taskStartDate.isBefore(sprintStartDate) || taskDueDate.isAfter(sprintEndDate)) {
          newStartDate = targetSprint.startDate;
          newDueDate = targetSprint.endDate;
        }

        // 如果是在 sprints 之间移动
        if (task.sprintId !== newSprintId && task.sprintId !== null) {
          const sourceSprint = sprints.find(sprint => sprint.id === task.sprintId);

          Modal.confirm({
            title: 'Move Issue',
            content: (
              <div>
                <p>This action will affect the sprint scope</p>
                <p><strong>{task.title}</strong> will be moved from sprint <strong>{sourceSprint?.name}</strong> to
                  sprint <strong>{targetSprint.name}</strong>.</p>
                {(newStartDate !== task.startDate || newDueDate !== task.dueDate) && (
                  <p style={{color: '#ff4d4f'}}>
                    Task duration will be adjusted to fit within sprint timeline.
                  </p>
                )}
              </div>
            ),
            onOk: async () => {
              try {
                await axios.put(
                  `/api/tasks/${taskId}`,
                  {
                    sprintId: newSprintId,
                    startDate: newStartDate,
                    dueDate: newDueDate
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
          // 如果是从 backlog 移动到 sprint
          await axios.put(
            `/api/tasks/${taskId}`,
            {
              sprintId: newSprintId,
              startDate: newStartDate,
              dueDate: newDueDate
            },
            {
              headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
              }
            }
          );
          await fetchData();
        }
      } else {
        // 如果移动到 backlog，只更新 sprintId
        await axios.put(
          `/api/tasks/${taskId}`,
          {sprintId: newSprintId},
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
        `/api/sprints/${sprint.id}`,
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
              <p style={{color: '#ff4d4f'}}>{error.response.data}</p>
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

  const getSprintButtonProps = (sprint: Sprint) => {
    switch (sprint.status) {
      case 'TO_DO':
        return {
          type: 'default' as const,
          children: 'Start Sprint',
          icon: <PlayCircleOutlined/>,
          title: 'Click to start the sprint'
        };
      case 'IN_PROGRESS':
        return {
          type: 'primary' as const,
          children: 'Started',
          style: {backgroundColor: '#52c41a', borderColor: '#52c41a'},
          icon: <PlayCircleOutlined/>,
          title: 'Click to mark as done'
        };
      case 'DONE':
        return {
          type: 'primary' as const,
          children: 'Done',
          style: {backgroundColor: '#1890ff', borderColor: '#1890ff'},
          icon: <PlayCircleOutlined/>,
          title: 'Click to restart sprint'
        };
      default:
        return {
          type: 'default' as const,
          children: 'Start Sprint',
          icon: <PlayCircleOutlined/>,
          title: 'Click to start the sprint'
        };
    }
  };

  if (loading || isProjectLoading) {
    return <Spin spinning={true}/>;
  }

  if (isError) {
    return <Alert type="error" message="Error loading project data"/>;
  }

  const calculateSprintPoints = (sprintId: number) => {
    const tasks = sprintTasks[sprintId] || [];
    const totalPoints = tasks.reduce((sum, task) => sum + (task.storyPoints || 0), 0);
    const completedPoints = tasks
      .filter(task => task.status === 'DONE')
      .reduce((sum, task) => sum + (task.storyPoints || 0), 0);

    return {totalPoints, completedPoints};
  };

  return (
    <div className="taskBoard">
      <Space direction="vertical" size="large" className="taskBoard-container">
        <div className="taskBoard-header">
          <div style={{display: 'flex', alignItems: 'center', gap: '16px'}}>
            <h1 className="taskBoard-header-title">
              {projectData?.data?.name} - Sprint Board
            </h1>
            <ProjectMembers projectId={projectId as string}/>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined/>}
            onClick={() => setIsCreateModalVisible(true)}
            size="small"
          >
            Create Sprint
          </Button>
        </div>

        <KanbanBoard onDragEnd={handleDragEnd}>
          {sprints.map((sprint: Sprint, index: number) => (
            <div key={`sprint-${sprint.id}`} className="taskBoard-sprint">
              <div className="taskBoard-sprint-header">
                <div className="taskBoard-sprint-title">
                  <span className="sprint-title">
          {`${index + 1}. ${sprint.name}`}
        </span>
                  <span className="sprint-number">{sprintTasks[sprint.id]?.length || 0}</span>
                  <Tag color="success" className="points-tag">
                    {calculateSprintPoints(sprint.id).completedPoints}/
                    {calculateSprintPoints(sprint.id).totalPoints} pts
                  </Tag>
                  <span className="date-info">
          Start: {dayjs(sprint.startDate).format('YYYY-MM-DD')} |
          Due: {dayjs(sprint.endDate).format('YYYY-MM-DD')}
        </span>
                </div>
                <div className="taskBoard-sprint-actions">
                  <Button
                    type="text"
                    icon={<EditOutlined />}
                    onClick={() => {
                      setEditingSprint(sprint);
                      setIsEditModalVisible(true);
                    }}
                    size="small"
                  />
                  <Tooltip title={getSprintButtonProps(sprint).title}>
                    <Button
                      {...getSprintButtonProps(sprint)}
                      size="small"
                      onClick={() => handleStartSprint(sprint)}
                    />
                  </Tooltip>
                  <DeleteSprintButton
                    sprintId={sprint.id}
                    onDeleteSuccess={fetchData}
                  />
                </div>
              </div>

              <KanbanColumn
                id={`sprint-${sprint.id}`}
                title=""
                count={0} // 设置为 0 以隐藏重复的数量显示
                showAddButton={false}
              >
                {sprintTasks[sprint.id]?.map((task) => (
                  <KanbanItem
                    key={task.id!}
                    id={task.id!.toString()}
                    data={task}
                  >
                    <TaskCardMemo {...task} sprintId={task.sprintId ?? null} />
                  </KanbanItem>
                ))}
              </KanbanColumn>
            </div>
          ))}
          <div className="taskBoard-sprint">
            <KanbanColumn
              id="backlog"
              title="Backlog"
              count={backlogTasks.length}
              showAddButton={false}
            >
              {backlogTasks.map((task) => (
                <KanbanItem
                  key={task.id!}
                  id={task.id!.toString()}
                  data={task}
                >
                  <TaskCardMemo {...task} sprintId={task.sprintId ?? null}/>
                </KanbanItem>
              ))}
            </KanbanColumn>
          </div>
        </KanbanBoard>
      </Space>

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
        onClose={() => setIsCreateModalVisible(false)}
        onSuccess={fetchData}
      />
    </div>
  );
};

export default TaskBoardPage;
