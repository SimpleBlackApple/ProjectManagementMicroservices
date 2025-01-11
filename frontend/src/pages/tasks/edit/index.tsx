import { useState, useEffect } from "react";
import { Modal, message, Form, Input, DatePicker, Slider, Select } from "antd";
import { useParams, useNavigate } from 'react-router-dom';
import dayjs from "dayjs";
import { ProjectMembers } from '../components/member/index';

const { TextArea } = Input;

interface Task {
  id: string;
  title: string;
  description: string;
  type: string;
  storyPoints: number;
  status: string;
  startDate: string;
  dueDate: string;
  managerId?: number;
  sprintId?: number;
}

export const TasksEditPage = () => {
  const { id, taskId } = useParams();
  const navigate = useNavigate();
  const [task, setTask] = useState<Task | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [form] = Form.useForm();


  // 获取原始任务数据
  useEffect(() => {
    const fetchTask = async () => {
      try {
        const response = await fetch(
          `/api/tasks/${taskId}`,
          {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`,
              'Content-Type': 'application/json'
            }
          }
        );
        if (!response.ok) {
          throw new Error('Failed to fetch task');
        }
        const data = await response.json();
        setTask(data);
        // 设置表单初始值
        form.setFieldsValue({
          title: data.title,
          description: data.description,
          type: data.type,
          status: data.status,
          storyPoints: data.storyPoints,
          startDate: data.startDate ? dayjs(data.startDate) : undefined,
          dueDate: data.dueDate ? dayjs(data.dueDate) : undefined,
          managerId: data.managerId
        });
      } catch (error) {
        message.error('Failed to load task');
      } finally {
        setIsLoading(false);
      }
    };

    fetchTask();
    // window.location.reload();
  }, [taskId, form]);

  // 处理表单提交
  const handleSubmit = async (values: any) => {
    try {
      const formData = {
        ...values,
        startDate: values.startDate?.format('YYYY-MM-DDTHH:00:00'),
        dueDate: values.dueDate?.format('YYYY-MM-DDTHH:00:00'),
        managerId: values.managerId ? Number(values.managerId) : null,
        sprintId: task?.sprintId
      };
      console.log('Submitting form data:', formData);

      const response = await fetch(
        `/api/tasks/${taskId}`,
        {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(formData),
        }
      );

      if (!response.ok) {
        throw new Error('Failed to update task');
      }
      const updatedTask = await response.json(); // 获取后端返回的最新任务数据

      message.success('Task updated successfully');

      navigate(`/projects/${id}/backlog/`, { state: { updatedTask } });
      window.location.reload();
    } catch (error) {
      message.error('Failed to update task');
    }
  };


  return (
    <Modal
      visible={true}
      onCancel={() => navigate(`/projects/${id}/backlog/`)}
      title="Edit Task"
      width={512}
      okText="Update"
      onOk={() => form.submit()}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        disabled={isLoading}
      >
        <Form.Item
          label="Title"
          name="title"
          rules={[{ required: true, message: "Please input task title!" }]}
        >
          <Input />
        </Form.Item>

        <Form.Item
          label="Description"
          name="description"
          rules={[{ required: true, message: "Please input task description!" }]}
        >
          <TextArea rows={4} />
        </Form.Item>

        <Form.Item
          label="Status"
          name="status"
          rules={[{ required: true, message: "Please select task status!" }]}
        >
          <Select>
            <Select.Option value="TO_DO">To Do</Select.Option>
            <Select.Option value="IN_PROGRESS">In Progress</Select.Option>
            <Select.Option value="DONE">Done</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="Type"
          name="type"
          rules={[{ required: true, message: "Please select task type!" }]}
        >
          <Select>
            <Select.Option value="user_story">User Story</Select.Option>
            <Select.Option value="bug">Bug</Select.Option>
            <Select.Option value="task">Task</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="Story Points"
          name="storyPoints"
          rules={[{ required: true, message: "Please select story points!" }]}
        >
          <Slider
            marks={{
              0: '0',
              2: '2',
              5: '5',
              8: '8',
              10: '10'
            }}
            min={0}
            max={10}
            step={1}
            tooltip={{
              formatter: (value) => `${value} points`
            }}
          />
        </Form.Item>

        <Form.Item
          label="Assignee"
          name="managerId"
          rules={[{ required: false, message: "Please select an assignee!" }]}
        >
          <ProjectMembers
            projectId={id as string}
            displayManagement={false}
            render={(members) => (
              <Select
                placeholder="Select an assignee"
                allowClear
                showSearch
                value={form.getFieldValue('managerId')}
                onChange={(value) => {
                  console.log('Selected value:', value);
                  form.setFieldValue('managerId', value);
                }}
                filterOption={(input, option) =>
                  option && typeof option.label === 'string'
                    ? option.label.toLowerCase().includes(input.toLowerCase())
                    : false
                }
              >
                {members.map(member => (
                  <Select.Option key={member.id} value={member.id}>
                    {member.name}
                  </Select.Option>
                ))}
              </Select>
            )}
          />
        </Form.Item>

        <Form.Item
          label="Start Date"
          name="startDate"
          rules={[{ required: true, message: "Please select start date!" }]}
        >
          <DatePicker
            style={{ width: '100%' }}
            showTime={{
              format: 'HH',
              minuteStep: 30,
              secondStep: 30
            }}
            format="YYYY-MM-DD HH:00"
          />
        </Form.Item>

        <Form.Item
          label="Due Date"
          name="dueDate"
          rules={[{ required: true, message: "Please select due date!" }]}
        >
          <DatePicker
            style={{ width: '100%' }}
            showTime={{
              format: 'HH',
              minuteStep: 30,
              secondStep: 30
            }}
            format="YYYY-MM-DD HH:00"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
