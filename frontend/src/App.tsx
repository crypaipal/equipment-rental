import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { Layout } from './components/Layout'
import { DashboardPage } from './pages/DashboardPage'
import { EquipmentCatalogPage } from './pages/EquipmentCatalogPage'
import { InventoryAdminPage } from './pages/InventoryAdminPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { RentalsPage } from './pages/RentalsPage'
import { ReservationsPage } from './pages/ReservationsPage'

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'catalog', element: <EquipmentCatalogPage /> },
      { path: 'inventory', element: <InventoryAdminPage /> },
      { path: 'reservations', element: <ReservationsPage /> },
      { path: 'rentals', element: <RentalsPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
    ],
  },
])

export function App() {
  return <RouterProvider router={router} />
}