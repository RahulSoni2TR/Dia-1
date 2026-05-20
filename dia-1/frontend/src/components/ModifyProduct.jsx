import React, { useState, useEffect } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function ModifyProduct({ onSwitchPage, onOpenModal }) {
  const [categories, setCategories] = useState([]);
  const [subCategories, setSubCategories] = useState([]);
  const [availableOrderIds, setAvailableOrderIds] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [searchId, setSearchId] = useState('');
  const [productId, setProductId] = useState(''); // Current Design No used for the API path
  const [oldOrderId, setOldOrderId] = useState('');
  const [orderIdOption, setOrderIdOption] = useState('existing');
  const [imagePreview, setImagePreview] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [extraFields, setExtraFields] = useState([]);

  const getPreviewUrl = (url) => {
    if (!url) return '';
    if (import.meta.env.DEV && url.startsWith('/')) {
      return `${API_BASE}${url}`;
    }
    return url;
  };

  const [formData, setFormData] = useState({
    categoryId: '',
    subCategoryId: '',
    productName: '',
    karat: '',
    designNo: '',
    gross: '',
    productNet: '',
    availableOrderId: '',
    customOrderId: '',
    productRemarks: '',
    labour: '',
    labourAll: '',
    labourPer: '',
    // Dynamic fields
    pcs: '', diaWeight: '', diaRate: '', diaOs: '', diaOsRate: '',
    vilandiCt: '', vilandiRate: '', diamondsCt: '', diamondsCtRate: '', beadsCt: '', beadsRate: '', pearlsGm: '', openPearlsRate: '', ssosPearllbl: '', ssosPearlCt: '', otherStonesCt: '', otherOsRate: '',
    vilandi: '', vRate: '', stones: '', vsRate: '', beadsCtVilandi: '', vbRate: '', pearlsGmVilandi: '', vpRate: '', ssPearlCt: '', vssRate: '', vrealStone: '', vfitting: '', vmoz: '', vmRate: '',
    stonesJadtar: '', jsRate: '', beadsCtJadtar: '', jbRate: '', pearlsGmJadtar: '', jpRate: '', ssPearlCtJadtar: '', jssRate: '', realStoneJadtar: '', jfitting: '', jmoz: '', jmRate: '', jadvilandi: '', jadvilandiRate: ''
  });

  useEffect(() => {
    const init = async () => {
      const res = await fetch(`${API_BASE}/categories`, { credentials: 'include' });
      if (res.ok) setCategories(await res.json());

      const modifyDesignNo = sessionStorage.getItem('modifyDesignNo');
      if (modifyDesignNo) {
        setSearchId(modifyDesignNo);
        loadProduct(modifyDesignNo);
      }
    };
    init();
  }, []);

  const loadProduct = async (idToLoad) => {
    const id = idToLoad || searchId;
    if (!id) return onOpenModal('Please enter a Design No.');

    try {
      const res = await fetch(`${API_BASE}/loadProduct/${encodeURIComponent(id)}`, { credentials: 'include' });
      if (!res.ok) throw new Error('Product not found');
      const p = await res.json();

      setProductId(p.designNo);
      setOldOrderId(p.orders?.orderId || '');
      setImagePreview(p.imageUrl || '');
      
      // Map basic fields
      const mapped = {
        categoryId: p.categoryId || '',
        subCategoryId: p.subCategoryId || '',
        productName: p.item || '',
        karat: p.karat || '',
        designNo: p.designNo || '',
        gross: p.gross || '',
        productNet: p.net || '',
        productRemarks: p.remarks || '',
        labour: p.labour || '',
        labourAll: p.labourAll || '',
        labourPer: p.labourP || '',
      };

      // Map category specific fields
      if (p.categoryId === 1) {
        Object.assign(mapped, { pcs: p.pcs, diaWeight: p.diamondsCt, diaRate: p.diaRt, diaOs: p.otherStonesCt, diaOsRate: p.otherStonesRt });
      } else if (p.categoryId === 2) {
        Object.assign(mapped, { vilandiCt: p.vilandiCt, vilandiRate: p.vRate, diamondsCt: p.diamondsCt, diamondsCtRate: p.diaRt, beadsCt: p.beadsCt, beadsRate: p.bdRate, pearlsGm: p.pearlsGm, ssosPearllbl: p.ssPearlCt, ssosPearlCt: p.ssRate, openPearlsRate: p.prlRate, otherStonesCt: p.otherStonesCt, otherOsRate: p.otherStonesRt });
      } else if (p.categoryId === 4) {
        Object.assign(mapped, { vilandi: p.vilandiCt, vRate: p.vRate, stones: p.stones, vsRate: p.stRate, beadsCtVilandi: p.beadsCt, vbRate: p.bdRate, pearlsGmVilandi: p.pearlsGm, vpRate: p.prlRate, ssPearlCt: p.ssPearlCt, vssRate: p.ssRate, vrealStone: p.realStone, vfitting: p.fitting, vmoz: p.mozonite, vmRate: p.mRate });
      } else if (p.categoryId === 5) {
        Object.assign(mapped, { stonesJadtar: p.stones, beadsCtJadtar: p.beadsCt, pearlsGmJadtar: p.pearlsGm, ssPearlCtJadtar: p.ssPearlCt, realStoneJadtar: p.realStone, jsRate: p.stRate, jbRate: p.bdRate, jpRate: p.prlRate, jssRate: p.ssRate, jfitting: p.fitting, jmoz: p.mozonite, jmRate: p.mRate, jadvilandi: p.vilandiCt, jadvilandiRate: p.vRate });
      }

      setFormData(prev => ({ ...prev, ...mapped }));
      if (p.customFields) {
        const extras = JSON.parse(p.customFields);
        setExtraFields(Object.entries(extras).map(([name, obj]) => ({ name, qty: obj.qty, rate: obj.rate })));
      }

      const children = categories.filter(c => c.parentId === p.categoryId);
      setSubCategories(children);
      loadAvailableOrderIds(p.categoryId, p.subCategoryId);
      setShowForm(true);
    } catch (e) {
      onOpenModal(e.message);
    }
  };

  const loadAvailableOrderIds = async (cat, sub) => {
    if (!cat || !sub) return;
    const res = await fetch(`${API_BASE}/available-order-ids?categoryId=${cat}&categoryId=${sub}`, { credentials: 'include' });
    if (res.ok) setAvailableOrderIds(await res.json());
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const uploadImage = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    const finalOrderId = orderIdOption === 'existing' ? (formData.availableOrderId || oldOrderId) : formData.customOrderId;
    
    const payload = new FormData();
    payload.append('productOrderid', finalOrderId);
    Object.keys(formData).forEach(k => { if (formData[k] !== '') payload.append(k, formData[k]); });
    if (imageFile) payload.append('image', imageFile);

    const extras = {};
    extraFields.forEach(f => { if (f.name) extras[f.name] = { qty: f.qty, rate: f.rate }; });
    payload.append('customFields', JSON.stringify(extras));

    try {
      const res = await fetch(`${API_BASE}/updateProduct/${encodeURIComponent(productId)}`, {
        method: 'PUT',
        body: payload,
        credentials: 'include'
      });
      if (res.ok) {
        onOpenModal('Product updated successfully!');
        onSwitchPage('home');
      } else {
        onOpenModal('Update failed. Please check inputs.');
      }
    } catch (e) { onOpenModal('Network error'); }
  };

  const renderCategorySpecific = () => {
    const c = parseInt(formData.categoryId);
    if (c === 1) return (
      <div className="category-specific">
        <label>Pieces</label><input type="number" name="pcs" value={formData.pcs} onChange={handleInputChange} />
        <div className="inline-fields">
          <div><label>Diamonds (ct)</label><input type="number" name="diaWeight" value={formData.diaWeight} onChange={handleInputChange} /></div>
          <div><label>Diamond Rate</label><input type="number" name="diaRate" value={formData.diaRate} onChange={handleInputChange} /></div>
        </div>
        <div className="inline-fields">
          <div><label>Other Stones</label><input type="number" name="diaOs" value={formData.diaOs} onChange={handleInputChange} /></div>
          <div><label>Other Rate</label><input type="number" name="diaOsRate" value={formData.diaOsRate} onChange={handleInputChange} /></div>
        </div>
      </div>
    );
    if (c === 2) return (
      <div className="category-specific">
        <div className="inline-fields">
          <div><label>Vilandi CT</label><input type="number" name="vilandiCt" value={formData.vilandiCt} onChange={handleInputChange} /></div>
          <div><label>Vilandi Rate</label><input type="number" name="vilandiRate" value={formData.vilandiRate} onChange={handleInputChange} /></div>
        </div>
        <div className="inline-fields">
          <div><label>Diamonds Weight</label><input type="number" name="diamondsCt" value={formData.diamondsCt} onChange={handleInputChange} /></div>
          <div><label>Diamond Rate</label><input type="number" name="diamondsCtRate" value={formData.diamondsCtRate} onChange={handleInputChange} /></div>
        </div>
      </div>
    );
    // ... add other category mappings as needed similar to AddProduct.jsx
    return null;
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
        <h1>Modify Product</h1>
        {!showForm ? (
          <div className="auth-form">
            {!showSearch ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <button className="action-button" style={{ width: '100%' }} onClick={() => onSwitchPage('batch-update')}>
                  Batch Update
                </button>
                <button className="action-button secondary" style={{ width: '100%' }} onClick={() => setShowSearch(true)}>
                  Existing Modify
                </button>
                <button className="action-button secondary" style={{ marginTop: '10px', width: '100%' }} onClick={() => onSwitchPage('home')}>Back to Dashboard</button>
              </div>
            ) : (
              <>
                <label>Enter Design No. to Modify:</label>
                <input type="text" value={searchId} onChange={e => setSearchId(e.target.value)} placeholder="e.g. DES-1234" />
                <button className="action-button" style={{ width: '100%' }} onClick={() => loadProduct()}>Load Product Details</button>
                <button className="action-button secondary" style={{ marginTop: '10px', width: '100%' }} onClick={() => setShowSearch(false)}>Back to Selection</button>
              </>
            )}
          </div>
        ) : (
          <form className="auth-form" onSubmit={handleUpdate}>
            <label>Category</label>
            <select name="categoryId" value={formData.categoryId} onChange={handleInputChange} disabled>
              {categories.filter(c => c.isParent).map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>

            <label>Sub Category</label>
            <select name="subCategoryId" value={formData.subCategoryId} onChange={handleInputChange}>
              {subCategories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>

            <label>Product Name</label>
            <input type="text" name="productName" value={formData.productName} onChange={handleInputChange} required />

            <div className="inline-fields">
              <div><label>Karat</label><input type="text" name="karat" value={formData.karat} onChange={handleInputChange} /></div>
              <div><label>Design No</label><input type="text" name="designNo" value={formData.designNo} onChange={handleInputChange} /></div>
            </div>

            <label>Product Image</label>
            <div style={{ textAlign: 'center', marginBottom: '10px' }}>
              {imagePreview && <img src={getPreviewUrl(imagePreview)} alt="Preview" style={{ maxWidth: '160px', borderRadius: '8px' }} />}
              <button type="button" className="action-button secondary" style={{ margin: '10px auto', width: 'fit-content' }} onClick={() => document.getElementById('modImg').click()}>Change Image</button>
              <input type="file" id="modImg" style={{ display: 'none' }} accept="image/*" onChange={uploadImage} />
            </div>

            <label>Register ID (Current: {oldOrderId || 'None'})</label>
            <div style={{ display: 'flex', gap: '15px', marginBottom: '10px' }}>
              <label style={{ margin: 0, fontWeight: 400 }}><input type="radio" checked={orderIdOption === 'existing'} onChange={() => setOrderIdOption('existing')} /> Select Available</label>
              <label style={{ margin: 0, fontWeight: 400 }}><input type="radio" checked={orderIdOption === 'new'} onChange={() => setOrderIdOption('new')} /> Enter New</label>
            </div>

            {orderIdOption === 'existing' ? (
              <select name="availableOrderId" value={formData.availableOrderId} onChange={handleInputChange}>
                <option value="">Keep Existing ({oldOrderId})</option>
                {availableOrderIds.map(id => <option key={id} value={id}>{id}</option>)}
              </select>
            ) : (
              <input type="text" name="customOrderId" value={formData.customOrderId} onChange={handleInputChange} placeholder="Enter New Register ID" />
            )}

            <div className="inline-fields">
              <div><label>Gross</label><input type="number" name="gross" step="0.001" value={formData.gross} onChange={handleInputChange} /></div>
              <div><label>Net</label><input type="number" name="productNet" step="0.001" value={formData.productNet} onChange={handleInputChange} /></div>
            </div>

            {renderCategorySpecific()}

            <div className="labour-three-inline">
              <div><label>Labour/Gm</label><input type="number" name="labour" value={formData.labour} disabled={formData.labourAll || formData.labourPer} onChange={handleInputChange} /></div>
              <div><label>Labour Amt</label><input type="number" name="labourAll" value={formData.labourAll} disabled={formData.labour || formData.labourPer} onChange={handleInputChange} /></div>
              <div><label>Labour %</label><input type="number" name="labourPer" value={formData.labourPer} disabled={formData.labour || formData.labourAll} onChange={handleInputChange} /></div>
            </div>

            <label>Additional Fields:</label>
            <button type="button" className="action-button secondary" style={{ width: 'fit-content', marginBottom: '10px' }} onClick={() => setExtraFields([...extraFields, { name: '', qty: '', rate: '' }])}>+ Add Field</button>
            {extraFields.map((f, i) => (
              <div key={i} className="extra-field-row">
                <input type="text" placeholder="Name" value={f.name} onChange={e => { const n = [...extraFields]; n[i].name = e.target.value; setExtraFields(n); }} />
                <input type="number" placeholder="Qty" value={f.qty} onChange={e => { const n = [...extraFields]; n[i].qty = e.target.value; setExtraFields(n); }} />
                <input type="number" placeholder="Rate" value={f.rate} onChange={e => { const n = [...extraFields]; n[i].rate = e.target.value; setExtraFields(n); }} />
                <button type="button" className="remove-btn" onClick={() => setExtraFields(extraFields.filter((_, idx) => idx !== i))}>&times;</button>
              </div>
            ))}

            <label>Remarks</label>
            <textarea name="productRemarks" value={formData.productRemarks} onChange={handleInputChange}></textarea>

            <div className="auth-link-container">
              <button type="submit" className="action-button" style={{ width: '100%' }}>Update Product</button>
              <button type="button" className="action-button secondary" style={{ width: '100%', marginTop: '10px' }} onClick={() => setShowForm(false)}>Cancel</button>
            </div>
          </form>
        )}
      </main>
    </>
  );
}

export default ModifyProduct;