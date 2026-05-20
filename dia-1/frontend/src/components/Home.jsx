import { useState, useEffect } from 'react';
import './Home.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function Home({ onSwitchPage, user }) {
  console.log('Home component rendering', user);
  const [prices, setPrices] = useState({});
  const [showPopup, setShowPopup] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPrices();
    checkPricePopup();
  }, []);

  const fetchPrices = async () => {
    try {
      const response = await fetch(`${API_BASE}/rates`, { credentials: 'include' });
      if (!response.ok) throw new Error('Failed to fetch rates');
      const data = await response.json();
      const ratesData = Array.isArray(data) ? data : (data.rates || []);

      const pricesObj = {
        gold_10k: getPriceForCommodity(ratesData, '10.00'),
        gold_14k: getPriceForCommodity(ratesData, '14.00'),
        gold_18k: getPriceForCommodity(ratesData, '18.00'),
        gold_22k: getPriceForCommodity(ratesData, '22.00'),
        gold_24k: getPriceForCommodity(ratesData, '24.00'),
        silver: getPriceForCommodity(ratesData, 'silver'),
        diamond: getPriceForCommodity(ratesData, 'diamond'),
        gst: getPriceForCommodity(ratesData, 'gst')
      };
      setPrices(pricesObj);
    } catch (error) {
      console.error('Error fetching prices:', error);
      setPrices({});
    } finally {
      setLoading(false);
    }
  };

  const getPriceForCommodity = (data, commodity) => {
    const rate = data.find(item => item.commodity === commodity);
    return rate ? rate.price : 'N/A';
  };

  const checkPricePopup = () => {
    const lastDismissed = localStorage.getItem("pricePopupDismissed");
    const now = new Date();
    const lastDismissedDate = lastDismissed ? new Date(lastDismissed) : null;

    if (!lastDismissedDate || now.getDate() !== lastDismissedDate.getDate()) {
      setShowPopup(true);
    } else {
      const hoursSinceDismissed = (now - lastDismissedDate) / (1000 * 60 * 60);
      if (hoursSinceDismissed >= 6) {
        setShowPopup(true);
      }
    }
  };

  const closePopup = () => setShowPopup(false);

  const redirectToSetPrices = () => {
    onSwitchPage('set-price');
  };

  const dismissForToday = () => {
    const now = new Date();
    localStorage.setItem("pricePopupDismissed", now);
    setShowPopup(false);
  };

  const handleLogout = () => {
    // Handle logout logic, perhaps clear localStorage and switch to login
    localStorage.removeItem('user');
    onSwitchPage('login');
  };

  const hasRole = (role) => {
    return user && user.roles && user.roles.includes(role);
  };

  const priceLabel = loading
    ? "Loading prices..."
    : `Gold 10K: ₹${prices.gold_10k} | Gold 14K: ₹${prices.gold_14k} | Gold 18K: ₹${prices.gold_18k} | Gold 22K: ₹${prices.gold_22k} | Gold 24K: ₹${prices.gold_24k} | Silver: ₹${prices.silver} | Diamond: ₹${prices.diamond} | GST: ${prices.gst}%`;

  return (
    <>
      {/* Header */}
      <header className="header">
        <div className="logo">Product <span>Manager</span></div>
        <div className="logout-container">
          <div className="dropdown">
            <button className="dropbtn">
              <i className="fas fa-user-circle"></i> User Options
            </button>
            <div className="dropdown-content">
              {hasRole('ROLE_ADMIN') && (
                <button type="button" className="dropdown-item" onClick={() => onSwitchPage('permissions')}>
                  <i className="fas fa-key"></i> User Permissions
                </button>
              )}
              <button type="button" className="dropdown-item" onClick={handleLogout}>
                <i className="fas fa-sign-out-alt"></i> Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Scrolling Prices Header */}
      <div className="scrolling-prices-container">
        <div className="scrolling-prices">
          <span className="scrolling-text">{priceLabel}</span>
        </div>
      </div>

      {/* Price Reminder Popup */}
      {showPopup && (
        <div className="popup-overlay">
          <div className="popup-content">
            <button className="popup-close-btn" onClick={closePopup}>&times;</button>
            <div className="popup-header">
              <h2>Set Prices Reminder</h2>
            </div>
            <div className="popup-body">
              <p>Have you updated the prices today?</p>
            </div>
            <div className="popup-actions">
              <button className="popup-button primary" onClick={redirectToSetPrices}>Set Prices Now</button>
              <button className="popup-button secondary" onClick={dismissForToday}>Already Updated</button>
            </div>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="home-main">
        <h1>Welcome, <span>{user ? user.username : 'Guest'}</span>!</h1>
        <p className="subtitle">Your one-stop solution for managing all product operations</p>
        {/* Action Buttons */}
        <div className="button-container">
          {(hasRole('ROLE_EDITOR') || hasRole('ROLE_ADMIN')) && (
            <button className="action-button" onClick={() => onSwitchPage('set-price')}>
              <i className="fas fa-dollar-sign"></i> Set Prices
            </button>
          )}
          {(hasRole('ROLE_EDITOR') || hasRole('ROLE_ADMIN') || hasRole('ROLE_VIEWER')) && (
            <button className="action-button" onClick={() => onSwitchPage('view-product')}>
              <i className="fas fa-eye"></i> View Products
            </button>
          )}
          {hasRole('ROLE_ADMIN') && (
            <button className="action-button" onClick={() => onSwitchPage('add-product')}>
              <i className="fas fa-plus"></i> Add Product
            </button>
          )}
          {(hasRole('ROLE_EDITOR') || hasRole('ROLE_ADMIN')) && (
            <button className="action-button" onClick={() => onSwitchPage('modify-product')}>
              <i className="fas fa-edit"></i> Modify Product
            </button>
          )}
          {hasRole('ROLE_ADMIN') && (
            <button className="action-button" onClick={() => onSwitchPage('remove-product')}>
              <i className="fas fa-trash"></i> Remove Product
            </button>
          )}
          {(hasRole('ROLE_EDITOR') || hasRole('ROLE_ADMIN') || hasRole('ROLE_VIEWER')) && (
            <button className="action-button" onClick={() => onSwitchPage('enquiry-log')}>
              <i className="fas fa-clipboard-list"></i> Product Enquiry
            </button>
          )}
          {(hasRole('ROLE_EDITOR') || hasRole('ROLE_ADMIN') || hasRole('ROLE_VIEWER')) && (
            <button className="action-button" onClick={() => onSwitchPage('verify-product')}>
              <i className="fas fa-check-circle"></i> Verify Products
            </button>
          )}
          {(hasRole('ROLE_EDITOR') || hasRole('ROLE_ADMIN') || hasRole('ROLE_VIEWER')) && (
            <button className="action-button" onClick={() => onSwitchPage('sales-log')}>
              <i className="fas fa-box-open"></i> Sold Products
            </button>
          )}
          {(hasRole('ROLE_EDITOR') || hasRole('ROLE_ADMIN')) && (
            <button className="action-button" onClick={() => onSwitchPage('generate-report')}>
              <i className="fas fa-file-alt"></i> Generate Report
            </button>
          )}
          {hasRole('ROLE_ADMIN') && (
            <button className="action-button" onClick={() => onSwitchPage('import-data')}>
              <i className="fas fa-file-import"></i> Import Data
            </button>
          )}
        </div>
      </main>
    </>
  );
}

export default Home;