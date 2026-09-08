import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from './App';

test('renders navigation bar', () => {
  render(
    <MemoryRouter>
      <App />
    </MemoryRouter>
  );
  expect(screen.getByText('庫存監控系統')).toBeInTheDocument();
  expect(screen.getByText('儀表板')).toBeInTheDocument();
  expect(screen.getByText('產品管理')).toBeInTheDocument();
  expect(screen.getByText('低庫存警示')).toBeInTheDocument();
});
