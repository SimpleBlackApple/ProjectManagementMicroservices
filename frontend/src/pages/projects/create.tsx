import { useModalForm } from "@refinedev/antd";
import { Form, Input, Modal } from "antd";
import React from "react";
import { useNavigate } from "react-router-dom";

const { TextArea } = Input;

interface ProjectsCreatePageProps {
  children?: React.ReactNode;
}

export const ProjectsCreatePage: React.FC<ProjectsCreatePageProps> = () => {
  const navigate = useNavigate();
  
  const { formProps, modalProps, close, show } = useModalForm({
    action: "create",
    resource: "projects",
    redirect: false,
    warnWhenUnsavedChanges: false,
    onMutationSuccess: () => {
      close();
      navigate("/projects");
    },
    // onMutationError: (error) => {
    //   console.error("Create project error:", error);
    // },
  });
  React.useEffect(() => {
    show();
  }, [show]);

  return (
    <Modal
      {...modalProps}
      onCancel={() => {
        close();
        navigate("/projects");
      }}
      title="Create New Project"
      width={512}
    >
      <Form
        {...formProps}
        layout="vertical"
        onFinish={(values) => {
          formProps.onFinish?.(values);
        }}
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