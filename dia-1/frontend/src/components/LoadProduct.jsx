import React, { useState, useEffect } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function LoadProduct({ onSwitchPage, onOpenModal }) {
  const [product, setProduct] = useState(null);
  const [includeAddons, setIncludeAddons] = useState(true);
  const [showVerifyModal, setShowVerifyModal] = useState(false);
  const [customerName, setCustomerName] = useState('');

  useEffect(() => {
    const productId = sessionStorage.getItem('productId');
    if (!productId) {
      onOpenModal('No product selected.');
      onSwitchPage('view-product');
      return;
    }
    fetchProduct(productId);
  }, []);

  const fetchProduct = async (id) => {
    try {
      const res = await fetch(`${API_BASE}/loadProductDetail/${id}`, { credentials: 'include' });
      if (!res.ok) throw new Error('Product not found');
      setProduct(await res.json());
    } catch (err) {
      onOpenModal(err.message);
    }
  };

  const handleAction = async (endpoint, method = 'POST') => {
    if (!customerName && (endpoint === 'logSale' || endpoint === 'logEnquiry')) {
      alert('Please enter a customer name.');
      return;
    }

    try {
      const payload = {
        productId: product.productId,
        designNo: product.designNo,
        customerName: customerName || 'System Admin',
        imageUrl: product.imageUrl,
        price: product.price,
        priceWithFields: product.priceWithFields,
        estimateSnapshot: { total: product.priceWithFields || product.price }
      };

      const url = endpoint === 'verify' ? `${API_BASE}/verify/${encodeURIComponent(product.designNo)}` : `${API_BASE}/${endpoint}`;
      const body = endpoint === 'verify' ? { verificationStatus: 1 } : payload;

      const res = await fetch(url, {
        method: endpoint === 'verify' ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
        credentials: 'include'
      });

      if (res.ok) {
        onOpenModal(`Action ${endpoint} successful!`);
        if (endpoint !== 'logEnquiry') onSwitchPage('view-product');
      }
    } catch (err) {
      onOpenModal('Action failed.');
    }
  };

  if (!product) return <div className="home-main">Loading details...</div>;

  const currentPrice = includeAddons ? (product.priceWithFields || product.price) : product.price;
  const extras = product.customFields ? JSON.parse(product.customFields) : {};

  return (
    <>
      <header className="header">
        <div className="logo">Product <span>Manager</span></div>
        <button className="logout-btn" onClick={() => onSwitchPage('login')}>Logout</button>
      </header>

      <main className="home-main" style={{ maxWidth: '800px', textAlign: 'left' }}>
        <button className="action-button secondary" onClick={() => onSwitchPage('view-product')} style={{ marginBottom: '20px' }}>
          ← Back to List
        </button>

        <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap', marginBottom: '30px' }}>
          <img src={product.imageUrl?.startsWith('/') ? `${API_BASE}${product.imageUrl}` : product.imageUrl} 
               alt={product.item} style={{ width: '250px', borderRadius: '12px', border: '1px solid #ddd' }} />
          <div>
            <img src={product.qrCodePath?.startsWith('/') ? `${API_BASE}${product.qrCodePath}` : product.qrCodePath} 
                 alt="QR" style={{ width: '150px', border: '1px dashed #ccc' }} />
            <div style={{ marginTop: '15px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <button className="action-button" onClick={() => setShowVerifyModal(true)}>Verify / Actions</button>
              <button className="action-button secondary" onClick={() => onSwitchPage('get-estimate')}>📊 Get Estimate</button>
            </div>
          </div>
        </div>

        <div className="product-details">
          <h1 style={{ textAlign: 'left', margin: 0 }}>{product.item}</h1>
          <p style={{ color: product.verificationStatus === 1 ? '#28a745' : '#dc3545', fontWeight: 'bold' }}>
            {product.verificationStatus === 1 ? '✔ Verified' : '✘ Unverified'}
          </p>
          <p><strong>Design No:</strong> {product.orders?.orderId || 'N/A'}/{product.designNo}</p>
          <p><strong>Gold:</strong> {product.karat} Karat</p>
          
          <div style={{ background: '#f8f9fa', padding: '15px', borderRadius: '8px', margin: '20px 0' }}>
            <label className="checkbox-item" style={{ color: '#000' }}>
              <input type="checkbox" checked={includeAddons} onChange={e => setIncludeAddons(e.target.checked)} />
              Include Addons in Price
            </label>
            <p style={{ fontSize: '1.8rem', fontWeight: '800', color: '#b8860b', margin: '10px 0' }}>
              ₹{Math.round(currentPrice).toLocaleString('en-IN')}
            </p>
          </div>

          <h3>Technical Specs</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <p><strong>Gross Weight:</strong> {Number(product.gross || 0).toFixed(3)} gm</p>
            <p><strong>Net Weight:</strong> {Number(product.net || 0).toFixed(3)} gm</p>
            {product.diamondsCt > 0 && <p><strong>Diamonds:</strong> {product.diamondsCt} ct</p>}
            {product.vilandiCt > 0 && <p><strong>Vilandi:</strong> {product.vilandiCt} ct</p>}
          </div>

          {Object.keys(extras).length > 0 && (
            <>
              <h3>Additional Fields</h3>
              <ul>
                {Object.entries(extras).map(([name, obj]) => (
                  <li key={name}><strong>{name}:</strong> {obj.qty}</li>
                ))}
              </ul>
            </>
          )}
          {product.remarks && <p><strong>Remarks:</strong> {product.remarks}</p>}
        </div>
      </main>

      {showVerifyModal && (
        <div className="popup-overlay">
          <div className="popup-content" style={{ maxWidth: '500px' }}>
            <h2>Product Actions</h2>
            <div className="auth-form" style={{ marginBottom: '20px' }}>
              <label>Customer Name *</label>
              <input type="text" value={customerName} onChange={e => setCustomerName(e.target.value)} placeholder="Required for Sale/Enquiry" />
            </div>
            <div className="popup-actions" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <button className="popup-button primary" onClick={() => handleAction('verify')}>Mark Verified</button>
              <button className="popup-button secondary" onClick={() => handleAction('logEnquiry')}>Log Enquiry</button>
              <button className="popup-button secondary" style={{ background: '#000' }} onClick={() => handleAction('logSale')}>Mark Sold</button>
              <button className="popup-button" style={{ background: '#ffc107', color: '#000' }} 
                      onClick={() => { sessionStorage.setItem('modifyDesignNo', product.designNo); onSwitchPage('modify-product'); }}>
                Modify
              </button>
            </div>
            <button className="close-btn" style={{ marginTop: '20px', position: 'static' }} onClick={() => setShowVerifyModal(false)}>Close</button>
          </div>
        </div>
      )}
    </>
  );
}

export default LoadProduct;