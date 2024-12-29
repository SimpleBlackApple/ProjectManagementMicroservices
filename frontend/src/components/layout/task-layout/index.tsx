import React from "react";
import { ThemedLayoutV2, ThemedSiderV2, ThemedTitleV2 } from "@refinedev/antd";
import { Header } from "../header";
import { useResource } from "@refinedev/core";
import { Menu } from "antd";
import { UnorderedListOutlined } from "@ant-design/icons";
import { useTranslate, useRouterContext, useRouterType, useLink } from "@refinedev/core";
import { useParams } from "react-router-dom";


export const CustomSider: React.FC = () => {
  const { resources } = useResource();
  const translate = useTranslate();
  const routerType = useRouterType();
  const { Link: LegacyLink } = useRouterContext();
  const NewLink = useLink();
  const Link = routerType === "legacy" ? LegacyLink : NewLink;
  const { id } = useParams(); // 获取当前 URL 中的项目 ID

  return (
    <ThemedSiderV2
      // Title={(titleProps) => <div>Scrum</div>}
      Title={(titleProps) => {
        return <ThemedTitleV2 {...titleProps} text="Scrum" />;
      }}
      render={({ logout }) => {
        // 过滤出内层资源（parent 为 projects 的资源）
        const innerResources = resources.filter(
          (item) => item.meta?.parent === "projects"
        );

        // 将资源转换为菜单项，替换 URL 中的 :id 参数
        const menuItems = innerResources.map((item) => {
          // 确保 list 是字符串类型
          const listPath = typeof item.list === 'string' ? item.list : '';
          const route = id ? listPath.replace(':id', id) : listPath;
          
          return (
            <Menu.Item
              key={item.name}
              icon={item.meta?.icon ?? <UnorderedListOutlined />}
            >
              <Link to={route ?? ""}>
                {translate(`${item.name}.${item.name}`, item.name)}
              </Link>
            </Menu.Item>
          );
        });

        return (
          <Menu
            selectedKeys={[window.location.pathname]}
            mode="inline"
            style={{
              paddingTop: "8px",
              border: "none",
              overflow: "auto",
              height: "calc(100% - 72px)",
            }}
          >
            {menuItems}
            {logout}
          </Menu>
        );
      }}
    />
  );
};

export const TaskLayout = ({ children }: React.PropsWithChildren) => {


  const { resources } = useResource();
  console.log(resources);
  return (
    <>
      <ThemedLayoutV2
        Header={Header}
        // Title={(titleProps) => {
        //   return <ThemedTitleV2 {...titleProps} text="Scrum" />;
        // }}
        Sider={CustomSider}
      >
        {children}
      </ThemedLayoutV2>
    </>
  );
};
