import { useState } from 'react';
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
import ImportData from './components/ImportData.jsx';
import Modal from './components/Modal.jsx'; 
import './components/Home.css';

function App() {
  const [page, setPage] = useState('login');
  const [modalMessage, setModalMessage] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [user, setUser] = useState(null);

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
          <Home onSwitchPage={setPage} user={user} />
        ) : page === 'set-price' ? (
          <SetPrice onSwitchPage={setPage} />
        ) : page === 'add-product' ? (
          <AddProduct onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'modify-product' ? (
          <ModifyProduct onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'batch-update' ? (
          <BatchUpdate onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'view-product' ? (
          <ViewProduct onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'remove-product' ? (
          <RemoveProduct onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'load-product' ? (
          <LoadProduct onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'verify-product' ? (
          <VerifyProducts onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'get-estimate' ? (
          <GetEstimate onSwitchPage={setPage} onOpenModal={openModal} />
        ) : page === 'enquiry-log' ? (
          <EnquiryLog onSwitchPage={setPage} />
        ) : page === 'estimate-snapshot' ? (
          <EstimateSnapshot onSwitchPage={setPage} />
        ) : page === 'product-snapshot' ? (
          <ProductSnapshot onSwitchPage={setPage} />
        ) : page === 'sales-log' ? (
          <SalesLog onSwitchPage={setPage} />
        ) : page === 'generate-report' ? (
          <GenerateReport onSwitchPage={setPage} />
        ) : page === 'import-data' ? (
          <ImportData onSwitchPage={setPage} />
        ) : (
          <main className="home-main">
            {page === 'login' && (
              <Login onSwitchPage={setPage} onOpenModal={openModal} onLoginSuccess={setUser} />
            )}
            {page === 'signup' && (
              <Signup onSwitchPage={setPage} onOpenModal={openModal} />
            )}
            {page === 'forgot' && (
              <ForgotPassword onSwitchPage={setPage} onOpenModal={openModal} />
            )}
            {page === 'reset' && (
              <ResetPassword onSwitchPage={setPage} onOpenModal={openModal} />
            )}
            {page === 'view-rates' && (
              <ViewRates onSwitchPage={setPage} />
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
