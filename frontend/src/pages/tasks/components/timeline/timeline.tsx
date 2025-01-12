import React, { useState, useEffect } from 'react';
import TimeLine from 'react-gantt-timeline';
import { Card, message, Radio } from 'antd';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './timeline.less';

interface TaskData {
    id: number | string;
    name: string;
    start: Date;
    end: Date;
    color: string;
    type: 'sprint' | 'task';
    sprintId?: number;
    isCollapsed?: boolean;
    hidden?: boolean;
    style?: React.CSSProperties;
}

type ViewMode = 'day' | 'week' | 'month';

const GanttChart = () => {
    const { id: projectId } = useParams();
    const [viewMode, setViewMode] = useState<ViewMode>('month');
    const [data, setData] = useState<TaskData[]>([]);
    const [links, setLinks] = useState([]);
    const [loading, setLoading] = useState(true);

    const sprintColors = [
        { bg: '#CCE7FF', bar: '#CCE7FF' },
        { bg: '#b4a7d6', bar: '#b4a7d6' },
        { bg: '#FFE1C7', bar: '#FFE1C7' },
        { bg: '#FFB3B3', bar: '#FFB3B3' },
    ];

    const fetchData = async () => {
        try {
            setLoading(true);
            const [sprintsResponse, tasksResponse] = await Promise.all([
                axios.get(`/api/projects/${projectId}/sprints`, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem('token')}`,
                    },
                }),
                axios.get(`/api/projects/${projectId}/tasks`, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem('token')}`,
                    },
                }),
            ]);

            const sprints = sprintsResponse.data;
            const tasks = tasksResponse.data;

            const doneSprints = sprints.filter((sprint: any) => sprint.status === 'IN_PROGRESS');

            const timelineData: TaskData[] = [];

            doneSprints.forEach((sprint: any, index: number) => {
                const sprintStart = new Date(sprint.startDate);
                const sprintEnd = new Date(sprint.endDate);
                const sprintColor = sprintColors[index % sprintColors.length];

                // 添加样式到冲刺
                timelineData.push({
                    id: `sprint-${sprint.id}`,
                    name: `Sprint: ${sprint.name}`,
                    start: sprintStart,
                    end: sprintEnd,
                    color: sprintColor.bg,
                    type: 'sprint',
                    isCollapsed: false,
                    style: {
                        backgroundColor: '#cbae9b',
                        color: "white",
                        fontWeight: "bold",
                    },
                });

                // 过滤该冲刺下的任务
                const sprintTasks = tasks.filter((task: any) => task.sprintId === sprint.id);
                sprintTasks.forEach((task: any) => {
                    const taskStart = new Date(task.startDate);
                    const taskEnd = new Date(task.dueDate);

                    const adjustedStart = taskStart < sprintStart ? sprintStart : taskStart;
                    const adjustedEnd = taskEnd > sprintEnd ? sprintEnd : taskEnd;

                    // 添加样式到任务
                    timelineData.push({
                        id: `task-${task.id}`,
                        name: `  ${task.title}`,
                        start: adjustedStart,
                        end: adjustedEnd,
                        color: sprintColor.bg,
                        type: 'task',
                        sprintId: sprint.id,
                        style: {
                            backgroundColor: "#8f1d1d", // 任务样式
                            color: "white",
                            borderRadius: "8px",
                        },
                    });
                });
            });

            setData(timelineData);
        } catch (error) {
            console.error('Error fetching data:', error);
            message.error('Failed to load timeline data');
        } finally {
            setLoading(false);
        }
    };


    const onUpdateTask = async (item: any, props: any) => {
        // 获取任务的真实 ID
        const [type, id] = item.id.toString().split('-');
        const startDate = props.start.toISOString();
        const endDate = props.end.toISOString();
        const sprintId = item.sprintId;

        try {
            if (type === 'task') {
                // 更新任务
                await axios.put(`/api/tasks/${id}`, {
                    startDate: startDate,
                    dueDate: endDate,
                    sprintId: sprintId,
                }, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem('token')}`,
                        'Content-Type': 'application/json'
                    }
                });
            } else if (type === 'sprint') {
                // 更新冲刺
                await axios.put(`/api/sprints/${id}`, {
                    startDate: startDate,
                    endDate: endDate,
                }, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem('token')}`,
                        'Content-Type': 'application/json'
                    }
                });
            }

            setData(prevData =>
                prevData.map(d =>
                    d.id === item.id ? { ...d, start: props.start, end: props.end } : d
                )
            );
            message.success(`${type === 'sprint' ? 'Sprint' : 'Task'} updated successfully`);
        } catch (error) {
            console.error('Error updating task or sprint:', error);
            message.error(`Failed to update ${type === 'sprint' ? 'sprint' : 'task'}`);
            fetchData(); // 重新获取数据以确保显示最新状态
        }
    };

    const onCreateLink = (item: any) => {
        console.log('Link created:', item);
    };

    useEffect(() => {
        fetchData();
    }, [projectId]);

    return (
        <Card
            title="Sprint Timeline"
            bordered={false}
            extra={
                <Radio.Group
                    value={viewMode}
                    onChange={(e) => setViewMode(e.target.value)}
                >
                    <Radio.Button value="day">Day</Radio.Button>
                    <Radio.Button value="week">Week</Radio.Button>
                    <Radio.Button value="month">Month</Radio.Button>
                </Radio.Group>
            }
        >
            <div className="timeline-container" style={{ height: 'calc(100vh - 200px)' }}>
                {loading ? (
                    <div>Loading...</div>
                ) : data.length > 0 ? (
                    <TimeLine
                        data={data}
                        links={links}
                        onUpdateTask={onUpdateTask}
                        onCreateLink={onCreateLink}
                        mode={viewMode}
                        itemHeight={40}
                    />
                ) : (
                    <div>No data available</div>
                )}
            </div>
        </Card>
    );
};

export default GanttChart;
