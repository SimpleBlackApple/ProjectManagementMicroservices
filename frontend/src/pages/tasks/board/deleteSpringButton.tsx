import React from "react";
import { Button, message, Popconfirm } from "antd";
import { DeleteOutlined } from "@ant-design/icons";
import axios from "axios";

type DeleteSprintButtonProps = {
  sprintId: number | null;
  onDeleteSuccess?: () => void; // 回调函数，用于成功删除后的刷新等操作
};

export const DeleteSprintButton: React.FC<DeleteSprintButtonProps> = ({
  sprintId,
  onDeleteSuccess,
}) => {
  const handleDelete = async () => {
    try {
      await axios.delete(`/api/sprints/${sprintId}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      message.success("Sprint deleted successfully!");
      onDeleteSuccess?.(); // 调用回调函数
    } catch (error) {
      console.error("Error deleting sprint:", error);
      message.error("Failed to delete sprint.");
    }
  };

  return (
    <Popconfirm
      title="Are you sure you want to delete this sprint?"
      onConfirm={handleDelete}
      okText="Yes"
      cancelText="No"
    >
      <Button
        type="primary"
        size="small"
        danger
        icon={<DeleteOutlined />}
      >
        {/* Delete */}
      </Button>
    </Popconfirm>
  );
};
