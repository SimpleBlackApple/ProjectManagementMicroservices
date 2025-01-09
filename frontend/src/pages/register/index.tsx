// src/pages/register.tsx
import { Typography, Input, Form, Button, message } from "antd";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import './index.less';

const { Link, Title } = Typography;

interface RegisterFormValues {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export const RegisterPage = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const handleSubmit = async (values: RegisterFormValues) => {
    try {
      const { confirmPassword, ...registerData } = values;
      await axios.post('http://localhost:8081/api/auth/signup', registerData);
      message.success('Registration successful! Please login.');
      navigate('/login');
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Registration failed. Please try again.');
    }
  };
// src/pages/register.tsx

  
    return (
      <div className="register-container">
        <div className="register-card">
          <Title level={2} className="register-title">
            Sign up for your account
          </Title>
  
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
          >
            <Form.Item
              name="name"
              label="Name"
              rules={[
                { required: true, message: 'Please input your name!' }
              ]}
            >
              <Input />
            </Form.Item>
  
            <Form.Item
              name="email"
              label="Email"
              rules={[
                { required: true, message: 'Please input your email!' },
                { type: 'email', message: 'Please enter a valid email!' }
              ]}
            >
              <Input />
            </Form.Item>
  
            <Form.Item
              name="password"
              label="Password"
              rules={[
                { required: true, message: 'Please input your password!' },
                { min: 8, message: 'Password must be at least 8 characters!' },
                {
                  pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
                  message: 'Password must contain uppercase, lowercase letters and numbers!'
                }
              ]}
              extra="Password must be at least 8 characters and contain uppercase, lowercase letters and numbers"
            >
              <Input.Password />
            </Form.Item>
  
            <Form.Item
              name="confirmPassword"
              label="Confirm Password"
              dependencies={['password']}
              rules={[
                { required: true, message: 'Please confirm your password!' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('The two passwords do not match!'));
                  },
                }),
              ]}
            >
              <Input.Password />
            </Form.Item>
  
            <Form.Item className="form-item-password">
              <Button type="primary" htmlType="submit" block>
                Sign up
              </Button>
            </Form.Item>
          </Form>
  
          <div className="signin-link">
            Already have an account? <Link onClick={() => navigate("/login")}>Sign in</Link>
          </div>
        </div>
      </div>
    );
  };