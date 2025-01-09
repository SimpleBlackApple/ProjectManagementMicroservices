import { useSearchParams } from "react-router-dom";
import { useModalForm } from "@refinedev/antd";
import { useNavigate, useParams } from 'react-router-dom';
import { Form, Input, Modal, DatePicker, Slider, Select } from "antd";
import dayjs, { Dayjs } from "dayjs";
import { useInvalidate } from "@refinedev/core";

const { TextArea } = Input;

interface TaskFormValues {
  title?: string;
  description?: string;
  startDate?: Dayjs;
  dueDate?: Dayjs;
  status?: string;
  type?: 'user_story' | 'bug' | 'task';
  storyPoints?: number;
}
export const TasksCreatePage = () => {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const invalidate = useInvalidate();

  const { formProps, modalProps, close } = useModalForm({
    action: "create",
    defaultVisible: true,
    resource: `projects/${id}/tasks`,
    redirect: false,
    warnWhenUnsavedChanges: false,
    onMutationSuccess: () => {
      invalidate({
        resource: "tasks",
        invalidates: ["list"],
      });
      close();
      navigate(`/projects/${id}/backlog/`);
    },

  });

  return (
    <Modal
      {...modalProps}
      onCancel={() => {
        close();
        navigate(`/projects/${id}/backlog/`);
      }}

      title="Add new card"
      width={512}
    >
      <Form
        {...formProps}
        layout="vertical"
        onFinish={(values: TaskFormValues) => {
          const formData = {
            ...values,
            status: searchParams.get("status") || "TO_DO",
            startDate: values.startDate?.format('YYYY-MM-DDTHH:00:00'),
            dueDate: values.dueDate?.format('YYYY-MM-DDTHH:00:00'),
          };
          formProps.onFinish?.(formData);
          console.log(formData)
        }}
        initialValues={{
          description: "Please describe your task here...",
          storyPoints: 10,
          type: "user_story"
        }}
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
          label="Type"
          name="type"
          initialValue="user_story"
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
          initialValue={0}
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
          label="Start Date"
          name="startDate"
          rules={[{ required: true, message: "Please select start date!" }]}
          getValueProps={(value) => ({
            value: value ? dayjs(value) : undefined,
          })}
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
          getValueProps={(value) => ({
            value: value ? dayjs(value) : undefined,
          })}
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
