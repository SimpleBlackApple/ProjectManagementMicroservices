import React from "react";
import { ThemedLayoutV2, ThemedTitleV2, ThemedSiderV2  } from "@refinedev/antd";

import { Header } from "../header";

export const ProjectLayout = ({ children }: React.PropsWithChildren) => {
  return (
    <>
      <ThemedLayoutV2
        Sider={() => null}
        Header={Header}
        Title={(titleProps) => {
          return <ThemedTitleV2 {...titleProps} text="Scrum" />;
        }}
      >
        {children}
      </ThemedLayoutV2>
    </>
  );
};
