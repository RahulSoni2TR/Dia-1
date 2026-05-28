import React, { useEffect, useState } from 'react';
import './Modal.css';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function ProductSnapshot({ onSwitchPage }) {
  const [product, setProduct] = useState(null);
  const [returnPage, setReturnPage] = useState('enquiry-log');

  useEffect(() => {
    const storedProduct = sessionStorage.getItem('productFromLog');
    const storedReturnPage = sessionStorage.getItem('returnToPage');
    
    if (storedProduct) {
      try {
        const parsed = JSON.parse(storedProduct);
        setProduct(parsed);
      } catch (e) {
        console.error('Error parsing product:', e);
      }
    }
    
    if (storedReturnPage) {
      // Extract page name from URL if it's a full URL
      if (storedReturnPage.includes('enquiry')) {
        setReturnPage('enquiry-log');
      } else if (storedReturnPage.includes('sales')) {
        setReturnPage('sales-log');
      }
    }
  }, []);

  const handleBack = () => {
    sessionStorage.removeItem('productFromLog');
    sessionStorage.removeItem('returnToPage');
    onSwitchPage(returnPage);
  };

  if (!product) {
    return (
      <div className="modal-overlay">
        <div className="modal-content">
          <button className="modal-close-btn" onClick={handleBack}>&times;</button>
          <h2>Product Not Found</h2>
          <p>No product data available.</p>
          <button className="action-button secondary" onClick={handleBack}>
            Back
          </button>
        </div>
      </div>
    );
  }

  const productImageUrl = product.imageUrl?.startsWith('/') 
    ? `${API_BASE}${product.imageUrl}` 
    : product.imageUrl;

  return (
    <div className="modal-overlay">
      <div className="estimate-snapshot-modal-content">
        <button className="modal-close-btn" onClick={handleBack}>&times;</button>
        
        <div style={{ padding: '20px' }}>
          <h2 style={{ marginBottom: '20px', borderBottom: '2px solid #ddd', paddingBottom: '10px' }}>
            Product Snapshot
          </h2>

          <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '30px', marginBottom: '20px' }}>
            {/* Image Section */}
            <div>
              {productImageUrl ? (
                <img 
                  src={productImageUrl} 
                  alt={product.item}
                  style={{ width: '100%', maxHeight: '400px', objectFit: 'cover', borderRadius: '8px', border: '1px solid #ddd' }}
                />
              ) : (
                <div style={{ 
                  width: '100%', 
                  height: '300px', 
                  backgroundColor: '#f0f0f0', 
                  display: 'flex', 
                  alignItems: 'center',
                  justifyContent: 'center',
                  borderRadius: '8px',
                  border: '1px solid #ddd'
                }}>
                  <span style={{ color: '#999' }}>No Image Available</span>
                </div>
              )}
            </div>

            {/* Details Section */}
            <div>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <tbody>
                  <tr>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold', width: '35%' }}>Product Name:</td>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.item || '-'}</td>
                  </tr>
                  <tr>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Design No:</td>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.designNo || '-'}</td>
                  </tr>
                  <tr>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Category:</td>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.categoryId || '-'}</td>
                  </tr>
                  <tr>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Gross Weight:</td>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.gross ? `${Number(product.gross).toFixed(3)} gm` : '-'}</td>
                  </tr>
                  <tr>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Net Weight:</td>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.net ? `${Number(product.net).toFixed(3)} gm` : '-'}</td>
                  </tr>
                  <tr>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Purity:</td>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.karat ? `${product.karat} KT` : '-'}</td>
                  </tr>
                  {product.diamondsCt > 0 && (
                    <tr>
                      <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Diamonds:</td>
                      <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.diamondsCt} ct</td>
                    </tr>
                  )}
                  {product.vilandiCt > 0 && (
                    <tr>
                      <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Vilandi:</td>
                      <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>{product.vilandiCt} ct</td>
                    </tr>
                  )}
                  <tr>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Price:</td>
                    <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold', color: '#27ae60' }}>
                      ₹{product.priceWithFields ? Math.round(product.priceWithFields).toLocaleString('en-IN') : Math.round(product.price || 0).toLocaleString('en-IN')}
                    </td>
                  </tr>
                  {product.createdAt && (
                    <tr>
                      <td style={{ padding: '8px', borderBottom: '1px solid #eee', fontWeight: 'bold' }}>Created:</td>
                      <td style={{ padding: '8px', borderBottom: '1px solid #eee' }}>
                        {new Date(product.createdAt).toLocaleString()}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Description */}
          {product.remarks && (
            <div style={{ marginBottom: '20px', marginTop: '30px' }}>
              <h3>Remarks</h3>
              <div style={{ padding: '10px', backgroundColor: '#f9f9f9', border: '1px solid #e0e0e0', borderRadius: '4px' }}>
                {product.remarks}
              </div>
            </div>
          )}

          {/* Back Button */}
          <div style={{ marginTop: '30px', textAlign: 'center' }}>
            <button className="action-button secondary" onClick={handleBack}>
              <i className="fas fa-arrow-left"></i> Back to Log
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductSnapshot;
