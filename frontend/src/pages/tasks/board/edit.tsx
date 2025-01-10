// components/SprintEditModal.tsx
import { useModalForm } from "@refinedev/antd";
import { Form, Input, Modal, DatePicker } from "antd";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import dayjs from 'dayjs';
import axios from 'axios';

const { RangePicker } = DatePicker;

interface SprintFormValues {
  name: string;
  sprintDates: [dayjs.Dayjs, dayjs.Dayjs];
}

interface SprintEditModalProps {
  visible: boolean;
  sprint: {
    id: number;
    name: string;
    startDate: string;
    endDate: string;
  } | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const SprintEditModal: React.FC<SprintEditModalProps> = ({
  visible,
  sprint,
  onClose,
  onSuccess
}) => {

  const [form] = Form.useForm<SprintFormValues>();

  // 设置初始值
  React.useEffect(() => {
    if (sprint && visible) {
      form.setFieldsValue({
        name: sprint.name,
        sprintDates: [
          dayjs(sprint.startDate),
          dayjs(sprint.endDate)
        ]
      });
    }
  }, [sprint, visible]);

  const handleSubmit = async (values: SprintFormValues) => {
    try {
      await axios.put(
        `/api/sprints/${sprint?.id}`,
        {
          name: values.name,
          startDate: values.sprintDates[0].format('YYYY-MM-DDTHH:mm:ss'),
          endDate: values.sprintDates[1].format('YYYY-MM-DDTHH:mm:ss')
        },
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        }
      );
      onSuccess();
      onClose();
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        Modal.error({
          title: '更新失败',
          content: error.response.data
        });
      }
    }
  };

  return (
    <Modal
      open={visible}
      onCancel={onClose}
      title="Edit Sprint"
      width={512}
      onOk={() => form.submit()}
    >
      <Form<SprintFormValues>
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
      >
        <Form.Item
          label="Sprint Name"
          name="name"
          rules={[{ required: true, message: "Please input sprint name!" }]}
        >
          <Input placeholder="Enter sprint name" />
        </Form.Item>

        <Form.Item
          label="Sprint Duration"
          name="sprintDates"
          rules={[{ required: true, message: "Please select sprint duration!" }]}
        >
          <RangePicker
            showTime={{ format: 'HH:mm' }}
            format="YYYY-MM-DD HH:mm"
            style={{ width: '100%' }}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
