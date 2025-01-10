import { useModalForm } from "@refinedev/antd";
import { Form, Input, Modal, DatePicker, Button } from "antd";
import React from "react";
import { useParams } from "react-router-dom";
import dayjs from 'dayjs';
import axios from "axios";

const { RangePicker } = DatePicker;

interface SprintFormValues {
  name: string;
  sprintDates: [dayjs.Dayjs, dayjs.Dayjs];
}
interface SprintCreateModalProps {
  visible: boolean;
  onClose: () => void;
  onSuccess: () => void;
}
export const SprintCreateModal: React.FC<SprintCreateModalProps> = ({
  visible,
  onSuccess,
  onClose
}) => {

  const { id: projectId } = useParams();
  const [form] = Form.useForm<SprintFormValues>();
  const { formProps, modalProps } = useModalForm({
    action: "create",
    resource: "sprints",  // 修改为直接使用 sprints
    redirect: false,
    warnWhenUnsavedChanges: false,
    successNotification: {
      message: "Successfully created sprint",
      type: "success"
    },
    errorNotification: {
      message: "Error creating sprint",
      type: "error"
    },
  });

  React.useEffect(() => {
  }, [visible]);

  const handleSubmit = async (values: SprintFormValues) => {
    try {
      await axios.post(
        `/api/projects/${projectId}/sprints`,
        {
          name: values.name,
          startDate: values.sprintDates[0].format('YYYY-MM-DDTHH:mm:ss'),
          endDate: values.sprintDates[1].format('YYYY-MM-DDTHH:mm:ss'),
          totalStoryPoints: 0,
          completedStoryPoints: 0,
          status: "TO_DO" as const
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
      // {...modalProps}
      onCancel={onClose}
      title="Create New Sprint"
      width={512}
      onOk={() => form.submit()}
    >
      <Form<SprintFormValues>
        {...formProps}
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
