import { AuthPage } from "@refinedev/antd";
import { ThemedTitleV2 } from "@refinedev/antd";

const authCredentials = {
  email: "firstuser@example.com",
  password: "123456",
};

export const LoginPage = () => {
  return (
    <AuthPage
      type="login"
      title={<ThemedTitleV2 collapsed={false} text="CRM Project" />}
      formProps={{
        initialValues: authCredentials,
      }}
      registerLink={false}
      forgotPasswordLink={false}
    />
  );
};