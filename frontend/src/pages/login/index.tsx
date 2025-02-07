import { AuthPage } from "@refinedev/antd";
import { ThemedTitleV2 } from "@refinedev/antd";
import { Typography, Space } from "antd";
import { useNavigate } from "react-router-dom";

const { Text, Link } = Typography;

const authCredentials = {
  email: "engineer@outlook.com",
  password: "Aa123456",
};

export const LoginPage = () => {
  const navigate = useNavigate();

  return (
    <AuthPage
      type="login"
      title={<ThemedTitleV2 collapsed={false} text="CRM Project" />}
      formProps={{
        initialValues: authCredentials,
      }}
      registerLink={false}
      forgotPasswordLink={false}
      renderContent={(content: React.ReactNode) => {
        return (
          <div>
            {content}
            <div style={{
              marginTop: "24px",
              textAlign: "center",
              display: "flex",
              justifyContent: "center",
              gap: "4px"
            }}>
              <Text>New user?</Text>
              <Link onClick={() => navigate("/register")}>
                Create an account
              </Link>
            </div>
          </div>
        );
      }}
    />
  );
};
