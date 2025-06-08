import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ProductList from './components/ProductList';
import Header from './components/Header';
import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignUpPage';
import VerifyPage from './pages/VerifyPage';
import { AuthProvider } from './context/AuthContext';
import CartPage from './pages/CartPage';
import AddressPage from './pages/AddressPage';
import OrderPage from './pages/OrderPage';
import OrdersPage from './pages/OrdersPage';
import OrderDetailsPage from './pages/OrderDetailsPage';
import WishlistPage from './pages/WishlistPage'
import AdminRoute from './components/AdminRoute';
import AdminDashboard from './pages/AdminDashboard';
import ProductDetailPage from './pages/ProductDetailPage';
import AdminLayout from './components/AdminLayout';
import AdminDashboardAnalytics from './pages/AdminDashboardAnalytics';
import AdminOrdersPage from './pages/admin/AdminOrdersPage';
import OrderDetailPage from './pages/admin/OrderDetailPage';
import PaymentDetailsPage from './pages/PaymentDetailsPage'
import TransactionsPage from './pages/TransactionsPage';

const App: React.FC = () => {
  return (
    <AuthProvider>
      <Router>
        <div>
          <Header />
          <Routes>
            <Route path="/" element={<ProductList />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignUpPage />} />
            <Route path="/verify" element={<VerifyPage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/address" element={<AddressPage />} />
            <Route path="/order" element={<OrderPage />} />
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/orders/:orderId" element={<OrderDetailsPage />} />
            <Route path="/wishlist" element={<WishlistPage />} />
            <Route path="/products/:id" element={<ProductDetailPage />} />
            <Route path="/admin/orders" element={<AdminOrdersPage />} />
            <Route path="/admin/orders/:orderId" element={<OrderDetailPage />} />
            <Route path="/payments/:paymentId" element={<PaymentDetailsPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/admin" element={<AdminLayout />}>
                      <Route path="dashboard" element={<AdminDashboard />} />
                      <Route path="analytics" element={<AdminDashboardAnalytics />} />
                      {/* Redirect /admin to /admin/dashboard by default */}
                      <Route index element={<Navigate to="dashboard" replace />} />
                    </Route>
            <Route path="/admin/*"
              element={
                <AdminRoute>
                  <AdminDashboard />
                </AdminRoute>
              }
            />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
};

export default App;