import React from 'react';
import { useParams } from 'react-router-dom';
import { useOne } from "@refinedev/core";
import { Spin, Alert } from 'antd';

export const TaskBoardPage: React.FC = () => {
  const { id } = useParams();

  const { data, isLoading, isError } = useOne({
    resource: "projects",
    id: id as string,
  });

  if (isLoading) {
    return (
      <div style={{
        padding: "24px",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh"
      }}>
        <Spin size="large" />
      </div>
    );
  }

  if (isError) {
    return (
      <div style={{ padding: "24px" }}>
        <Alert
          message="错误"
          description="加载项目信息失败"
          type="error"
          showIcon
        />
      </div>
    );
  }

  return (
    <div style={{ padding: "24px" }}>
      <h1>{data?.data.name} - 任务看板</h1>
      <div style={{ marginTop: "16px" }}>
        {/* 这里后续添加看板内容 */}
        <p>项目描述: {data?.data.description}</p>
      </div>
    </div>
  );
};

export default TaskBoardPage;
