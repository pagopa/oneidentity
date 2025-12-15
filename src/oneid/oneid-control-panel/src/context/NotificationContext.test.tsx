import { render, screen, act } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { NotificationProvider, useNotification } from './NotificationContext';

const TestComponent = () => {
  const { showNotification } = useNotification();

  return (
    <div>
      <button onClick={() => showNotification('Success message', 'success')}>
        Show Success
      </button>
      <button onClick={() => showNotification('Error message', 'error')}>
        Show Error
      </button>
    </div>
  );
};

describe('NotificationContext', () => {
  it('should provide notification context', () => {
    render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    expect(screen.getByText('Show Success')).toBeInTheDocument();
    expect(screen.getByText('Show Error')).toBeInTheDocument();
  });

  it('should show success notification', () => {
    render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    const button = screen.getByText('Show Success');
    act(() => {
      button.click();
    });

    expect(screen.getByText('Success message')).toBeInTheDocument();
  });

  it('should show error notification', () => {
    render(
      <NotificationProvider>
        <TestComponent />
      </NotificationProvider>
    );

    const button = screen.getByText('Show Error');
    act(() => {
      button.click();
    });

    expect(screen.getByText('Error message')).toBeInTheDocument();
  });

  it('should throw error when used outside provider', () => {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => {
      render(<TestComponent />);
    }).toThrow('useNotification must be used within a NotificationProvider');

    consoleSpy.mockRestore();
  });
});
