// App.jsx
import { AppProvider } from './userapp';
import UserDashboard from './userdashboard';

export default function AppUserProvider() {
  return (
    <AppProvider>
      <UserDashboard />
    </AppProvider>
  );
}