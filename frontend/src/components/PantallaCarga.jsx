import { Flex, Spin } from 'antd';

export default function PantallaCarga() {
  return (
    <Flex justify="center" align="center" style={{ minHeight: '100vh' }}>
      <Spin size="large" />
    </Flex>
  );
}
