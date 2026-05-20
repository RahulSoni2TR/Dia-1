import React, { useState } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function RemoveProduct({ onSwitchPage, onOpenModal }) {
  const [designNo, setDesignNo] = useState('');
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(false);
  const [categoryNames, setCategoryNames] = useState({ category: '', subCategory: '' });

  const fetchCategoryName = async (id) => {
    if (!id) return 'Unknown';
    try {
      const res = await fetch(`${API_BASE}/${id}/name`, { credentials: 'include' });
      if (res.ok) return await res.text();
      return 'Unknown';
    } catch (error) {
      return 'Unknown';
    }
  };

  const handleLoadProduct = async () => {
    if (!designNo) return onOpenModal('Please enter a Design No.');
    setLoading(true);
    setProduct(null);
    try {
      const res = await fetch(`${API_BASE}/loadProduct/${encodeURIComponent(designNo)}`, { credentials: 'include' });
      if (!res.ok) throw new Error('Product not found.');
      const data = await res.json();
      setProduct(data);
      
      const [catName, subCatName] = await Promise.all([
        fetchCategoryName(data.parentCategoryId),
        fetchCategoryName(data.subCategoryId)
      ]);
      setCategoryNames({ category: catName, subCategory: subCatName });
    } catch (error) {
      onOpenModal(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (e) => {
    e.preventDefault();
    if (!product) return;

    try {
      const res = await fetch(`${API_BASE}/remove/${encodeURIComponent(product.designNo)}`, {
        method: 'POST',
        credentials: 'include'
      });
      const data = await res.json();
      if (res.ok && data.success) {
        onOpenModal('Product removed successfully!');
        setProduct(null);
        setDesignNo('');
      } else {
        onOpenModal(data.message || 'Error removing product.');
      }
    } catch (error) {
      onOpenModal('Network error. Please try again.');
    }
  };

  return (
    <>
      <header className="header">
        <div className="logo">Product <span>Manager</span></div>
        <div className="logout-container">
          <button className="logout-btn" onClick={() => onSwitchPage('login')}>Logout</button>
        </div>
      </header>

      <main className="home-main">
        <h1>Remove Product</h1>
        <div className="auth-form">
          <label>Enter Design No. to Remove:</label>
          <div style={{ display: 'flex', gap: '10px' }}>
            <input 
              type="text" 
              value={designNo} 
              onChange={(e) => setDesignNo(e.target.value)} 
              placeholder="e.g. DES-1234"
              onKeyPress={(e) => e.key === 'Enter' && handleLoadProduct()}
            />
            <button type="button" className="action-button" onClick={handleLoadProduct} disabled={loading} style={{ flexShrink: 0, padding: '0 20px' }}>
              <i className="fas fa-search"></i> Load
            </button>
          </div>

          {product && (
            <div className="category-specific" style={{ textAlign: 'left', background: '#fdfdfd', padding: '20px', borderRadius: '8px', border: '1px solid #eee' }}>
              <p><strong>Product Name:</strong> {product.item}</p>
              <p><strong>Gross:</strong> {Number(product.gross ?? 0).toFixed(3)} gm</p>
              <p><strong>Net:</strong> {Number(product.net ?? 0).toFixed(3)} gm</p>
              <p><strong>Category:</strong> {categoryNames.category}</p>
              <p><strong>Sub Category:</strong> {categoryNames.subCategory}</p>
              <p><strong>Create Date:</strong> {product.createDateTime}</p>
              {product.imageUrl && (
                <div style={{ textAlign: 'center', marginTop: '15px' }}>
                  <img src={product.imageUrl.startsWith('/') ? `${API_BASE}${product.imageUrl}` : product.imageUrl} 
                       alt="Product" 
                       style={{ maxWidth: '160px', borderRadius: '8px', border: '1px solid #ddd' }} />
                </div>
              )}
              
              <button type="button" className="action-button" onClick={handleRemove} style={{ width: '100%', marginTop: '20px', background: '#dc3545' }}>
                <i className="fas fa-trash"></i> Remove Product
              </button>
            </div>
          )}

          <button type="button" className="action-button secondary" onClick={() => onSwitchPage('home')} style={{ marginTop: '10px' }}>
            <i className="fas fa-arrow-left"></i> Back to Dashboard
          </button>
        </div>
      </main>
    </>
  );
}

export default RemoveProduct;