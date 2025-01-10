// src/pages/projects/edit.tsx
import { Form, Input, Modal, message } from "antd";
import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { log } from "console";

const { TextArea } = Input;

export const ProjectsEditPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [form] = Form.useForm();
  const [visible, setVisible] = useState(true);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      console.log(values);

      // 添加 headers
      await axios.put(
        `/api/projects/${id}`,
        values,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json',
          }
        }
      );

      message.success('Project updated successfully');
      handleCancel();
    } catch (error) {
      message.error('Failed to update project');
    } finally {
      setLoading(false);
    }
  };


  // 处理取消
  const handleCancel = () => {
    setVisible(false);
    navigate("/projects");
  };

  return (
    <Modal
      open={visible}
      title="Edit Project"
      width={512}
      onCancel={handleCancel}
      onOk={() => form.submit()}
      confirmLoading={loading}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
      >
        <Form.Item
          label="Name"
          name="name"
          rules={[{ required: true, message: "Please input project name!" }]}
        >
          <Input placeholder="Enter project name" />
        </Form.Item>

        <Form.Item
          label="Description"
          name="description"
          rules={[{ required: true, message: "Please input project description!" }]}
        >
          <TextArea
            rows={4}
            placeholder="Enter project description"
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
