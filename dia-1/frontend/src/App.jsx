import { useState, useEffect } from 'react';
import Login from './components/Login.jsx';
import Signup from './components/Signup.jsx';
import ForgotPassword from './components/ForgotPassword.jsx';
import ResetPassword from './components/ResetPassword.jsx';
import ViewRates from './components/ViewRates.jsx';
import Home from './components/Home.jsx';
import SetPrice from './components/SetPrice.jsx';
import AddProduct from './components/AddProduct.jsx';
import ModifyProduct from './components/ModifyProduct.jsx';
import BatchUpdate from './components/BatchUpdate.jsx';
import ViewProduct from './components/ViewProduct.jsx';
import RemoveProduct from './components/RemoveProduct.jsx';
import LoadProduct from './components/LoadProduct.jsx';
import GetEstimate from './components/GetEstimate.jsx';
import VerifyProducts from './components/VerifyProducts.jsx';
import EnquiryLog from './components/EnquiryLog.jsx';
import SalesLog from './components/SalesLog.jsx';
import EstimateSnapshot from './components/EstimateSnapshot.jsx';
import ProductSnapshot from './components/ProductSnapshot.jsx';
import GenerateReport from './components/GenerateReport.jsx';
import CustomFoldableTags from './components/CustomFoldableTags.jsx';
import ImportData from './components/ImportData.jsx';
import Permissions from './components/Permissions.jsx';
import PriceHistory from './components/PriceHistory.jsx';
import Modal from './components/Modal.jsx'; 
import './components/Home.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function App() {
  const getInitialPage = () => {
    const path = window.location.pathname;
    
    // Check if it's the QR scan path (case-insensitive and optional s)
    const decodedPath = decodeURIComponent(path);
    const qrMatch = decodedPath.match(/^\/loadProducts?ByDesignNo\/([^/]+)/i);
    if (qrMatch) {
      const designNo = qrMatch[1];
      try {
        sessionStorage.setItem('productId', designNo);
        sessionStorage.setItem('isDesignNo', 'true');
      } catch (e) {
        console.warn('sessionStorage is not accessible:', e);
      }
      return 'load-product';
    }

    const cleanPath = path.replace(/^\/|\/$/g, '');
    if (!cleanPath || cleanPath === 'login') return 'login';
    if (cleanPath === 'custom-tags') return 'custom-foldable-tags';
    if (cleanPath === 'public-rates') return 'view-rates';
    return cleanPath;
  };

  const [page, setPage] = useState(getInitialPage);
  const [modalMessage, setModalMessage] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  
  // Safely initialize user from localStorage
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('user');
      if (stored) {
        return JSON.parse(stored);
      }
    } catch (e) {
      console.warn('localStorage is not accessible during initialization:', e);
    }
    return null;
  });

  useEffect(() => {
    const handlePopState = () => {
      setPage(getInitialPage());
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const handleSwitchPage = (newPage) => {
    setPage(newPage);
    let path = '/';
    if (newPage !== 'login') {
      if (newPage === 'custom-foldable-tags') {
        path = '/custom-tags';
      } else if (newPage === 'view-rates') {
        path = '/public-rates';
      } else {
        path = `/${newPage}`;
      }
    }
    window.history.pushState(null, '', path);
  };

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    try {
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (e) {
      console.warn('Failed to save user to localStorage:', e);
    }
  };

  const openModal = (message) => {
    setModalMessage(message);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setModalMessage('');
  };

  return (
    <div className="home-page">
      <div className="flex-grow">
        {page === 'home' ? (
          <Home onSwitchPage={handleSwitchPage} user={user} />
        ) : page === 'set-price' ? (
          <SetPrice onSwitchPage={handleSwitchPage} />
        ) : page === 'add-product' ? (
          <AddProduct onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'modify-product' ? (
          <ModifyProduct onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'batch-update' ? (
          <BatchUpdate onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'view-product' ? (
          <ViewProduct onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'remove-product' ? (
          <RemoveProduct onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'load-product' ? (
          <LoadProduct onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'verify-product' ? (
          <VerifyProducts onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'get-estimate' ? (
          <GetEstimate onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'enquiry-log' ? (
          <EnquiryLog onSwitchPage={handleSwitchPage} />
        ) : page === 'estimate-snapshot' ? (
          <EstimateSnapshot onSwitchPage={handleSwitchPage} />
        ) : page === 'product-snapshot' ? (
          <ProductSnapshot onSwitchPage={handleSwitchPage} />
        ) : page === 'sales-log' ? (
          <SalesLog onSwitchPage={handleSwitchPage} />
        ) : page === 'generate-report' ? (
          <GenerateReport onSwitchPage={handleSwitchPage} />
        ) : page === 'custom-foldable-tags' ? (
          <CustomFoldableTags onSwitchPage={handleSwitchPage} />
        ) : page === 'import-data' ? (
          <ImportData onSwitchPage={handleSwitchPage} />
        ) : page === 'permissions' ? (
          <Permissions onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
        ) : page === 'price-history' ? (
          <PriceHistory onSwitchPage={handleSwitchPage} />
        ) : (
          <main className="home-main">
            {page === 'login' && (
              <Login onSwitchPage={handleSwitchPage} onOpenModal={openModal} onLoginSuccess={handleLoginSuccess} />
            )}
            {page === 'signup' && (
              <Signup onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
            )}
            {page === 'forgot' && (
              <ForgotPassword onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
            )}
            {page === 'reset' && (
              <ResetPassword onSwitchPage={handleSwitchPage} onOpenModal={openModal} />
            )}
            {page === 'view-rates' && (
              <ViewRates onSwitchPage={handleSwitchPage} />
            )}
          </main>
        )}
      </div>

      <footer className="footer">
        <p>&copy; 2025 Jewellery Store Manager. All rights reserved.</p>
      </footer>

      <Modal isOpen={modalOpen} message={modalMessage} onClose={closeModal} />
    </div>
  );
}

export default App;
