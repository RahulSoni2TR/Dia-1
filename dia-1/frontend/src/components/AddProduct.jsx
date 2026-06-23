import React, { useState, useEffect } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

function AddProduct({ onSwitchPage, onOpenModal }) {
  const [categories, setCategories] = useState([]);
  const [subCategories, setSubCategories] = useState([]);
  const [availableOrderIds, setAvailableOrderIds] = useState([]);
  const [formData, setFormData] = useState({
    categoryId: '',
    subCategoryId: '',
    productName: '',
    karatId: '',
    stockQuantity: '',
    availableOrderId: '',
    customOrderId: '',
    remarks: '',
  });
  const [extraFields, setExtraFields] = useState([]);
  const [imageFile, setImageFile] = useState(null);
  const [prices, setPrices] = useState({});

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [catRes, ratesRes] = await Promise.all([
          fetch(`${API_BASE}/categories`, { credentials: 'include' }),
          fetch(`${API_BASE}/rates`, { credentials: 'include' })
        ]);

        const catContentType = catRes.headers.get("content-type") || "";
        const ratesContentType = ratesRes.headers.get("content-type") || "";

        if (!catContentType.includes("application/json") || !ratesContentType.includes("application/json")) {
          console.error("Expected JSON but received HTML. This often happens if the session expired.");
          throw new Error("Server returned HTML instead of data. Please try logging in again.");
        }

        if (catRes.ok) {
          const catData = await catRes.json();
          setCategories(catData);
        }
        if (ratesRes.ok) {
          const ratesData = await ratesRes.json();
          const pObj = {};
          ratesData.forEach(r => pObj[r.commodity] = r.price);
          setPrices(pObj);
        }
      } catch (error) {
        console.error('Error fetching initial data:', error);
      }
    };
    fetchData();
  }, []);

  const handleCategoryChange = (e) => {
    const catId = e.target.value;
    const children = categories.filter(c => c.parentId === parseInt(catId));
    setSubCategories(children);
    setFormData(prev => ({
      ...prev,
      categoryId: catId,
      subCategoryId: '',
      availableOrderId: '',
      customOrderId: '',
    }));
    setAvailableOrderIds([]);
  };

  const handleSubCategoryChange = async (e) => {
    const subCatId = e.target.value;
    setFormData(prev => ({ ...prev, subCategoryId: subCatId, availableOrderId: '', customOrderId: '' }));

    if (formData.categoryId && subCatId) {
      try {
        const params = new URLSearchParams();
        params.append('categoryId', formData.categoryId);
        params.append('categoryId', subCatId);
        const response = await fetch(`${API_BASE}/available-order-ids?${params.toString()}`, { credentials: 'include' });
        if (response.ok) setAvailableOrderIds(await response.json());
      } catch (error) {
        console.error('Error fetching order IDs:', error);
      }
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleExtraFieldChange = (index, field, value) => {
    const updated = [...extraFields];
    updated[index][field] = value;
    setExtraFields(updated);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const orderId = formData.availableOrderId || formData.customOrderId.trim();
    if (!orderId) return onOpenModal('Please select or enter a Register ID.');

    const payload = new FormData();
    payload.append('productName', formData.productName);
    payload.append('stockQuantity', formData.stockQuantity || '0');
    payload.append('categoryId', formData.categoryId);
    payload.append('subCategoryId', formData.subCategoryId);
    payload.append('karatId', formData.karatId);
    payload.append('orderId', orderId);
    payload.append('remarks', formData.remarks);
    if (imageFile) payload.append('imageUrl', imageFile);

    // Map all other dynamic fields to the payload
    const common = ['categoryId', 'subCategoryId', 'productName', 'karatId', 'stockQuantity', 'availableOrderId', 'customOrderId', 'remarks'];
    Object.keys(formData).forEach(key => {
      if (!common.includes(key) && formData[key]) payload.append(key, formData[key]);
    });

    const extras = {};
    extraFields.forEach(f => { if (f.name) extras[f.name] = { qty: f.qty || '0', rate: f.rate || '0' }; });
    if (Object.keys(extras).length > 0) payload.append('customFields', JSON.stringify(extras));

    try {
      const response = await fetch(`${API_BASE}/add`, { 
        method: 'POST', 
        body: payload,
        credentials: 'include'
      });
      const data = await response.json();
      if (response.ok && data.success) {
        onOpenModal('Product added successfully!');
        onSwitchPage('home');
      } else {
        onOpenModal(data.message || 'Error adding product.');
      }
    } catch (error) {
      onOpenModal('Network error. Please try again.');
    }
  };

  const renderDynamicFields = () => {
    const sc = formData.subCategoryId;
    if (!sc) return null;

    // Group 1: Diamond Rings / Bangles / Pendants (11, 12, 13)
    if (["11", "12", "13"].includes(sc)) {
      return (
        <div className="category-specific">
          <label>Design No</label>
          <input type="text" name="designNoDR" required onChange={handleInputChange} />
          <label>Gross Weight</label>
          <input type="number" name="diamondGross" step="0.001" required onChange={handleInputChange} />
          <label>Net Weight</label>
          <input type="number" name="net" step="0.001" required onChange={handleInputChange} />
          <label>Pieces</label>
          <input type="number" name="pcs" onChange={handleInputChange} />
          <div className="inline-fields">
            <div><label>Diamonds (ct)</label><input type="number" name="diaWeight" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Diamond Rate</label><input type="number" name="diaRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Other Stones</label><input type="number" name="diaSt" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Other Stones Rate</label><input type="number" name="diaStRate" onChange={handleInputChange} /></div>
          </div>
          <div className="labour-three-inline">
            <div><label>Labour/Gm</label><input type="number" name="drLabour" disabled={formData.drLabourAll || formData.drLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour Amt</label><input type="number" name="drLabourAll" disabled={formData.drLabour || formData.drLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour %</label><input type="number" name="drLabourP" disabled={formData.drLabour || formData.drLabourAll} onChange={handleInputChange} /></div>
          </div>
        </div>
      );
    }

    // Group 2: Open Setting (14, 15)
    if (["14", "15"].includes(sc)) {
      return (
        <div className="category-specific">
          <label>Design No</label>
          <input type="text" name="designNoOS" required onChange={handleInputChange} />
          <label>Gross</label><input type="number" name="gross" step="0.001" required onChange={handleInputChange} />
          <label>Net</label><input type="number" name="net" step="0.001" required onChange={handleInputChange} />
          <div className="labour-three-inline">
            <div><label>Labour/Gm</label><input type="number" name="osLabour" disabled={formData.osLabourAll || formData.osLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour Amt</label><input type="number" name="osLabourAll" disabled={formData.osLabour || formData.osLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour %</label><input type="number" name="osLabourP" disabled={formData.osLabour || formData.osLabourAll} onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Vilandi (ct)</label><input type="number" name="vilandiCt" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Vilandi Rate</label><input type="number" name="vilandiRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Diamonds (ct)</label><input type="number" name="diamondsCt" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Diamonds Rate</label><input type="number" name="diamondsCtRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Beads (Ct.)</label><input type="number" name="beadsCt" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Beads Rate</label><input type="number" name="vilandiBeadsRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Pearls (gm)</label><input type="number" name="pearlsGm" step="0.001" onChange={handleInputChange} /></div>
            <div><label>Pearls Rate</label><input type="number" name="vilandiPearlRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>SS Pearls (Ct.)</label><input type="number" name="ssPearlCts" step="0.01" onChange={handleInputChange} /></div>
            <div><label>SS Pearls Rate</label><input type="number" name="osSSPearlRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Other Stones</label><input type="number" name="otherStonesCt" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Other Stones Rate</label><input type="number" name="openStRate" onChange={handleInputChange} /></div>
          </div>
        </div>
      );
    }

    // Group 3: Chains (16, 18)
    if (["16", "18"].includes(sc)) {
      return (
        <div className="category-specific">
          <label>Design No</label><input type="text" name="designNo" required onChange={handleInputChange} />
          <label>Gross</label><input type="number" name="chainGross" step="0.001" required onChange={handleInputChange} />
          <label>Net</label><input type="number" name="chainNet" step="0.001" required onChange={handleInputChange} />
          <div className="labour-three-inline">
            <div><label>Labour/Gm</label><input type="number" name="chainLabour" disabled={formData.chainLabourAll || formData.chainLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour Amt</label><input type="number" name="chainLabourAll" disabled={formData.chainLabour || formData.chainLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour %</label><input type="number" name="chainLabourP" disabled={formData.chainLabour || formData.chainLabourAll} onChange={handleInputChange} /></div>
          </div>
        </div>
      );
    }

    // Group 4: Diamond Earrings / Rings (19, 20)
    if (["19", "20"].includes(sc)) {
      return (
        <div className="category-specific">
          <label>Design No</label><input type="text" name="designNoEarring" required onChange={handleInputChange} />
          <label>Gross Weight</label><input type="number" name="earringGross" step="0.001" required onChange={handleInputChange} />
          <label>Net Weight</label><input type="number" name="earringNet" step="0.001" required onChange={handleInputChange} />
          <label>Pieces</label><input type="number" name="earringPcs" onChange={handleInputChange} />
          <div className="inline-fields">
            <div><label>Diamonds (ct)</label><input type="number" name="diamondWeightEarring" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Diamonds Rate</label><input type="number" name="diamondsWtRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Other Stones</label><input type="number" name="earSt" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Other Stones Rate</label><input type="number" name="earStRate" onChange={handleInputChange} /></div>
          </div>
          <div className="labour-three-inline">
            <div><label>Labour/Gm</label><input type="number" name="diamondLabour" disabled={formData.diamondLabourAll || formData.drLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour Amt</label><input type="number" name="diamondLabourAll" disabled={formData.diamondLabour || formData.drLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour %</label><input type="number" name="drLabourP" disabled={formData.diamondLabour || formData.diamondLabourAll} onChange={handleInputChange} /></div>
          </div>
        </div>
      );
    }

    // Group 5: Vilandi (9, 10)
    if (["9", "10"].includes(sc)) {
      return (
        <div className="category-specific">
          <label>Design No</label><input type="text" name="designNoVilandi" required onChange={handleInputChange} />
          <label>Gross</label><input type="number" name="vilandiGross" step="0.001" required onChange={handleInputChange} />
          <label>Net</label><input type="number" name="net" step="0.001" required onChange={handleInputChange} />
          <div className="labour-three-inline">
            <div><label>Labour/Gm</label><input type="number" name="vilandiLabour" disabled={formData.vilandiLabourAll || formData.vilandiLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour Amt</label><input type="number" name="vilandiLabourAll" disabled={formData.vilandiLabour || formData.vilandiLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour %</label><input type="number" name="vilandiLabourP" disabled={formData.vilandiLabour || formData.vilandiLabourAll} onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Vilandi (ct)</label><input type="number" name="vilandi" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Vilandi Rate</label><input type="number" name="vilandiRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Stones</label><input type="number" name="stones" onChange={handleInputChange} /></div>
            <div><label>Stones Rate</label><input type="number" name="vilandiStoneRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Beads (Ct.)</label><input type="number" name="beadsVilandi" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Beads Rate</label><input type="number" name="vilandiBeadsRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Pearls (gm)</label><input type="number" name="pearlsVilandi" step="0.001" onChange={handleInputChange} /></div>
            <div><label>Pearls Rate</label><input type="number" name="vilandiPearlRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>SS Pearls (Ct.)</label><input type="number" name="ssPearlCt" step="0.01" onChange={handleInputChange} /></div>
            <div><label>SS Pearls Rate</label><input type="number" name="vilandiSSPearlRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Real Stones</label><input type="number" name="realStone" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Fitting</label><input type="number" name="vilandiFitting" onChange={handleInputChange} /></div>
          </div>
        </div>
      );
    }

    // Group 6: Jadtar Register (6, 7, 8, 17)
    if (["6", "7", "8", "17"].includes(sc)) {
      return (
        <div className="category-specific">
          <label>Design No</label><input type="text" name="designNoJadtar" required onChange={handleInputChange} />
          <label>Gross</label><input type="number" name="jadtarGross" step="0.001" required onChange={handleInputChange} />
          <label>Net</label><input type="number" name="jadtarNet" step="0.001" required onChange={handleInputChange} />
          <div className="labour-three-inline">
            <div><label>Labour/Gm</label><input type="number" name="jadtarLabour" disabled={formData.jadtarLabourAll || formData.jadtarLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour Amt</label><input type="number" name="jadtarLabourAll" disabled={formData.jadtarLabour || formData.jadtarLabourP} onChange={handleInputChange} /></div>
            <div><label>Labour %</label><input type="number" name="jadtarLabourP" disabled={formData.jadtarLabour || formData.jadtarLabourAll} onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Stones (ct)</label><input type="number" name="jadtarStones" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Stones Rate</label><input type="number" name="jadtarStoneRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Beads (Ct.)</label><input type="number" name="jadtarBeads" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Beads Rate</label><input type="number" name="jadtarBeadsRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Pearls Weight (gm)</label><input type="number" name="jadtarPearls" step="0.001" onChange={handleInputChange} /></div>
            <div><label>Pearls Rate</label><input type="number" name="jadtarPearlRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>SS Pearls (Ct.)</label><input type="number" name="jadtarSSPearl" step="0.01" onChange={handleInputChange} /></div>
            <div><label>SS Pearls Rate</label><input type="number" name="jadtarSSPearlRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Real Stones</label><input type="number" name="jadtarRealStone" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Fitting</label><input type="number" name="jadtarFitting" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Vilandi (ct)</label><input type="number" name="jadvilandi" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Vilandi Rate</label><input type="number" name="jadvilandiRate" onChange={handleInputChange} /></div>
          </div>
          <div className="inline-fields">
            <div><label>Mozonite</label><input type="number" name="mozStone" step="0.01" onChange={handleInputChange} /></div>
            <div><label>Mozonite Rate</label><input type="number" name="mozStoneRate" onChange={handleInputChange} /></div>
          </div>
        </div>
      );
    }
    return null;
  };

  const priceLabel = `Gold 24K: ₹${prices['24.00'] || 'N/A'} | Silver: ₹${prices['silver'] || 'N/A'} | Diamond: ₹${prices['diamond'] || 'N/A'} | GST: ${prices['gst'] || '3'}%`;

  return (
    <>
      <header className="header">
        <div className="logo">Product <span>Manager</span></div>
        <div className="logout-container">
          <button className="logout-btn" onClick={() => onSwitchPage('login')}>Logout</button>
        </div>
      </header>

      <div className="scrolling-prices-container">
        <div className="scrolling-prices">
          <span className="scrolling-text">{priceLabel}</span>
        </div>
      </div>

      <main className="home-main">
        <h1>Add New Product</h1>
        <form className="auth-form" onSubmit={handleSubmit}>
          <label>Category</label>
          <select name="categoryId" required value={formData.categoryId} onChange={handleCategoryChange}>
            <option value="">Select Category</option>
            {categories.filter(c => c.isParent).map(c => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>

          {formData.categoryId && (
            <>
              <label>Sub Category</label>
              <select name="subCategoryId" required value={formData.subCategoryId} onChange={handleSubCategoryChange}>
                <option value="">Select Sub Category</option>
                {subCategories.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </>
          )}

          <label>Product Name</label>
          <input type="text" name="productName" required value={formData.productName} onChange={handleInputChange} />

          <label>Gold Karat</label>
          <select name="karatId" required value={formData.karatId} onChange={handleInputChange}>
            <option value="">Select Karat</option>
            <option value="10">9K</option>
            <option value="14">14K</option>
            <option value="18">18K</option>
            <option value="22">22K</option>
            <option value="24">24K</option>
          </select>

          <label>Stock Quantity</label>
          <input type="number" name="stockQuantity" value={formData.stockQuantity} onChange={handleInputChange} />

          <label>Product Image</label>
          <input type="file" accept="image/*" onChange={(e) => setImageFile(e.target.files[0])} />

          {formData.subCategoryId && (
            <>
              <div className="inline-fields" style={{ marginTop: '10px' }}>
                <div>
                  <label>Available Register IDs</label>
                  <select name="availableOrderId" value={formData.availableOrderId} onChange={handleInputChange} disabled={formData.customOrderId}>
                    <option value="">Select from available</option>
                    {availableOrderIds.map(id => <option key={id} value={id}>{id}</option>)}
                  </select>
                </div>
                <div>
                  <label>OR Enter New ID</label>
                  <input type="text" name="customOrderId" placeholder="Custom ID" value={formData.customOrderId} onChange={handleInputChange} disabled={formData.availableOrderId} />
                </div>
              </div>
              {renderDynamicFields()}
            </>
          )}

          <div id="extraFieldsContainer">
            <label>Additional Fields:</label>
            <button type="button" className="action-button secondary" onClick={() => setExtraFields([...extraFields, { name: '', qty: '', rate: '' }])} style={{ marginBottom: '10px', width: 'fit-content' }}>+ Add Field</button>
            {extraFields.map((field, index) => (
              <div key={index} className="extra-field-row">
                <input type="text" placeholder="Name" required value={field.name} onChange={(e) => handleExtraFieldChange(index, 'name', e.target.value)} />
                <input type="number" placeholder="Qty" step="0.001" required value={field.qty} onChange={(e) => handleExtraFieldChange(index, 'qty', e.target.value)} />
                <input type="number" placeholder="Rate" required value={field.rate} onChange={(e) => handleExtraFieldChange(index, 'rate', e.target.value)} />
                <button type="button" className="remove-btn" onClick={() => setExtraFields(extraFields.filter((_, i) => i !== index))}>&times;</button>
              </div>
            ))}
          </div>

          <label>Remarks</label>
          <textarea name="remarks" value={formData.remarks} onChange={handleInputChange}></textarea>

          <div className="auth-link-container">
            <input type="submit" className="action-button" value="Add Product" style={{ width: '100%' }} />
            <button type="button" className="action-button secondary" style={{ width: '100%', marginTop: '10px' }} onClick={() => onSwitchPage('home')}>Back to Dashboard</button>
          </div>
        </form>
      </main>
    </>
  );
}

export default AddProduct;