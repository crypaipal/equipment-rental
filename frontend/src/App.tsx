import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { AuthLayout } from './components/AuthLayout'
import { Layout } from './components/Layout'
import { GuestRoute } from './auth/GuestRoute'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { RoleRoute } from './auth/RoleRoute'
import { DashboardPage } from './pages/DashboardPage'
import { EquipmentCatalogPage } from './pages/EquipmentCatalogPage'
import { InventoryAdminPage } from './pages/InventoryAdminPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { RentalsPage } from './pages/RentalsPage'
import { ReservationsPage } from './pages/ReservationsPage'
import { MyReservationsPage } from './pages/MyReservationsPage'

const router = createBrowserRouter([
  {
    element: <GuestRoute />,
    children: [
      {
        element: <AuthLayout />,
        children: [
          { path: '/login', element: <LoginPage /> },
          { path: '/register', element: <RegisterPage /> },
        ],
      },
    ],
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <Layout />,
        children: [
          { index: true, element: <DashboardPage /> },
          { path: '/catalog', element: <EquipmentCatalogPage /> },
          {
            element: <RoleRoute allowedRoles={['BORROWER']} />,
            children: [
              { path: '/my-reservations', element: <MyReservationsPage /> },
            ],
          },
          {
            element: (
                <RoleRoute allowedRoles={['LAB_ASSISTANT', 'SYSTEM_ADMIN']} />
            ),
            children: [
              { path: '/inventory', element: <InventoryAdminPage /> },
              { path: '/reservations', element: <ReservationsPage /> },
              { path: '/rentals', element: <RentalsPage /> },
            ],
          },
        ],
      },
    ],
  },
])

export function App() {
  return <RouterProvider router={router} />
}