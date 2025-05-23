import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
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
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
};

export default App;