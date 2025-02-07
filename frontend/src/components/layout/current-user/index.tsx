import React from "react";
import { useGetIdentity, useLogout } from "@refinedev/core";
import { SettingOutlined, LogoutOutlined } from "@ant-design/icons";
import { Popover, Menu } from "antd"; // 改用 Menu
import type { User } from "@/graphql/schema.types";
import { CustomAvatar } from "../../../pages/tasks/components/member/avatar";
import { Text } from "../../text";
import { AccountSettings } from "../account-settings";

export const CurrentUser = () => {
  const [opened, setOpened] = React.useState(false);
  const { data: user } = useGetIdentity<User>();
  const { mutate: logout } = useLogout();

  const content = (
    <div style={{ display: "flex", flexDirection: "column" }}>
      <Text strong style={{ padding: "12px 20px" }}>
        {user?.name}
      </Text>
      <Menu
        mode="vertical"
        style={{
          border: "none",
          borderTop: "1px solid #d9d9d9",
        }}
        items={[
          {
            key: "settings",
            icon: <SettingOutlined />,
            label: "Account settings",
            onClick: () => setOpened(true),
          },
          {
            key: "logout",
            icon: <LogoutOutlined />,
            label: "Logout",
            onClick: () => logout(),
          },
        ]}
      />
    </div>
  );

  return (
    <>
      <Popover
        placement="bottomRight"
        content={content}
        trigger="click"
        overlayInnerStyle={{ padding: 0 }}
        overlayStyle={{ zIndex: 999 }}
      >
        <CustomAvatar
          name={user?.name}
          src={user?.avatarUrl}
          size="default"
          style={{ cursor: "pointer" }}
        />
      </Popover>
      {user && (
        <AccountSettings
          opened={opened}
          setOpened={setOpened}
          userId={user.id}
        />
      )}
    </>
  );
};
