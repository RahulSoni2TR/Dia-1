import React, { useState, useEffect } from 'react';

const API_BASE = import.meta.env.DEV ? 'http://localhost:8080' : '';

const commonFields = [
  { key: "productName", label: "Product Name" },
  { key: "karat", label: "Karat" },
  { key: "gross", label: "Gross" },
  { key: "productNet", label: "Net" },
  { key: "labour", label: "Labour/Gm" },
  { key: "labourAll", label: "Labour Amount" },
  { key: "labourPer", label: "Labour %" },
  { key: "productRemarks", label: "Remarks" }
];

const categoryFieldsMap = {
  1: [
    { key: "pcs", label: "Pieces" },
    { key: "diaWeight", label: "Diamonds (ct)" },
    { key: "diaRate", label: "Diamond Rate" },
    { key: "diaOs", label: "Other Stones" },
    { key: "diaOsRate", label: "Other Stones Rate" }
  ],
  2: [
    { key: "vilandiCt", label: "Vilandi CT" },
    { key: "vilandiRate", label: "Vilandi Rate" },
    { key: "diamondsCt", label: "Diamonds Weight" },
    { key: "diamondsCtRate", label: "Diamond Rate" },
    { key: "beadsCt", label: "Beads (Ct)" },
    { key: "beadsRate", label: "Beads Rate" },
    { key: "pearlsGm", label: "Pearls (gm)" },
    { key: "openPearlsRate", label: "Pearls Rate" },
    { key: "ssosPearllbl", label: "SS Pearls (Ct)" },
    { key: "ssosPearlCt", label: "SS Pearls Rate" },
    { key: "otherStonesCt", label: "Other Stones (Ct)" },
    { key: "otherOsRate", label: "Other Stones Rate" }
  ],
  3: [],
  4: [
    { key: "vilandi", label: "Vilandi" },
    { key: "vRate", label: "Vilandi Rate" },
    { key: "stones", label: "Stones" },
    { key: "vsRate", label: "Stones Rate" },
    { key: "beadsCtVilandi", label: "Beads (Ct)" },
    { key: "vbRate", label: "Beads Rate" },
    { key: "pearlsGmVilandi", label: "Pearls (gm)" },
    { key: "vpRate", label: "Pearls Rate" },
    { key: "ssPearlCt", label: "SS Pearls (Ct)" },
    { key: "vssRate", label: "SS Pearls Rate" },
    { key: "vrealStone", label: "Real Stone" },
    { key: "vfitting", label: "Fitting" },
    { key: "vmoz", label: "Mozonite" },
    { key: "vmRate", label: "Mozonite Rate" }
  ],
  5: [
    { key: "stonesJadtar", label: "Stones" },
    { key: "jsRate", label: "Stones Rate" },
    { key: "beadsCtJadtar", label: "Beads (Ct)" },
    { key: "jbRate", label: "Beads Rate" },
    { key: "pearlsGmJadtar", label: "Pearls (gm)" },
    { key: "jpRate", label: "Pearls Rate" },
    { key: "ssPearlCtJadtar", label: "SS Pearl (Ct)" },
    { key: "jssRate", label: "SS Pearls Rate" },
    { key: "realStoneJadtar", label: "Real Stone" },
    { key: "jfitting", label: "Fitting" },
    { key: "jmoz", label: "Mozonite" },
    { key: "jmRate", label: "Mozonite Rate" }
  ]
};

function BatchUpdate({ onSwitchPage, onOpenModal }) {
  const [categoryId, setCategoryId] = useState('');
  const [selectedFields, setSelectedFields] = useState(new Set());
  const [fieldValues, setFieldValues] = useState({});
  const labourKeys = ['labour', 'labourAll', 'labourPer'];

  const handleCategoryChange = (e) => {
    setCategoryId(e.target.value);
    setSelectedFields(new Set());
    setFieldValues({});
  };

  const handleFieldToggle = (fieldKey) => {
    const newSelected = new Set(selectedFields);
    if (newSelected.has(fieldKey)) {
      newSelected.delete(fieldKey);
      const newValues = { ...fieldValues };
      delete newValues[fieldKey];
      setFieldValues(newValues);
    } else {
      if (labourKeys.includes(fieldKey)) {
        labourKeys.forEach(k => {
          if (k !== fieldKey) {
            newSelected.delete(k);
            const newValues = { ...fieldValues };
            delete newValues[k];
            setFieldValues(newValues);
          }
        });
      }
      newSelected.add(fieldKey);
    }
    setSelectedFields(newSelected);
  };

  const handleValueChange = (key, value) => {
    setFieldValues(prev => ({ ...prev, [key]: value }));
  };

  const handleUpdate = async () => {
    if (!categoryId || selectedFields.size === 0) return;

    const payload = {
      categoryId: parseInt(categoryId, 10),
      updates: fieldValues
    };

    try {
      const res = await fetch(`${API_BASE}/batch-update`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        credentials: 'include'
      });
      const data = await res.json();
      if (res.ok && data.success) {
        onOpenModal(`Batch update successful! ${data.updatedCount} products updated.`);
        onSwitchPage('home');
      } else {
        onOpenModal('Batch update failed. Please check your inputs.');
      }
    } catch (error) {
      onOpenModal('Network error. Please try again.');
    }
  };

  const currentAvailableFields = categoryId ? [...commonFields, ...(categoryFieldsMap[categoryId] || [])] : [];

  return (
    <>
      <header className="header">
        <div className="logo">Product <span>Manager</span></div>
        <div className="logout-container">
          <button className="logout-btn" onClick={() => onSwitchPage('login')}>Logout</button>
        </div>
      </header>

      <main className="home-main" style={{ maxWidth: '900px' }}>
        <h1>Batch Update</h1>
        <p className="subtitle">Update multiple products in a category at once.</p>

        <div className="auth-form">
          <label>Select Category</label>
          <select value={categoryId} onChange={handleCategoryChange}>
            <option value="">-- Select Category --</option>
            <option value="1">Diamond</option>
            <option value="2">Open Setting</option>
            <option value="3">Plain Gold</option>
            <option value="4">Vilandi</option>
            <option value="5">Jadtar</option>
          </select>

          {categoryId && (
            <>
              <label>Select Fields to Update</label>
              <div className="checkbox-grid">
                {currentAvailableFields.map(f => (
                  <label key={f.key} className="checkbox-item">
                    <input type="checkbox" checked={selectedFields.has(f.key)} onChange={() => handleFieldToggle(f.key)} />
                    {f.label}
                  </label>
                ))}
              </div>

              {selectedFields.size > 0 && (
                <div style={{ marginTop: '20px' }}>
                  <label>New Values</label>
                  <div className="category-specific">
                    {[...selectedFields].map(key => {
                      const label = currentAvailableFields.find(f => f.key === key)?.label;
                      return (
                        <div key={key} style={{ marginBottom: '10px' }}>
                          <label style={{ fontSize: '0.9rem', color: '#666' }}>{label}</label>
                          <input type="text" placeholder={`Enter new ${label}`} value={fieldValues[key] || ''} onChange={(e) => handleValueChange(key, e.target.value)} />
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </>
          )}

          <div className="auth-link-container">
            <button className="action-button" style={{ width: '100%' }} disabled={!categoryId || selectedFields.size === 0} onClick={handleUpdate}>Apply Batch Changes</button>
            <button className="action-button secondary" style={{ width: '100%', marginTop: '10px' }} onClick={() => onSwitchPage('home')}>Back to Dashboard</button>
          </div>
        </div>
      </main>
    </>
  );
}

export default BatchUpdate;